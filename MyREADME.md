OLD DOCUMENTS!!

Replaced
org/telegram/messenger  - > ru.aragats.messenger !!! NO
org_telegram_messenger
org.telegram.messenger

org/telegram
org_telegram
org.telegram


LOCAL_MODULE := tmessages.4 //  in Android.mk change

Classes with native method must save package structure. !!!!  because the use system native methods and they are in *so files for each system



Emoji.replaceEmoji - replace code to smiles
Some system method for managing session and users in MessagesController: logout, register push and son on.
ContactsController.formatName(user.first_name, user.last_name);
BaseFragment.onFragmentCreate() - method check whether we cn run activity before to open (create) it. Look at usage of this method in ActionBarLayout.
Semaphore - learn it. Usage in ChartActivity in the method onFragmentCreate().
Send notification about Crash!!!
MediaController.wifiDownloadMask
MediaController.canDownloadMedia()

create view by layout
LayoutInflater li = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
view = li.inflate(ru.aragats.whats.R.layout.chat_unread_layout, viewGroup, false);

BackupImageView - with AvatarDrawable,. Check PostCreateChatActivity


NEW

1.
In AndroidManifest.xml (debug/release) I replaced google MAP Api ID to my ID.
<!--//TODO-CONFIG-->
        <!--<meta-data android:name="com.google.android.maps.v2.API_KEY" android:value="AIzaSyA-t0jLPjUt2FxrA8VPK2EiYHcYcboIR6k" />-->
        <meta-data android:name="com.google.android.maps.v2.API_KEY" android:value="AIzaSyD5u0N9-ItAsw_RVm2xiiePSJeeoXtt-LQ" />

2. TODO CONFIG - mean config which I adjusted.

3. BuildVars.java
//TODO-CONFIG: APP_ID, APP_HASH, FOURSQUARE_API_KEY, FOURSQUARE_API_VERSION, HOCKEY_APP_ID

4. Create jsk (keystore) files.
gradle.properties
RELEASE_STORE_PASSWORD=password
RELEASE_KEY_ALIAS=alias
RELEASE_KEY_PASSWORD=password

5. LaunchActivity and IntroActivity
comment these lines
//TODO-CONFIG HOCKEY_APP_HASH
AndroidUtilities.checkForCrashes(this);
AndroidUtilities.checkForUpdates(this);
if there is not correct HOCKEY_APP_ID



TODO before release:
Remove all telegram reference and configs. Image folder, Video and so on.!!!
Rename packages and app ids - Not to interfere with Telegram App


Notes:

1. LaunchActivity.handleIntent for accept sharing files (photo). Different types and actions. Look at original code.
Temporally delete some function from the method. and <intent-filter> action.view  SEND_MULTIPLE in AndroidManifest.xml
2. //TODO-mock  or TODO-temp. temp mock which I should redesign.