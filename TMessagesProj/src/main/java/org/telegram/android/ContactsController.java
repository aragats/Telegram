/*
 * This is the source code of Telegram for Android v. 1.3.x.
 * It is licensed under GNU GPL v. 2 or later.
 * You should have received a copy of the license in this archive (see LICENSE).
 *
 * Copyright Nikolai Kudashov, 2013.
 */

package org.telegram.android;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.SharedPreferences;

import ru.aragats.wgo.ApplicationLoader;
import ru.aragats.wgo.R;

import org.telegram.messenger.FileLog;
import org.telegram.messenger.UserConfig;

import java.util.HashMap;

public class ContactsController {

    private Account currentAccount;
    private String inviteText;
    private boolean updatingInviteText = false;
    private HashMap<String, String> sectionsToReplace = new HashMap<>();


    private static volatile ContactsController Instance = null;
    public static ContactsController getInstance() {
        ContactsController localInstance = Instance;
        if (localInstance == null) {
            synchronized (ContactsController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new ContactsController();
                }
            }
        }
        return localInstance;
    }

    public ContactsController() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        if (preferences.getBoolean("needGetStatuses", false)) {
//            reloadContactsStatuses();
        }

        sectionsToReplace.put("À", "A");
        sectionsToReplace.put("Á", "A");
        sectionsToReplace.put("Ä", "A");
        sectionsToReplace.put("Ù", "U");
        sectionsToReplace.put("Ú", "U");
        sectionsToReplace.put("Ü", "U");
        sectionsToReplace.put("Ì", "I");
        sectionsToReplace.put("Í", "I");
        sectionsToReplace.put("Ï", "I");
        sectionsToReplace.put("È", "E");
        sectionsToReplace.put("É", "E");
        sectionsToReplace.put("Ê", "E");
        sectionsToReplace.put("Ë", "E");
        sectionsToReplace.put("Ò", "O");
        sectionsToReplace.put("Ó", "O");
        sectionsToReplace.put("Ö", "O");
        sectionsToReplace.put("Ç", "C");
        sectionsToReplace.put("Ñ", "N");
        sectionsToReplace.put("Ÿ", "Y");
        sectionsToReplace.put("Ý", "Y");
        sectionsToReplace.put("Ţ", "Y");
    }

    public void cleanup() {

    }

    public void checkInviteText() {
        SharedPreferences preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
        inviteText = preferences.getString("invitetext", null);
        int time = preferences.getInt("invitetexttime", 0);
        if (!updatingInviteText && (inviteText == null || time + 86400 < (int)(System.currentTimeMillis() / 1000))) {
            updatingInviteText = true;

            //Load from the server invite message for lang async and save to settings
            String inviteMessage = "Check Whats going on?";

            updatingInviteText = false;
            preferences = ApplicationLoader.applicationContext.getSharedPreferences("mainconfig", Activity.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString("invitetext", inviteMessage);
            editor.putInt("invitetexttime", (int) (System.currentTimeMillis() / 1000));
            editor.commit();

        }
    }

    public String getInviteText() {
        return inviteText != null ? inviteText : LocaleController.getString("InviteText", R.string.InviteText);
    }

    //TODO check accounts receiving from AccountManager
    public void checkAppAccount() {
        AccountManager am = AccountManager.get(ApplicationLoader.applicationContext);
        Account[] accounts;
        try {
            accounts = am.getAccountsByType("org.telegram.account");
            if (accounts != null && accounts.length > 0) {
                for (Account c : accounts) {
                    am.removeAccount(c, null, null);
                }
            }
        } catch (Exception e) {
            FileLog.e("tmessages", e);
        }

        accounts = am.getAccountsByType("ru.aragats.wgo");
        boolean recreateAccount = false;
        if (UserConfig.isClientActivated()) {
            if (accounts.length == 1) {
                Account acc = accounts[0];
                if (!acc.name.equals(UserConfig.getCurrentUser().getPhone())) {
                    recreateAccount = true;
                } else {
                    currentAccount = acc;
                }
            } else {
                recreateAccount = true;
            }
        } else {
            if (accounts.length > 0) {
                recreateAccount = true;
            }
        }
        if (recreateAccount) {
            for (Account c : accounts) {
                am.removeAccount(c, null, null);
            }
            if (UserConfig.isClientActivated()) {
                try {
                    //TODO UserConfig.getCurrentUser() is null. Deshalb Method wirft Exception.
                    currentAccount = new Account(UserConfig.getCurrentUser().getPhone(), "ru.aragats.wgo");
                    am.addAccountExplicitly(currentAccount, "", null);
                } catch (Exception e) {
                    FileLog.e("tmessages", e);
                }
            }
        }
    }

    public static String formatName(String firstName, String lastName) {
        /*if ((firstName == null || firstName.length() == 0) && (lastName == null || lastName.length() == 0)) {
            return LocaleController.getString("HiddenName", R.string.HiddenName);
        }*/
        if (firstName != null) {
            firstName = firstName.trim();
        }
        if (lastName != null) {
            lastName = lastName.trim();
        }
        StringBuilder result = new StringBuilder((firstName != null ? firstName.length() : 0) + (lastName != null ? lastName.length() : 0) + 1);
        if (LocaleController.nameDisplayOrder == 1) {
            if (firstName != null && firstName.length() > 0) {
                result.append(firstName);
                if (lastName != null && lastName.length() > 0) {
                    result.append(" ");
                    result.append(lastName);
                }
            } else if (lastName != null && lastName.length() > 0) {
                result.append(lastName);
            }
        } else {
            if (lastName != null && lastName.length() > 0) {
                result.append(lastName);
                if (firstName != null && firstName.length() > 0) {
                    result.append(" ");
                    result.append(firstName);
                }
            } else if (firstName != null && firstName.length() > 0) {
                result.append(firstName);
            }
        }
        return result.toString();
    }
}
