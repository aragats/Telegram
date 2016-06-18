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
3. Remove and re develop send exception info. When Exception appears, apps ask to send info to developers.


TODO:
1. JNI exception !! load some native methods.  native method com.facebook.breakpad.BreakpadManager.getMinidumpFlags()
2. Localize and rename string value. Noch keine Nachrichten... replace with something else.
3. Exception in PhotoPicker when I click  on ...
4. Add task into bitbucket!!
5. ContactsController.checkAppAccount UserConfig.loadUser ... and other - do not invoke them. ??
6. Change drawer left sidebar size of header
7. Fragen und Antworten - replace the link or create a page with antworten. Landing Page.
8. Loading container error. If I got the empy result ??


TOP libraries for Android
https://infinum.co/the-capsized-eight/articles/top-5-android-libraries-every-android-developer-should-know-about



VK URL
https://api.vk.com/method/photos.search?lat=52.400608&long=13.011822&count=100&radius=5000&v=5.44
https://api.vk.com/method/newsfeed.search?lat=52.400608&lng=13.011822&count=100

#0088CC - color of telegram text and color of my icon. 
#607d8b - color of launcher icon
#5A5C67 - color for grey icon in Action Bar.
http://stackoverflow.com/questions/26899820/android-5-0-how-to-change-recent-apps-title-color


Instance Run Settings.
http://stackoverflow.com/questions/20915266/error-type-3-error-activity-class-does-not-exist