/*
 * This is the source code of Telegram for Android v. 2.x.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013-2015.
 */

package org.telegram.messenger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.util.Base64;

import org.telegram.android.AndroidUtilities;

import java.io.File;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionsManager implements Action.ActionDelegate{

    private ArrayList<Long> sessionsToDestroy = new ArrayList<>();
    private ArrayList<Long> destroyingSessions = new ArrayList<>();
    private HashMap<Integer, ArrayList<Long>> quickAckIdToRequestIds = new HashMap<>();

    private HashMap<Long, Integer> pingIdToDate = new HashMap<>();
    private ConcurrentHashMap<Integer, ArrayList<Long>> requestsByGuids = new ConcurrentHashMap<>(100, 1.0f, 2);
    private ConcurrentHashMap<Long, Integer> requestsByClass = new ConcurrentHashMap<>(100, 1.0f, 2);
    private volatile int connectionState = 2;

    private ArrayList<RPCRequest> requestQueue = new ArrayList<>();
    private ArrayList<RPCRequest> runningRequests = new ArrayList<>();
    private ArrayList<Action> actionQueue = new ArrayList<>();

    private ArrayList<Integer> unknownDatacenterIds = new ArrayList<>();
    private ArrayList<Integer> unauthorizedDatacenterIds = new ArrayList<>();
    private final HashMap<Integer, ArrayList<NetworkMessage>> genericMessagesToDatacenters = new HashMap<>();

    private TLRPC.TL_auth_exportedAuthorization movingAuthorization;
    public static final int DEFAULT_DATACENTER_ID = Integer.MAX_VALUE;
    private static final int DC_UPDATE_TIME = 60 * 60;
    private long lastOutgoingMessageId = 0;
    private int isTestBackend = 0;
    private int timeDifference = 0;
    private int currentPingTime;
    private int lastDestroySessionRequestTime;
    private boolean updatingDcSettings = false;
    private int updatingDcStartTime = 0;
    private int lastDcUpdateTime = 0;
    private long pushSessionId;
    private boolean registeringForPush = false;

    private boolean paused = false;
    private long lastPingTime = System.currentTimeMillis();
    private long lastPushPingTime = 0;
    private boolean pushMessagesReceived = true;
    private boolean sendingPushPing = false;
    private int nextSleepTimeout = 30000;
    private long nextPingId = 0;

    private long lastPauseTime = System.currentTimeMillis();
    private boolean appPaused = true;

    private volatile long nextCallToken = 1;

    private PowerManager.WakeLock wakeLock = null;

    private static volatile ConnectionsManager Instance = null;

    public static ConnectionsManager getInstance() {
        ConnectionsManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (ConnectionsManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new ConnectionsManager();
                }
            }
        }
        return localInstance;
    }

    private Runnable stageRunnable = new Runnable() {
        @Override
        public void run() {
            Utilities.stageQueue.handler.removeCallbacks(stageRunnable);

            long currentTime = System.currentTimeMillis();
            if (lastPauseTime != 0 && lastPauseTime < currentTime - nextSleepTimeout) {
                boolean dontSleep = !pushMessagesReceived;
                if (!dontSleep) {
                    for (RPCRequest request : runningRequests) {
                        if (request.rawRequest instanceof TLRPC.TL_get_future_salts) {
                            dontSleep = true;
                        } else if (request.retryCount < 10 && (request.runningStartTime + 60 > (int) (currentTime / 1000)) && ((request.flags & RPCRequest.RPCRequestClassDownloadMedia) != 0 || (request.flags & RPCRequest.RPCRequestClassUploadMedia) != 0)) {
                            dontSleep = true;
                            break;
                        }
                    }
                }
                if (!dontSleep) {
                    for (RPCRequest request : requestQueue) {
                        if (request.rawRequest instanceof TLRPC.TL_get_future_salts) {
                            dontSleep = true;
                        } else if ((request.flags & RPCRequest.RPCRequestClassDownloadMedia) != 0 || (request.flags & RPCRequest.RPCRequestClassUploadMedia) != 0) {
                            dontSleep = true;
                            break;
                        }
                    }
                }
                if (!dontSleep) {
                    if (!paused) {
                        FileLog.e("tmessages", "pausing network and timers by sleep time = " + nextSleepTimeout);

                    }
                    try {
                        paused = true;
                        Utilities.stageQueue.postRunnable(stageRunnable, 1000);
                        return;
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                } else {
                    lastPauseTime += 30 * 1000;
                    FileLog.e("tmessages", "don't sleep 30 seconds because of salt, upload or download request");
                }
            }
            if (paused) {
                paused = false;
                FileLog.e("tmessages", "resume network and timers");
            }


            Utilities.stageQueue.postRunnable(stageRunnable, 1000);
        }
    };

    public ConnectionsManager() {
        lastOutgoingMessageId = 0;
        loadSession();

        if (!isNetworkOnline()) {
            connectionState = 1;
        }

        Utilities.stageQueue.postRunnable(stageRunnable, 1000);

        try {
            PowerManager pm = (PowerManager) ApplicationLoader.applicationContext.getSystemService(Context.POWER_SERVICE);
            wakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "lock");
            wakeLock.setReferenceCounted(false);
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
    }

    public int getConnectionState() {
        return connectionState;
    }

    public void setConnectionState(int state) {
        connectionState = state;
    }

    private void resumeNetworkInternal() {
        if (paused) {
            lastPauseTime = System.currentTimeMillis();
            nextSleepTimeout = 30000;
            FileLog.e("tmessages", "wakeup network in background");
        } else if (lastPauseTime != 0) {
            lastPauseTime = System.currentTimeMillis();
            FileLog.e("tmessages", "reset sleep timeout");
        }
    }

    public void resumeNetworkMaybe() {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                resumeNetworkInternal();
            }
        });
    }

    //TODO NEED?
    public void applicationMovedToForeground() {
        Utilities.stageQueue.postRunnable(stageRunnable);
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (paused) {
                    nextSleepTimeout = 30000;
                    FileLog.e("tmessages", "reset timers by application moved to foreground");
                }
            }
        });
    }

    //TODO NEED ?
    public void setAppPaused(final boolean value, final boolean byScreenState) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (!byScreenState) {
                    appPaused = value;
                    FileLog.e("tmessages", "app paused = " + value);
                }
                if (value) {
                    if (byScreenState) {
                        if (lastPauseTime == 0) {
                            lastPauseTime = System.currentTimeMillis();
                        }
                    } else {
                        lastPauseTime = System.currentTimeMillis();
                    }
                } else {
                    if (appPaused) {
                        return;
                    }
                    FileLog.e("tmessages", "reset app pause time");
                    if (lastPauseTime != 0 && System.currentTimeMillis() - lastPauseTime > 5000) {
//                        ContactsController.getInstance().checkContacts();
                    }
                    lastPauseTime = 0;
                    ConnectionsManager.getInstance().applicationMovedToForeground();
                }
            }
        });
    }

    public long getPauseTime() {
        return lastPauseTime;
    }

    //================================================================================
    // Config and session manage
    //================================================================================


    void setTimeDifference(int diff) {
        boolean store = Math.abs(diff - timeDifference) > 25;
        timeDifference = diff;
        if (store) {
            saveSession();
        }
    }

    public void switchBackend() {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                if (isTestBackend == 0) {
                    isTestBackend = 1;
                } else {
                    isTestBackend = 0;
                }
                saveSession();
                Utilities.stageQueue.postRunnable(new Runnable() {
                    @Override
                    public void run() {
                        UserConfig.clearConfig();
                        System.exit(0);
                    }
                });
            }
        });
    }

    private void loadSession() {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                File configFile = new File(ApplicationLoader.applicationContext.getFilesDir(), "config.dat");
                if (configFile.exists()) {
                    try {
                        SerializedData data = new SerializedData(configFile);
                        isTestBackend = data.readInt32(false);
                        int version = data.readInt32(false);
                        sessionsToDestroy.clear();
                        int count = data.readInt32(false);
                        for (int a = 0; a < count; a++) {
                            sessionsToDestroy.add(data.readInt64(false));
                        }
                        timeDifference = data.readInt32(false);
                        count = data.readInt32(false);
//                        for (int a = 0; a < count; a++) {
//                            Datacenter datacenter = new Datacenter(data, 0);
//                            datacenters.put(datacenter.datacenterId, datacenter);
//                        }
                        data.cleanup();
                    } catch (Exception e) {
                        UserConfig.clearConfig();
                    }
                } else {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("dataconfig", Context.MODE_PRIVATE);
                    isTestBackend = preferences.getInt("datacenterSetId", 0);
                    timeDifference = preferences.getInt("timeDifference", 0);
                    lastDcUpdateTime = preferences.getInt("lastDcUpdateTime", 0);
                    pushSessionId = preferences.getLong("pushSessionId", 0);

                    try {
                        sessionsToDestroy.clear();
                        String sessionsString = preferences.getString("sessionsToDestroy", null);
                        if (sessionsString != null) {
                            byte[] sessionsBytes = Base64.decode(sessionsString, Base64.DEFAULT);
                            if (sessionsBytes != null) {
                                SerializedData data = new SerializedData(sessionsBytes);
                                int count = data.readInt32(false);
                                for (int a = 0; a < count; a++) {
                                    sessionsToDestroy.add(data.readInt64(false));
                                }
                                data.cleanup();
                            }
                        }
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }

                    try {
                        String datacentersString = preferences.getString("datacenters", null);
                        if (datacentersString != null) {
                            byte[] datacentersBytes = Base64.decode(datacentersString, Base64.DEFAULT);
                            if (datacentersBytes != null) {
                                SerializedData data = new SerializedData(datacentersBytes);
                                int count = data.readInt32(false);
                                data.cleanup();
                            }
                        }
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }


            }
        });
    }

    @SuppressLint("NewApi")
    protected static boolean useIpv6Address() {
        if (BuildVars.DEBUG_VERSION && Build.VERSION.SDK_INT >= 19) {
            try {
                NetworkInterface networkInterface;
                Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
                while (networkInterfaces.hasMoreElements()) {
                    networkInterface = networkInterfaces.nextElement();
                    if (!networkInterface.isUp() || networkInterface.isLoopback() || networkInterface.getInterfaceAddresses().isEmpty()) {
                        continue;
                    }
                    FileLog.e("tmessages", "valid interface: " + networkInterface);
                    for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                        InetAddress inetAddress = address.getAddress();
                        if (BuildVars.DEBUG_VERSION) {
                            FileLog.e("tmessages", "address: " + inetAddress.getHostAddress());
                        }
                        if (inetAddress.isLinkLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isMulticastAddress()) {
                            continue;
                        }
                        if (BuildVars.DEBUG_VERSION) {
                            FileLog.e("tmessages", "address is good");
                        }
                    }
                }
            } catch (Throwable e) {
                FileLog.e("tmessages", e);
            }
        }
        if (Build.VERSION.SDK_INT < 50) {
            return false;
        }
        try {
            NetworkInterface networkInterface;
            Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
            while (networkInterfaces.hasMoreElements()) {
                networkInterface = networkInterfaces.nextElement();
                if (!networkInterface.isUp() || networkInterface.isLoopback()) {
                    continue;
                }
                boolean hasIpv4 = false;
                boolean hasIpv6 = false;
                for (InterfaceAddress address : networkInterface.getInterfaceAddresses()) {
                    InetAddress inetAddress = address.getAddress();
                    if (inetAddress.isLinkLocalAddress() || inetAddress.isLoopbackAddress() || inetAddress.isMulticastAddress()) {
                        continue;
                    }
                    if (inetAddress instanceof Inet6Address) {
                        hasIpv6 = true;
                    } else if (inetAddress instanceof Inet4Address) {
                        hasIpv4 = true;
                    }
                }
                if (!hasIpv4 && hasIpv6) {
                    return true;
                }
            }
        } catch (Throwable e) {
            FileLog.e("tmessages", e);
        }

        return false;
    }

    private void saveSession() {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                try {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("dataconfig", Context.MODE_PRIVATE);
                    SharedPreferences.Editor editor = preferences.edit();
//                    editor.putInt("datacenterSetId", isTestBackend);
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }
        });
    }


    public void cleanUp() {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                for (int a = 0; a < requestQueue.size(); a++) {
                    RPCRequest request = requestQueue.get(a);
                    if ((request.flags & RPCRequest.RPCRequestClassWithoutLogin) != 0) {
                        continue;
                    }
                    requestQueue.remove(a);
                    if (request.completionBlock != null) {
                        TLRPC.TL_error implicitError = new TLRPC.TL_error();
                        implicitError.code = -1000;
                        implicitError.text = "";
                        request.completionBlock.run(null, implicitError);
                    }
                    a--;
                }
                for (int a = 0; a < runningRequests.size(); a++) {
                    RPCRequest request = runningRequests.get(a);
                    if ((request.flags & RPCRequest.RPCRequestClassWithoutLogin) != 0) {
                        continue;
                    }
                    runningRequests.remove(a);
                    if (request.completionBlock != null) {
                        TLRPC.TL_error implicitError = new TLRPC.TL_error();
                        implicitError.code = -1000;
                        implicitError.text = "";
                        request.completionBlock.run(null, implicitError);
                    }
                    a--;
                }
                pingIdToDate.clear();
                quickAckIdToRequestIds.clear();


                sessionsToDestroy.clear();
                saveSession();
            }
        });
    }


    long getTimeFromMsgId(long messageId) {
        return (long) (messageId / 4294967296.0 * 1000);
    }

    //================================================================================
    // Requests manage
    //================================================================================
    int lastClassGuid = 1;

    public int generateClassGuid() {
        int guid = lastClassGuid++;
        requestsByGuids.put(guid, new ArrayList<Long>());
        return guid;
    }

    public void cancelRpcsForClassGuid(int guid) {
        ArrayList<Long> requests = requestsByGuids.get(guid);
        if (requests != null) {
            for (Long request : requests) {
                cancelRpc(request, true);
            }
            requestsByGuids.remove(guid);
        }
    }


    public void cancelRpc(final long token, final boolean notifyServer) {
        cancelRpc(token, notifyServer, false);
    }

    public void cancelRpc(final long token, final boolean notifyServer, final boolean ifNotSent) {
        if (token == 0) {
            return;
        }
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                boolean found = false;

                for (int i = 0; i < requestQueue.size(); i++) {
                    RPCRequest request = requestQueue.get(i);
                    if (request.token == token) {
                        found = true;
                        request.cancelled = true;
                        FileLog.d("tmessages", "===== Cancelled queued rpc request " + request.rawRequest);
                        requestQueue.remove(i);
                        break;
                    }
                }

                if (!ifNotSent) {
                    for (int i = 0; i < runningRequests.size(); i++) {
                        RPCRequest request = runningRequests.get(i);
                        if (request.token == token) {
                            found = true;

                            FileLog.d("tmessages", "===== Cancelled running rpc request " + request.rawRequest);

                            if ((request.flags & RPCRequest.RPCRequestClassGeneric) != 0) {
                                if (notifyServer) {
                                    TLRPC.TL_rpc_drop_answer dropAnswer = new TLRPC.TL_rpc_drop_answer();
                                    dropAnswer.req_msg_id = request.runningMessageId;
                                }
                            }

                            request.cancelled = true;
                            request.rawRequest.freeResources();
                            request.rpcRequest.freeResources();
                            runningRequests.remove(i);
                            break;
                        }
                    }
                    if (!found) {
                        FileLog.d("tmessages", "***** Warning: cancelling unknown request");
                    }
                }
            }
        });
    }

    //TODO NEED
    public static boolean isNetworkOnline() {
        try {
            ConnectivityManager cm = (ConnectivityManager) ApplicationLoader.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null && (netInfo.isConnectedOrConnecting() || netInfo.isAvailable())) {
                return true;
            }

            netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

            if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                return true;
            } else {
                netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
                if (netInfo != null && netInfo.isConnectedOrConnecting()) {
                    return true;
                }
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
            return true;
        }
        return false;
    }

    //TODO NEED
    public static boolean isRoaming() {
        try {
            ConnectivityManager cm = (ConnectivityManager) ApplicationLoader.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getActiveNetworkInfo();
            if (netInfo != null) {
                return netInfo.isRoaming();
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return false;
    }

    //TODO NEED
    public static boolean isConnectedToWiFi() {
        try {
            ConnectivityManager cm = (ConnectivityManager) ApplicationLoader.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo netInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            if (netInfo != null && netInfo.getState() == NetworkInfo.State.CONNECTED) {
                return true;
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }
        return false;
    }

    //TODO NEED
    public int getCurrentTime() {
        return (int) (System.currentTimeMillis() / 1000) + timeDifference;
    }

    public int getTimeDifference() {
        return timeDifference;
    }


    //================================================================================
    // TCPConnection delegate
    //================================================================================



    //================================================================================
    // Actors manage
    //================================================================================

    public void dequeueActor(final Action actor, final boolean execute) {
        if (actionQueue.size() == 0 || execute) {
            actor.execute(null);
        }
        actionQueue.add(actor);
    }

    @Override
    public void ActionDidFinishExecution(final Action action, HashMap<String, Object> params) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                actionQueue.remove(action);
                action.delegate = null;
            }
        });
    }

    @Override
    public void ActionDidFailExecution(final Action action) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                actionQueue.remove(action);
                action.delegate = null;
            }
        });
    }
}
