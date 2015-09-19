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
import android.content.pm.PackageInfo;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.PowerManager;
import android.util.Base64;

import org.telegram.android.AndroidUtilities;
import org.telegram.android.LocaleController;
import org.telegram.android.NotificationCenter;

import java.io.File;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ConnectionsManager implements Action.ActionDelegate, TcpConnection.TcpConnectionDelegate {
    private HashMap<Integer, Datacenter> datacenters = new HashMap<>();

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
    private ArrayList<Integer> neededDatacenterIds = new ArrayList<>();
    private ArrayList<Integer> unauthorizedDatacenterIds = new ArrayList<>();
    private final HashMap<Integer, ArrayList<NetworkMessage>> genericMessagesToDatacenters = new HashMap<>();

    private TLRPC.TL_auth_exportedAuthorization movingAuthorization;
    public static final int DEFAULT_DATACENTER_ID = Integer.MAX_VALUE;
    private static final int DC_UPDATE_TIME = 60 * 60;
    protected int currentDatacenterId;
    protected int movingToDatacenterId;
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
            if (datacenters != null) {
                Datacenter datacenter = datacenterWithId(currentDatacenterId);
                if (sendingPushPing && lastPushPingTime < System.currentTimeMillis() - 30000 || Math.abs(lastPushPingTime - System.currentTimeMillis()) > 60000 * 3 + 10000) {
                    lastPushPingTime = 0;
                    sendingPushPing = false;
                    if (datacenter != null && datacenter.pushConnection != null) {
                        datacenter.pushConnection.suspendConnection(true);
                    }
                    FileLog.e("tmessages", "push ping timeout");
                }
                if (lastPushPingTime < System.currentTimeMillis() - 60000 * 3) {
                    FileLog.e("tmessages", "time for push ping");
                    lastPushPingTime = System.currentTimeMillis();
                    if (datacenter != null) {
//                        generatePing(datacenter, true);
                    }
                }
            }

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
                        for (Datacenter datacenter : datacenters.values()) {
                            datacenter.suspendConnections();
                        }
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

            if (datacenters != null) {
//                MessagesController.getInstance().updateTimerProc();
                Datacenter datacenter = datacenterWithId(currentDatacenterId);
                if (datacenter != null) {
                    if (datacenter.authKey != null) {
                        if (lastPingTime < System.currentTimeMillis() - 19000) {
                            lastPingTime = System.currentTimeMillis();
//                            generatePing();
                        }
                        if (!updatingDcSettings && lastDcUpdateTime < (int) (System.currentTimeMillis() / 1000) - DC_UPDATE_TIME) {
                        }
                        processRequestQueue(0, 0);
                    } else {
                        boolean notFound = true;
                        for (Action actor : actionQueue) {
                            if (actor instanceof HandshakeAction) {
                                HandshakeAction eactor = (HandshakeAction) actor;
                                if (eactor.datacenter.datacenterId == datacenter.datacenterId) {
                                    notFound = false;
                                    break;
                                }
                            }
                        }
                        if (notFound) {
                            HandshakeAction actor = new HandshakeAction(datacenter);
                            actor.delegate = ConnectionsManager.this;
                            dequeueActor(actor, true);
                        }
                    }
                }
            }

            Utilities.stageQueue.postRunnable(stageRunnable, 1000);
        }
    };

    public ConnectionsManager() {
        lastOutgoingMessageId = 0;
        movingToDatacenterId = DEFAULT_DATACENTER_ID;
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

    public Datacenter datacenterWithId(int datacenterId) {
        if (datacenterId == DEFAULT_DATACENTER_ID) {
            return datacenters.get(currentDatacenterId);
        }
        return datacenters.get(datacenterId);
    }

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
                datacenters.clear();
                fillDatacenters();
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
                        for (int a = 0; a < count; a++) {
                            Datacenter datacenter = new Datacenter(data, 0);
                            datacenters.put(datacenter.datacenterId, datacenter);
                        }
                        currentDatacenterId = data.readInt32(false);
                        data.cleanup();
                    } catch (Exception e) {
                        UserConfig.clearConfig();
                    }
                } else {
                    SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("dataconfig", Context.MODE_PRIVATE);
                    isTestBackend = preferences.getInt("datacenterSetId", 0);
                    currentDatacenterId = preferences.getInt("currentDatacenterId", 0);
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
                                for (int a = 0; a < count; a++) {
                                    Datacenter datacenter = new Datacenter(data, 1);
                                    datacenters.put(datacenter.datacenterId, datacenter);
                                }
                                data.cleanup();
                            }
                        }
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }

                if (currentDatacenterId != 0 && UserConfig.isClientActivated()) {
                    Datacenter datacenter = datacenterWithId(currentDatacenterId);
                    if (datacenter == null || datacenter.authKey == null) {
                        currentDatacenterId = 0;
                        datacenters.clear();
                        UserConfig.clearConfig();
                    }
                }

                fillDatacenters();

                if (datacenters.size() != 0 && currentDatacenterId == 0 || pushSessionId == 0) {
                    if (pushSessionId == 0) {
                        pushSessionId = Utilities.random.nextLong();
                    }
                    if (currentDatacenterId == 0) {
                        currentDatacenterId = 2;
                    }
                    saveSession();
                }
                movingToDatacenterId = DEFAULT_DATACENTER_ID;
            }
        });
    }

    private void fillDatacenters() {
        if (datacenters.size() == 0) {
            if (isTestBackend == 0) {
                Datacenter datacenter = new Datacenter();
                datacenter.datacenterId = 1;
                datacenter.addAddressAndPort("149.154.175.50", 443, 0);
                datacenter.addAddressAndPort("2001:b28:f23d:f001:0000:0000:0000:000a", 443, 1);
                datacenters.put(datacenter.datacenterId, datacenter);

                datacenter = new Datacenter();
                datacenter.datacenterId = 2;
                datacenter.addAddressAndPort("149.154.167.51", 443, 0);
                datacenter.addAddressAndPort("2001:67c:4e8:f002:0000:0000:0000:000a", 443, 1);
                datacenters.put(datacenter.datacenterId, datacenter);

                datacenter = new Datacenter();
                datacenter.datacenterId = 3;
                datacenter.addAddressAndPort("149.154.175.100", 443, 0);
                datacenter.addAddressAndPort("2001:b28:f23d:f003:0000:0000:0000:000a", 443, 1);
                datacenters.put(datacenter.datacenterId, datacenter);

                datacenter = new Datacenter();
                datacenter.datacenterId = 4;
                datacenter.addAddressAndPort("149.154.167.91", 443, 0);
                datacenter.addAddressAndPort("2001:67c:4e8:f004:0000:0000:0000:000a", 443, 1);
                datacenters.put(datacenter.datacenterId, datacenter);

                datacenter = new Datacenter();
                datacenter.datacenterId = 5;
                datacenter.addAddressAndPort("149.154.171.5", 443, 0);
                datacenter.addAddressAndPort("2001:b28:f23f:f005:0000:0000:0000:000a", 443, 1);
                datacenters.put(datacenter.datacenterId, datacenter);
            } else {
                Datacenter datacenter = new Datacenter();
                datacenter.datacenterId = 1;
                datacenter.addAddressAndPort("149.154.175.10", 443, 0);
                datacenter.addAddressAndPort("2001:b28:f23d:f001:0000:0000:0000:000e", 443, 1);
                datacenters.put(datacenter.datacenterId, datacenter);

                datacenter = new Datacenter();
                datacenter.datacenterId = 2;
                datacenter.addAddressAndPort("149.154.167.40", 443, 0);
                datacenter.addAddressAndPort("2001:67c:4e8:f002:0000:0000:0000:000e", 443, 1);
                datacenters.put(datacenter.datacenterId, datacenter);

                datacenter = new Datacenter();
                datacenter.datacenterId = 3;
                datacenter.addAddressAndPort("149.154.175.117", 443, 0);
                datacenter.addAddressAndPort("2001:b28:f23d:f003:0000:0000:0000:000e", 443, 1);
                datacenters.put(datacenter.datacenterId, datacenter);
            }
        } else if (datacenters.size() == 1) {
            Datacenter datacenter = new Datacenter();
            datacenter.datacenterId = 2;
            datacenter.addAddressAndPort("149.154.167.51", 443, 0);
            datacenter.addAddressAndPort("2001:67c:4e8:f002:0000:0000:0000:000a", 443, 1);
            datacenters.put(datacenter.datacenterId, datacenter);

            datacenter = new Datacenter();
            datacenter.datacenterId = 3;
            datacenter.addAddressAndPort("149.154.175.100", 443, 0);
            datacenter.addAddressAndPort("2001:b28:f23d:f003:0000:0000:0000:000a", 443, 1);
            datacenters.put(datacenter.datacenterId, datacenter);

            datacenter = new Datacenter();
            datacenter.datacenterId = 4;
            datacenter.addAddressAndPort("149.154.167.91", 443, 0);
            datacenter.addAddressAndPort("2001:67c:4e8:f004:0000:0000:0000:000a", 443, 1);
            datacenters.put(datacenter.datacenterId, datacenter);

            datacenter = new Datacenter();
            datacenter.datacenterId = 5;
            datacenter.addAddressAndPort("149.154.171.5", 443, 0);
            datacenter.addAddressAndPort("2001:b28:f23f:f005:0000:0000:0000:000a", 443, 1);
            datacenters.put(datacenter.datacenterId, datacenter);
        }
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
                    editor.putInt("datacenterSetId", isTestBackend);
                    Datacenter currentDatacenter = datacenterWithId(currentDatacenterId);
                    if (currentDatacenter != null) {
                        editor.putInt("currentDatacenterId", currentDatacenterId);
                        editor.putInt("timeDifference", timeDifference);
                        editor.putInt("lastDcUpdateTime", lastDcUpdateTime);
                        editor.putLong("pushSessionId", pushSessionId);

                        ArrayList<Long> sessions = new ArrayList<>();
                        currentDatacenter.getSessions(sessions);

                        if (!sessions.isEmpty()) {
                            SerializedData data = new SerializedData(sessions.size() * 8 + 4);
                            data.writeInt32(sessions.size());
                            for (long session : sessions) {
                                data.writeInt64(session);
                            }
                            editor.putString("sessionsToDestroy", Base64.encodeToString(data.toByteArray(), Base64.DEFAULT));
                            data.cleanup();
                        } else {
                            editor.remove("sessionsToDestroy");
                        }

                        if (!datacenters.isEmpty()) {
                            SerializedData data = new SerializedData();
                            data.writeInt32(datacenters.size());
                            for (Datacenter datacenter : datacenters.values()) {
                                datacenter.SerializeToStream(data);
                            }
                            editor.putString("datacenters", Base64.encodeToString(data.toByteArray(), Base64.DEFAULT));
                            data.cleanup();
                        } else {
                            editor.remove("datacenters");
                        }
                    } else {
                        editor.remove("datacenters");
                        editor.remove("sessionsToDestroy");
                        editor.remove("currentDatacenterId");
                        editor.remove("timeDifference");
                    }
                    editor.commit();
                    File configFile = new File(ApplicationLoader.applicationContext.getFilesDir(), "config.dat");
                    if (configFile.exists()) {
                        configFile.delete();
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }
        });
    }

    void clearRequestsForRequestClass(int requestClass, Datacenter datacenter) {
        for (RPCRequest request : runningRequests) {
            Datacenter dcenter = datacenterWithId(request.runningDatacenterId);
            if ((request.flags & requestClass) != 0 && dcenter != null && dcenter.datacenterId == datacenter.datacenterId) {
                request.runningMessageId = 0;
                request.runningMessageSeqNo = 0;
                request.runningStartTime = 0;
                request.runningMinStartTime = 0;
                request.transportChannelToken = 0;
            }
        }
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

                for (Datacenter datacenter : datacenters.values()) {
                    datacenter.recreateSessions();
                    datacenter.authorized = false;
                }

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

    public void bindRequestToGuid(final Long request, final int guid) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                ArrayList<Long> requests = requestsByGuids.get(guid);
                if (requests != null) {
                    requests.add(request);
                    requestsByClass.put(request, guid);
                }
            }
        });
    }

    public void removeRequestInClass(final Long request) {
        AndroidUtilities.runOnUIThread(new Runnable() {
            @Override
            public void run() {
                Integer guid = requestsByClass.get(request);
                if (guid != null) {
                    ArrayList<Long> requests = requestsByGuids.get(guid);
                    if (requests != null) {
                        requests.remove(request);
                    }
                }
            }
        });
    }







    private TLObject wrapInLayer(TLObject object, int datacenterId, RPCRequest request) {
        if (object.layer() > 0) {
            Datacenter datacenter = datacenterWithId(datacenterId);
            if (datacenter == null || datacenter.lastInitVersion != BuildVars.BUILD_VERSION) {
                request.initRequest = true;
                TLRPC.initConnection invoke = new TLRPC.initConnection();
                invoke.query = object;
                invoke.api_id = BuildVars.APP_ID;
                try {
                    invoke.lang_code = LocaleController.getLocaleString(LocaleController.getInstance().getSystemDefaultLocale());
                    if (invoke.lang_code.length() == 0) {
                        invoke.lang_code = "en";
                    }
                    invoke.device_model = Build.MANUFACTURER + Build.MODEL;
                    PackageInfo pInfo = ApplicationLoader.applicationContext.getPackageManager().getPackageInfo(ApplicationLoader.applicationContext.getPackageName(), 0);
                    invoke.app_version = pInfo.versionName + " (" + pInfo.versionCode + ")";
                    invoke.system_version = "SDK " + Build.VERSION.SDK_INT;
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                    invoke.lang_code = "en";
                    invoke.device_model = "Android unknown";
                    invoke.app_version = "App version unknown";
                    invoke.system_version = "SDK " + Build.VERSION.SDK_INT;
                }
                if (invoke.lang_code == null || invoke.lang_code.length() == 0) {
                    invoke.lang_code = "en";
                }
                if (invoke.device_model == null || invoke.device_model.length() == 0) {
                    invoke.device_model = "Android unknown";
                }
                if (invoke.app_version == null || invoke.app_version.length() == 0) {
                    invoke.app_version = "App version unknown";
                }
                if (invoke.system_version == null || invoke.system_version.length() == 0) {
                    invoke.system_version = "SDK Unknown";
                }
                TLRPC.invokeWithLayer invoke2 = new TLRPC.invokeWithLayer();
                invoke2.query = invoke;
                FileLog.d("wrap in layer", "" + object);
                object = invoke2;
            }
        }
        return object;
    }

    public long performRpc(final TLObject rpc, final RPCRequest.RPCRequestDelegate completionBlock) {
        return performRpc(rpc, completionBlock, null, true, RPCRequest.RPCRequestClassGeneric, DEFAULT_DATACENTER_ID);
    }

    public long performRpc(final TLObject rpc, final RPCRequest.RPCRequestDelegate completionBlock, boolean requiresCompletion, int requestClass) {
        return performRpc(rpc, completionBlock, null, requiresCompletion, requestClass, DEFAULT_DATACENTER_ID, true);
    }

    public long performRpc(final TLObject rpc, final RPCRequest.RPCRequestDelegate completionBlock, final RPCRequest.RPCQuickAckDelegate quickAckBlock, final boolean requiresCompletion, final int requestClass, final int datacenterId) {
        return performRpc(rpc, completionBlock, quickAckBlock, requiresCompletion, requestClass, datacenterId, true);
    }

    public long performRpc(final TLObject rpc, final RPCRequest.RPCRequestDelegate completionBlock, final RPCRequest.RPCQuickAckDelegate quickAckBlock, final boolean requiresCompletion, final int requestClass, final int datacenterId, final boolean runQueue) {
        if (rpc == null || !UserConfig.isClientActivated() && (requestClass & RPCRequest.RPCRequestClassWithoutLogin) == 0) {
            FileLog.e("tmessages", "can't do request without login " + rpc);
            return 0;
        }

        final long requestToken = nextCallToken++;

        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                RPCRequest request = new RPCRequest();
                request.token = requestToken;
                request.flags = requestClass;

                request.runningDatacenterId = datacenterId;

                request.rawRequest = rpc;
                request.rpcRequest = wrapInLayer(rpc, datacenterId, request);
                request.completionBlock = completionBlock;
                request.quickAckBlock = quickAckBlock;
                request.requiresCompletion = requiresCompletion;

                requestQueue.add(request);

                if (runQueue) {
                    processRequestQueue(0, 0);
                }
            }
        });

        return requestToken;
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
                                    performRpc(dropAnswer, null, false, request.flags);
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

    private void processRequestQueue(int requestClass, int _datacenterId) {
        boolean haveNetwork = true;//isNetworkOnline();

        genericMessagesToDatacenters.clear();
        unknownDatacenterIds.clear();
        neededDatacenterIds.clear();
        unauthorizedDatacenterIds.clear();

        TcpConnection genericConnection = null;
        Datacenter defaultDatacenter = datacenterWithId(currentDatacenterId);
        if (defaultDatacenter != null) {
            genericConnection = defaultDatacenter.getGenericConnection(this);
        }

        int currentTime = (int) (System.currentTimeMillis() / 1000);
        for (int i = 0; i < runningRequests.size(); i++) {
            RPCRequest request = runningRequests.get(i);

            int datacenterId = request.runningDatacenterId;
            if (datacenterId == DEFAULT_DATACENTER_ID) {
                if (movingToDatacenterId != DEFAULT_DATACENTER_ID) {
                    continue;
                }
                datacenterId = currentDatacenterId;
            }

            if (datacenters.size() > 1 && (request.flags & RPCRequest.RPCRequestClassTryDifferentDc) != 0) {
                int requestStartTime = request.runningStartTime;
                int timeout = 30;
                if (updatingDcSettings && request.rawRequest instanceof TLRPC.TL_help_getConfig) {
                    requestStartTime = updatingDcStartTime;
                    timeout = 60;
                }
                if (requestStartTime != 0 && requestStartTime < currentTime - timeout) {
                    FileLog.e("tmessages", "move " + request.rawRequest + " to requestQueue");
                    requestQueue.add(request);
                    runningRequests.remove(i);
                    i--;
                    continue;
                }
            }

            Datacenter requestDatacenter = datacenterWithId(datacenterId);
            if (!request.initRequest && requestDatacenter.lastInitVersion != BuildVars.BUILD_VERSION) {
                request.rpcRequest = wrapInLayer(request.rawRequest, requestDatacenter.datacenterId, request);
                ByteBufferDesc os = new ByteBufferDesc(true);
                request.rpcRequest.serializeToStream(os);
                request.serializedLength = os.length();
            }

            if (requestDatacenter == null) {
                if (!unknownDatacenterIds.contains(datacenterId)) {
                    unknownDatacenterIds.add(datacenterId);
                }
                continue;
            } else if (requestDatacenter.authKey == null) {
                if (!neededDatacenterIds.contains(datacenterId)) {
                    neededDatacenterIds.add(datacenterId);
                }
                continue;
            } else if (!requestDatacenter.authorized && request.runningDatacenterId != DEFAULT_DATACENTER_ID && request.runningDatacenterId != currentDatacenterId && (request.flags & RPCRequest.RPCRequestClassEnableUnauthorized) == 0) {
                if (!unauthorizedDatacenterIds.contains(datacenterId)) {
                    unauthorizedDatacenterIds.add(datacenterId);
                }
                continue;
            }

            float maxTimeout = 8.0f;

            TcpConnection connection = null;
            if ((request.flags & RPCRequest.RPCRequestClassGeneric) != 0) {
                connection = requestDatacenter.getGenericConnection(this);
            } else if ((request.flags & RPCRequest.RPCRequestClassDownloadMedia) != 0) {
                connection = requestDatacenter.getDownloadConnection(this);
            } else if ((request.flags & RPCRequest.RPCRequestClassUploadMedia) != 0) {
                connection = requestDatacenter.getUploadConnection(this);
            }

            if ((request.flags & RPCRequest.RPCRequestClassGeneric) != 0) {
                if (connection.channelToken == 0) {
                    continue;
                }
            } else {
                if (!haveNetwork || connection.channelToken == 0) {
                    continue;
                }
                maxTimeout = 30.0f;
            }

            boolean forceThisRequest = (request.flags & requestClass) != 0 && requestDatacenter.datacenterId == _datacenterId;

            if (request.rawRequest instanceof TLRPC.TL_get_future_salts || request.rawRequest instanceof TLRPC.TL_destroy_session) {
                if (request.runningMessageId != 0) {
                    request.addRespondMessageId(request.runningMessageId);
                }
                request.runningMessageId = 0;
                request.runningMessageSeqNo = 0;
                request.transportChannelToken = 0;
                forceThisRequest = false;
            }

            if (((Math.abs(currentTime - request.runningStartTime) > maxTimeout) && (currentTime > request.runningMinStartTime || Math.abs(currentTime - request.runningMinStartTime) > 60.0)) || forceThisRequest) {
                if (!forceThisRequest && request.transportChannelToken > 0) {
                    if ((request.flags & RPCRequest.RPCRequestClassGeneric) != 0 && request.transportChannelToken == connection.channelToken) {
                        FileLog.d("tmessages", "Request token is valid, not retrying " + request.rawRequest);
                        continue;
                    } else {
                        if (connection.channelToken != 0 && request.transportChannelToken == connection.channelToken) {
                            FileLog.d("tmessages", "Request download token is valid, not retrying " + request.rawRequest);
                            continue;
                        }
                    }
                }

                if (request.transportChannelToken != 0 && request.transportChannelToken != connection.channelToken) {
                    request.lastResendTime = 0;
                }

                request.retryCount++;

                if (!request.salt && (request.flags & RPCRequest.RPCRequestClassDownloadMedia) != 0) {
                    int retryMax = 10;
                    if ((request.flags & RPCRequest.RPCRequestClassForceDownload) == 0) {
                        if (request.wait) {
                            retryMax = 1;
                        } else {
                            retryMax = 6;
                        }
                    }
                    if (request.retryCount >= retryMax) {
                        FileLog.e("tmessages", "timed out " + request.rawRequest);
                        TLRPC.TL_error error = new TLRPC.TL_error();
                        error.code = -123;
                        error.text = "RETRY_LIMIT";
                        if (request.completionBlock != null) {
                            request.completionBlock.run(null, error);
                        }
                        runningRequests.remove(i);
                        i--;
                        continue;
                    }
                }

                NetworkMessage networkMessage = new NetworkMessage();
                networkMessage.protoMessage = new TLRPC.TL_protoMessage();

                if (request.runningMessageSeqNo == 0) {
                    request.runningMessageSeqNo = connection.generateMessageSeqNo(true);
//                    request.runningMessageId = generateMessageId();
                }
                networkMessage.protoMessage.msg_id = request.runningMessageId;
                networkMessage.protoMessage.seqno = request.runningMessageSeqNo;
                networkMessage.protoMessage.bytes = request.serializedLength;
                networkMessage.protoMessage.body = request.rpcRequest;
                networkMessage.rawRequest = request.rawRequest;
                networkMessage.requestId = request.token;

                request.runningStartTime = currentTime;

                if ((request.flags & RPCRequest.RPCRequestClassGeneric) != 0) {
                    request.transportChannelToken = connection.channelToken;
                    addMessageToDatacenter(requestDatacenter.datacenterId, networkMessage);
                } else if ((request.flags & RPCRequest.RPCRequestClassDownloadMedia) != 0) {
                    request.transportChannelToken = connection.channelToken;
                    ArrayList<NetworkMessage> arr = new ArrayList<>();
                    arr.add(networkMessage);
                    proceedToSendingMessages(arr, connection, false);
                } else if ((request.flags & RPCRequest.RPCRequestClassUploadMedia) != 0) {
                    request.transportChannelToken = connection.channelToken;
                    ArrayList<NetworkMessage> arr = new ArrayList<>();
                    arr.add(networkMessage);
                    proceedToSendingMessages(arr, connection, false);
                }
            }
        }

        if (genericConnection != null && genericConnection.channelToken != 0) {
            Datacenter currentDatacenter = datacenterWithId(currentDatacenterId);

            for (Long it : sessionsToDestroy) {
                if (destroyingSessions.contains(it)) {
                    continue;
                }
                if (System.currentTimeMillis() / 1000 - lastDestroySessionRequestTime > 2.0) {
                    lastDestroySessionRequestTime = (int) (System.currentTimeMillis() / 1000);
                    TLRPC.TL_destroy_session destroySession = new TLRPC.TL_destroy_session();
                    destroySession.session_id = it;
                    destroyingSessions.add(it);

                    NetworkMessage networkMessage = new NetworkMessage();
                    networkMessage.protoMessage = wrapMessage(destroySession, currentDatacenter.connection, false);
                    if (networkMessage.protoMessage != null) {
                        addMessageToDatacenter(currentDatacenter.datacenterId, networkMessage);
                    }
                }
            }
        }

        int genericRunningRequestCount = 0;
        int uploadRunningRequestCount = 0;
        int downloadRunningRequestCount = 0;

        for (RPCRequest request : runningRequests) {
            if ((request.flags & RPCRequest.RPCRequestClassGeneric) != 0) {
                genericRunningRequestCount++;
            } else if ((request.flags & RPCRequest.RPCRequestClassUploadMedia) != 0) {
                uploadRunningRequestCount++;
            } else if ((request.flags & RPCRequest.RPCRequestClassDownloadMedia) != 0) {
                downloadRunningRequestCount++;
            }
        }

        for (int i = 0; i < requestQueue.size(); i++) {
            RPCRequest request = requestQueue.get(i);
            if (request.cancelled) {
                requestQueue.remove(i);
                i--;
                continue;
            }

            int datacenterId = request.runningDatacenterId;
            if (datacenterId == DEFAULT_DATACENTER_ID) {
                if (movingToDatacenterId != DEFAULT_DATACENTER_ID && (request.flags & RPCRequest.RPCRequestClassEnableUnauthorized) == 0) {
                    continue;
                }
                datacenterId = currentDatacenterId;
            }

            if (datacenters.size() > 1 && (request.flags & RPCRequest.RPCRequestClassTryDifferentDc) != 0) {
                int requestStartTime = request.runningStartTime;
                int timeout = 30;
                if (updatingDcSettings && request.rawRequest instanceof TLRPC.TL_help_getConfig) {
                    requestStartTime = updatingDcStartTime;
                    updatingDcStartTime = currentTime;
                    timeout = 60;
                } else {
                    request.runningStartTime = 0;
                }
                if (requestStartTime != 0 && requestStartTime < currentTime - timeout) {
                    ArrayList<Datacenter> allDc = new ArrayList<>(datacenters.values());
                    for (int a = 0; a < allDc.size(); a++) {
                        Datacenter dc = allDc.get(a);
                        if (dc.datacenterId == datacenterId) {
                            allDc.remove(a);
                            break;
                        }
                    }
                    Datacenter newDc = allDc.get(Math.abs(Utilities.random.nextInt() % allDc.size()));
                    datacenterId = newDc.datacenterId;
                    if (!(request.rawRequest instanceof TLRPC.TL_help_getConfig)) {
                        currentDatacenterId = datacenterId;
                    } else {
                        request.runningDatacenterId = datacenterId;
                    }
                }
            }

            Datacenter requestDatacenter = datacenterWithId(datacenterId);
            if (!request.initRequest && requestDatacenter.lastInitVersion != BuildVars.BUILD_VERSION) {
                request.rpcRequest = wrapInLayer(request.rawRequest, requestDatacenter.datacenterId, request);
            }

            if (requestDatacenter == null) {
                unknownDatacenterIds.add(datacenterId);
                continue;
            } else if (requestDatacenter.authKey == null) {
                neededDatacenterIds.add(datacenterId);
                continue;
            } else if (!requestDatacenter.authorized && request.runningDatacenterId != DEFAULT_DATACENTER_ID && request.runningDatacenterId != currentDatacenterId && (request.flags & RPCRequest.RPCRequestClassEnableUnauthorized) == 0) {
                unauthorizedDatacenterIds.add(datacenterId);
                continue;
            }

            TcpConnection connection = null;
            if ((request.flags & RPCRequest.RPCRequestClassGeneric) != 0) {
                connection = requestDatacenter.getGenericConnection(this);
            } else if ((request.flags & RPCRequest.RPCRequestClassDownloadMedia) != 0) {
                connection = requestDatacenter.getDownloadConnection(this);
            } else if ((request.flags & RPCRequest.RPCRequestClassUploadMedia) != 0) {
                connection = requestDatacenter.getUploadConnection(this);
            }

            if ((request.flags & RPCRequest.RPCRequestClassGeneric) != 0 && connection.channelToken == 0) {
                continue;
            }

            if (request.requiresCompletion) {
                if ((request.flags & RPCRequest.RPCRequestClassGeneric) != 0) {
                    if (genericRunningRequestCount >= 60) {
                        continue;
                    }
                    genericRunningRequestCount++;
                } else if ((request.flags & RPCRequest.RPCRequestClassUploadMedia) != 0) {
                    if (!haveNetwork || uploadRunningRequestCount >= 5) {
                        continue;
                    }
                    uploadRunningRequestCount++;
                } else if ((request.flags & RPCRequest.RPCRequestClassDownloadMedia) != 0) {
                    if (!haveNetwork || downloadRunningRequestCount >= 5) {
                        continue;
                    }
                    downloadRunningRequestCount++;
                }
            }

            long messageId = 0;

            boolean canCompress = (request.flags & RPCRequest.RPCRequestClassCanCompress) != 0;

            SerializedData os = new SerializedData(!canCompress);
            request.rpcRequest.serializeToStream(os);
            int requestLength = os.length();

            if (requestLength != 0) {
                if (canCompress) {
                    try {
                        byte[] data = Utilities.compress(os.toByteArray());
                        os.cleanup();
                        if (data.length < requestLength) {
                            TLRPC.TL_gzip_packed packed = new TLRPC.TL_gzip_packed();
                            packed.packed_data = data;
                            request.rpcRequest = packed;
                            os = new SerializedData(true);
                            packed.serializeToStream(os);
                            requestLength = os.length();
                            os.cleanup();
                        }
                    } catch (Exception e) {
                        FileLog.e("tmessages", e);
                    }
                }

                NetworkMessage networkMessage = new NetworkMessage();
                networkMessage.protoMessage = new TLRPC.TL_protoMessage();
                networkMessage.protoMessage.msg_id = messageId;
                networkMessage.protoMessage.seqno = connection.generateMessageSeqNo(true);
                networkMessage.protoMessage.bytes = requestLength;
                networkMessage.protoMessage.body = request.rpcRequest;
                networkMessage.rawRequest = request.rawRequest;
                networkMessage.requestId = request.token;

                request.runningMessageId = messageId;
                request.runningMessageSeqNo = networkMessage.protoMessage.seqno;
                request.serializedLength = requestLength;
                request.runningStartTime = (int) (System.currentTimeMillis() / 1000);
                request.transportChannelToken = connection.channelToken;
                if (request.requiresCompletion) {
                    runningRequests.add(request);
                }

                if ((request.flags & RPCRequest.RPCRequestClassGeneric) != 0) {
                    addMessageToDatacenter(requestDatacenter.datacenterId, networkMessage);
                } else {
                    ArrayList<NetworkMessage> arr = new ArrayList<>();
                    arr.add(networkMessage);
                    proceedToSendingMessages(arr, connection, false);
                }
            } else {
                FileLog.e("tmessages", "***** Couldn't serialize " + request.rawRequest);
            }

            requestQueue.remove(i);
            i--;
        }

        for (Datacenter datacenter : datacenters.values()) {
            if (genericMessagesToDatacenters.get(datacenter.datacenterId) == null && datacenter.connection != null && datacenter.connection.channelToken != 0 && datacenter.connection.hasMessagesToConfirm()) {
                genericMessagesToDatacenters.put(datacenter.datacenterId, new ArrayList<NetworkMessage>());
            }
        }

        for (int iter : genericMessagesToDatacenters.keySet()) {
            Datacenter datacenter = datacenterWithId(iter);
            if (datacenter != null) {
                boolean scannedPreviousRequests = false;
                long lastSendMessageRpcId = 0;

                boolean hasSendMessage = false;
                ArrayList<NetworkMessage> arr = genericMessagesToDatacenters.get(iter);
                for (NetworkMessage networkMessage : arr) {
                    TLRPC.TL_protoMessage message = networkMessage.protoMessage;

                    Object rawRequest = networkMessage.rawRequest;

                    if (rawRequest != null && (rawRequest instanceof TLRPC.TL_messages_sendMessage ||
                            rawRequest instanceof TLRPC.TL_messages_sendMedia ||
                            rawRequest instanceof TLRPC.TL_messages_forwardMessages ||
                            rawRequest instanceof TLRPC.TL_messages_forwardMessage ||
                            rawRequest instanceof TLRPC.TL_messages_sendEncrypted ||
                            rawRequest instanceof TLRPC.TL_messages_sendEncryptedFile ||
                            rawRequest instanceof TLRPC.TL_messages_sendEncryptedService)) {

                        if (rawRequest instanceof TLRPC.TL_messages_sendMessage) {
                            hasSendMessage = true;
                        }

                        if (!scannedPreviousRequests) {
                            scannedPreviousRequests = true;

                            ArrayList<Long> currentRequests = new ArrayList<>();
                            for (NetworkMessage currentNetworkMessage : arr) {
                                TLRPC.TL_protoMessage currentMessage = currentNetworkMessage.protoMessage;

                                Object currentRawRequest = currentNetworkMessage.rawRequest;

                                if (currentRawRequest instanceof TLRPC.TL_messages_sendMessage ||
                                        currentRawRequest instanceof TLRPC.TL_messages_sendMedia ||
                                        currentRawRequest instanceof TLRPC.TL_messages_forwardMessages ||
                                        currentRawRequest instanceof TLRPC.TL_messages_forwardMessage ||
                                        currentRawRequest instanceof TLRPC.TL_messages_sendEncrypted ||
                                        currentRawRequest instanceof TLRPC.TL_messages_sendEncryptedFile ||
                                        currentRawRequest instanceof TLRPC.TL_messages_sendEncryptedService) {
                                    currentRequests.add(currentMessage.msg_id);
                                }
                            }

                            long maxRequestId = 0;
                            for (RPCRequest request : runningRequests) {
                                if (request.rawRequest instanceof TLRPC.TL_messages_sendMessage ||
                                        request.rawRequest instanceof TLRPC.TL_messages_sendMedia ||
                                        request.rawRequest instanceof TLRPC.TL_messages_forwardMessages ||
                                        request.rawRequest instanceof TLRPC.TL_messages_forwardMessage ||
                                        request.rawRequest instanceof TLRPC.TL_messages_sendEncrypted ||
                                        request.rawRequest instanceof TLRPC.TL_messages_sendEncryptedFile ||
                                        request.rawRequest instanceof TLRPC.TL_messages_sendEncryptedService) {
                                    if (!currentRequests.contains(request.runningMessageId)) {
                                        maxRequestId = Math.max(maxRequestId, request.runningMessageId);
                                    }
                                }
                            }

                            lastSendMessageRpcId = maxRequestId;
                        }

                        if (lastSendMessageRpcId != 0 && lastSendMessageRpcId != message.msg_id) {
                            TLRPC.TL_invokeAfterMsg invokeAfterMsg = new TLRPC.TL_invokeAfterMsg();
                            invokeAfterMsg.msg_id = lastSendMessageRpcId;
                            invokeAfterMsg.query = message.body;

                            message.body = invokeAfterMsg;
                            message.bytes = message.bytes + 4 + 8;
                        }

                        lastSendMessageRpcId = message.msg_id;
                    }
                }

                proceedToSendingMessages(arr, datacenter.getGenericConnection(this), hasSendMessage);
            }
        }

        if ((requestClass & RPCRequest.RPCRequestClassGeneric) != 0) {
            ArrayList<NetworkMessage> messagesIt = genericMessagesToDatacenters.get(_datacenterId);
            if (messagesIt == null || messagesIt.size() == 0) {
//                generatePing();
            }
        }

        if (!unknownDatacenterIds.isEmpty() && !updatingDcSettings) {
        }

        for (int num : neededDatacenterIds) {
            if (num != movingToDatacenterId) {
                boolean notFound = true;
                for (Action actor : actionQueue) {
                    if (actor instanceof HandshakeAction) {
                        HandshakeAction eactor = (HandshakeAction) actor;
                        if (eactor.datacenter.datacenterId == num) {
                            notFound = false;
                            break;
                        }
                    }
                }
                if (notFound) {
                    HandshakeAction actor = new HandshakeAction(datacenterWithId(num));
                    actor.delegate = this;
                    dequeueActor(actor, true);
                }
            }
        }

        for (int num : unauthorizedDatacenterIds) {
            if (num != currentDatacenterId && num != movingToDatacenterId && UserConfig.isClientActivated()) {
                boolean notFound = true;
                for (Action actor : actionQueue) {

                }
                if (notFound) {

                }
            }
        }
    }

    void addMessageToDatacenter(int datacenterId, NetworkMessage message) {
        ArrayList<NetworkMessage> arr = genericMessagesToDatacenters.get(datacenterId);
        if (arr == null) {
            arr = new ArrayList<>();
            genericMessagesToDatacenters.put(datacenterId, arr);
        }
        arr.add(message);
    }

    TLRPC.TL_protoMessage wrapMessage(TLObject message, TcpConnection connection, boolean meaningful) {
        ByteBufferDesc os = new ByteBufferDesc(true);
        message.serializeToStream(os);

        if (os.length() != 0) {
            TLRPC.TL_protoMessage protoMessage = new TLRPC.TL_protoMessage();
//            protoMessage.msg_id = generateMessageId();
            protoMessage.bytes = os.length();
            protoMessage.body = message;
            protoMessage.seqno = connection.generateMessageSeqNo(meaningful);
            return protoMessage;
        } else {
            FileLog.e("tmessages", "***** Couldn't serialize " + message);
            return null;
        }
    }

    void proceedToSendingMessages(ArrayList<NetworkMessage> messageList, TcpConnection connection, boolean reportAck) {
        if (connection.getSissionId() == 0) {
            return;
        }

        ArrayList<NetworkMessage> messages = new ArrayList<>();
        if (messageList != null) {
            messages.addAll(messageList);
        }

        NetworkMessage message = null;
        if (message != null) {
            messages.add(message);
        }

//        sendMessagesToTransport(messages, connection, reportAck);
    }




    void refillSaltSet(final Datacenter datacenter) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                for (RPCRequest request : requestQueue) {
                    if (request.rawRequest instanceof TLRPC.TL_get_future_salts) {
                        Datacenter requestDatacenter = datacenterWithId(request.runningDatacenterId);
                        if (requestDatacenter.datacenterId == datacenter.datacenterId) {
                            return;
                        }
                    }
                }

                for (RPCRequest request : runningRequests) {
                    if (request.rawRequest instanceof TLRPC.TL_get_future_salts) {
                        Datacenter requestDatacenter = datacenterWithId(request.runningDatacenterId);
                        if (requestDatacenter.datacenterId == datacenter.datacenterId) {
                            return;
                        }
                    }
                }

                TLRPC.TL_get_future_salts getFutureSalts = new TLRPC.TL_get_future_salts();
                getFutureSalts.num = 32;

                performRpc(getFutureSalts, new RPCRequest.RPCRequestDelegate() {
                    @Override
                    public void run(TLObject response, TLRPC.TL_error error) {
                        TLRPC.TL_futuresalts res = (TLRPC.TL_futuresalts) response;
                        if (error == null) {
                            int currentTime = getCurrentTime();
                            datacenter.mergeServerSalts(currentTime, res.salts);
                            saveSession();
                        }
                    }
                }, null, true, RPCRequest.RPCRequestClassGeneric | RPCRequest.RPCRequestClassWithoutLogin, datacenter.datacenterId);
            }
        });
    }

    void messagesConfirmed(final long requestMsgId) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                for (RPCRequest request : runningRequests) {
                    if (requestMsgId == request.runningMessageId) {
                        request.confirmed = true;
                    }
                }
            }
        });
    }

    private void rpcCompleted(final long requestMsgId) {
        Utilities.stageQueue.postRunnable(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < runningRequests.size(); i++) {
                    RPCRequest request = runningRequests.get(i);
                    removeRequestInClass(request.token);
                    if (request.respondsToMessageId(requestMsgId)) {
                        request.rawRequest.freeResources();
                        request.rpcRequest.freeResources();
                        runningRequests.remove(i);
                        i--;
                    }
                }
            }
        });
    }






    //================================================================================
    // TCPConnection delegate
    //================================================================================

    @Override
    public void tcpConnectionClosed(TcpConnection connection) {
        if (connection.getDatacenterId() == currentDatacenterId && (connection.transportRequestClass & RPCRequest.RPCRequestClassGeneric) != 0) {
            if (isNetworkOnline()) {
                connectionState = 2;
            } else {
                connectionState = 1;
            }
            if (BuildVars.DEBUG_VERSION) {
                try {
                    ConnectivityManager cm = (ConnectivityManager) ApplicationLoader.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo[] networkInfos = cm.getAllNetworkInfo();
                    for (int a = 0; a < 2; a++) {
                        if (a >= networkInfos.length) {
                            break;
                        }
                        NetworkInfo info = networkInfos[a];
                        FileLog.e("tmessages", "Network: " + info.getTypeName() + " status: " + info.getState() + " info: " + info.getExtraInfo() + " object: " + info.getDetailedState() + " other: " + info);
                    }
                    if (networkInfos.length == 0) {
                        FileLog.e("tmessages", "no network available");
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", "NETWORK STATE GET ERROR", e);
                }
            }
            final int stateCopy = connectionState;
            AndroidUtilities.runOnUIThread(new Runnable() {
                @Override
                public void run() {
                    NotificationCenter.getInstance().postNotificationName(NotificationCenter.didUpdatedConnectionState, stateCopy);
                }
            });
        } else if ((connection.transportRequestClass & RPCRequest.RPCRequestClassPush) != 0) {
            FileLog.e("tmessages", "push connection closed");
            if (BuildVars.DEBUG_VERSION) {
                try {
                    ConnectivityManager cm = (ConnectivityManager) ApplicationLoader.applicationContext.getSystemService(Context.CONNECTIVITY_SERVICE);
                    NetworkInfo[] networkInfos = cm.getAllNetworkInfo();
                    for (int a = 0; a < 2; a++) {
                        if (a >= networkInfos.length) {
                            break;
                        }
                        NetworkInfo info = networkInfos[a];
                        FileLog.e("tmessages", "Network: " + info.getTypeName() + " status: " + info.getState() + " info: " + info.getExtraInfo() + " object: " + info.getDetailedState() + " other: " + info);
                    }
                    if (networkInfos.length == 0) {
                        FileLog.e("tmessages", "no network available");
                    }
                } catch (Exception e) {
                    FileLog.e("tmessages", "NETWORK STATE GET ERROR", e);
                }
            }
            sendingPushPing = false;
            lastPushPingTime = System.currentTimeMillis() - 60000 * 3 + 4000;
        }
    }

    @Override
    public void tcpConnectionConnected(TcpConnection connection) {
        Datacenter datacenter = datacenterWithId(connection.getDatacenterId());
        if (datacenter.authKey != null) {
            if ((connection.transportRequestClass & RPCRequest.RPCRequestClassPush) != 0) {
                sendingPushPing = false;
                //lastPushPingTime = System.currentTimeMillis() - 60000 * 3 + 4000; //TODO check this
                //FileLog.e("tmessages", "schedule push ping in 4 seconds");
                lastPushPingTime = System.currentTimeMillis();
//                generatePing(datacenter, true);
            } else {
                if (paused && lastPauseTime != 0) {
                    lastPauseTime = System.currentTimeMillis();
                    nextSleepTimeout = 30000;
                }
                processRequestQueue(connection.transportRequestClass, connection.getDatacenterId());
            }
        }
    }

    @Override
    public void tcpConnectionQuiackAckReceived(TcpConnection connection, int ack) {
        ArrayList<Long> arr = quickAckIdToRequestIds.get(ack);
        if (arr != null) {
            for (RPCRequest request : runningRequests) {
                if (arr.contains(request.token)) {
                    if (request.quickAckBlock != null) {
                        request.quickAckBlock.quickAck();
                    }
                }
            }
            quickAckIdToRequestIds.remove(ack);
        }
    }

    private void finishUpdatingState(TcpConnection connection) {
        if (connection.getDatacenterId() == currentDatacenterId && (connection.transportRequestClass & RPCRequest.RPCRequestClassGeneric) != 0) {
            if (ConnectionsManager.getInstance().connectionState == 3) {
                ConnectionsManager.getInstance().connectionState = 0;
                final int stateCopy = ConnectionsManager.getInstance().connectionState;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didUpdatedConnectionState, stateCopy);
                    }
                });
            }
        }
    }

    @Override
    public void tcpConnectionReceivedData(TcpConnection connection, ByteBufferDesc data, int length) {
        if (connection.getDatacenterId() == currentDatacenterId && (connection.transportRequestClass & RPCRequest.RPCRequestClassGeneric) != 0) {
            if (connectionState == 1 || connectionState == 2) {
                connectionState = 3;
                final int stateCopy = connectionState;
                AndroidUtilities.runOnUIThread(new Runnable() {
                    @Override
                    public void run() {
                        NotificationCenter.getInstance().postNotificationName(NotificationCenter.didUpdatedConnectionState, stateCopy);
                    }
                });
            }
        }
        if (length == 4) {
            int error = data.readInt32(false);
            FileLog.e("tmessages", "mtproto error = " + error);
            connection.suspendConnection(true);
            connection.connect();
            return;
        }
        Datacenter datacenter = datacenterWithId(connection.getDatacenterId());

        long keyId = data.readInt64(false);
        if (keyId == 0) {
            long messageId = data.readInt64(false);
            if (connection.isMessageIdProcessed(messageId)) {
                finishUpdatingState(connection);
                return;
            }

            int messageLength = data.readInt32(false);

            TLObject message = null;

            if (message != null) {
//                processMessage(message, messageId, 0, 0, connection, 0, 0);
                connection.addProcessedMessageId(messageId);
            }
        } else {
            if (datacenter.authKeyId == 0 || keyId != datacenter.authKeyId) {
                FileLog.e("tmessages", "Error: invalid auth key id " + connection);
                datacenter.switchTo443Port();
                connection.suspendConnection(true);
                connection.connect();
                return;
            }

            byte[] messageKey = data.readData(16, false);
            MessageKeyData keyData = MessageKeyData.generateMessageKeyData(datacenter.authKey, messageKey, true);

            Utilities.aesIgeEncryption(data.buffer, keyData.aesKey, keyData.aesIv, false, false, data.position(), length - 24);

            long messageServerSalt = data.readInt64(false);
            long messageSessionId = data.readInt64(false);

            if (messageSessionId != connection.getSissionId()) {
                FileLog.e("tmessages", String.format("***** Error: invalid message session ID (%d instead of %d)", messageSessionId, connection.getSissionId()));
                finishUpdatingState(connection);
                return;
            }

            boolean doNotProcess = false;

            long messageId = data.readInt64(false);
            int messageSeqNo = data.readInt32(false);
            int messageLength = data.readInt32(false);

            if (connection.isMessageIdProcessed(messageId)) {
                doNotProcess = true;
            }

            if (messageSeqNo % 2 != 0) {
                connection.addMessageToConfirm(messageId);
            }

            byte[] realMessageKeyFull = Utilities.computeSHA1(data.buffer, 24, Math.min(messageLength + 32 + 24, data.limit()));
            if (realMessageKeyFull == null) {
                return;
            }

            if (!Utilities.arraysEquals(messageKey, 0, realMessageKeyFull, realMessageKeyFull.length - 16)) {
                FileLog.e("tmessages", "***** Error: invalid message key");
                datacenter.switchTo443Port();
                connection.suspendConnection(true);
                connection.connect();
                return;
            }

            if (!doNotProcess) {
                TLObject message = null;
                if (message != null) {
                    FileLog.d("tmessages", "received object " + message);
//                    processMessage(message, messageId, messageSeqNo, messageServerSalt, connection, 0, 0);
                    connection.addProcessedMessageId(messageId);

                    if ((connection.transportRequestClass & RPCRequest.RPCRequestClassPush) != 0) {
                        ArrayList<NetworkMessage> messages = new ArrayList<>();
//                        NetworkMessage networkMessage = connection.generateConfirmationRequest();
//                        if (networkMessage != null) {
//                            messages.add(networkMessage);
//                        }
//                        sendMessagesToTransport(messages, connection, false);
                    }
                }
            } else {
                proceedToSendingMessages(null, connection, false);
            }
            finishUpdatingState(connection);
        }
    }




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
        if (action instanceof HandshakeAction) {
            HandshakeAction eactor = (HandshakeAction) action;
            eactor.datacenter.connection.delegate = this;
            saveSession();

            if (eactor.datacenter.datacenterId == currentDatacenterId || eactor.datacenter.datacenterId == movingToDatacenterId) {
                timeDifference = (Integer) params.get("timeDifference");
                eactor.datacenter.recreateSessions();

                clearRequestsForRequestClass(RPCRequest.RPCRequestClassGeneric, eactor.datacenter);
                clearRequestsForRequestClass(RPCRequest.RPCRequestClassDownloadMedia, eactor.datacenter);
                clearRequestsForRequestClass(RPCRequest.RPCRequestClassUploadMedia, eactor.datacenter);
            }
            processRequestQueue(RPCRequest.RPCRequestClassTransportMask, eactor.datacenter.datacenterId);
        }
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
