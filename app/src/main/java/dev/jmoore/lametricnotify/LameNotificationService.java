package dev.jmoore.lametricnotify;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.Base64;

public class LameNotificationService extends NotificationListenerService {

    // For preventing duplicate events
    private static final int DUPLICATE_THRESHOLD = 1000;
    private String previousContent;
    private Long previousTimestamp;

    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        // Get information about the notification. n in nFoo is "notification"
        String notificationPackage = sbn.getPackageName();
        String nTitle = sbn.getNotification().extras.getString(MainActivity.NOTIFICATION_EXTRA_TITLE);
        String nText = MainActivity.STRING_EMPTY;

        // LaMetric uses "frames" to show multiple slides of data
        String[] nFrames;

        // Wrap in a try/catch as some notifications don't properly use android.text
        try {
            // Some apps (like Signal) don't return a proper String so we cannot use .getString()
            //noinspection ConstantConditions
            nText = sbn.getNotification().extras.get(MainActivity.NOTIFICATION_EXTRA_TEXT).toString();
            nFrames = new String[]{nTitle, nText};
        } catch (NullPointerException ex) {
            nFrames = new String[]{nTitle};
        }

        // Get the current encoded data and timestamp for duplicate prevention
        String currentContent = Base64.encodeToString((nTitle + ":" + nText).getBytes(), Base64.NO_WRAP);
        Long currentTimestamp = sbn.getPostTime();

        // 1. If both are null, they haven't been set (this is the first notification the service gets after starting)
        // 2. New content and Timestamps have a difference of at least 1000 milliseconds
        if ((previousContent == null || previousTimestamp == null) || (!currentContent.equals(previousContent) && (currentTimestamp - previousTimestamp > DUPLICATE_THRESHOLD))) {
            previousContent = currentContent;
            previousTimestamp = currentTimestamp;

            try {
                SharedPreferences settings = getApplicationContext().getSharedPreferences(MainActivity.SHARED_PREFERENCES_NAME, MODE_PRIVATE);
                if (settings.getBoolean(notificationPackage, false) && getSsid().equals(settings.getString(MainActivity.SHARED_PREFERENCES_KEY_SSID, MainActivity.STRING_EMPTY))) {
                    Lametric lametric = new Lametric(getApplicationContext(),
                            settings.getString(MainActivity.SHARED_PREFERENCES_KEY_ADDRESS, MainActivity.SHARED_PREFERENCES_DEFAULT_ADDRESS),
                            settings.getString(MainActivity.SHARED_PREFERENCES_KEY_API, MainActivity.SHARED_PREFERENCES_DEFAULT_API));
                    lametric.sendNotification(getIcon(notificationPackage), nFrames);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    private String getIcon(String app) {
        String defIcon = "38031";

        switch (app) {
            case "com.instagram.android":
                return getResources().getString(R.string.icon_instagram);
            case "com.facebook.katana":
                return getResources().getString(R.string.icon_facebook);
            case "com.snapchat.android":
                return getResources().getString(R.string.icon_snapchat);
            case "com.whatsapp":
                return getResources().getString(R.string.icon_whatsapp);
            case "com.google.android.gm":
                return getResources().getString(R.string.icon_gmail);
            case "org.thoughtcrime.securesms":
                return getResources().getString(R.string.icon_signal);
            case "com.linkedin.android":
                return getResources().getString(R.string.icon_linkedin);
            case "com.facebook.orca":
                return getResources().getString(R.string.icon_messenger);
            case "com.discord":
                return getResources().getString(R.string.icon_discord);
            case "com.reddit.frontpage":
                return getResources().getString(R.string.icon_reddit);
            case "com.twitter.android":
                return getResources().getString(R.string.icon_twitter);
            case "com.google.android.youtube":
                return getResources().getString(R.string.icon_youtube);
            case "tv.twitch.android.app":
                return getResources().getString(R.string.icon_twitch);
            case "com.zhiliaoapp.musically":
                return getResources().getString(R.string.icon_tiktok);
            case "com.paypal.android.p2pmobile":
                return getResources().getString(R.string.icon_paypal);
            case "com.valvesoftware.android.steam.community":
                return getResources().getString(R.string.icon_steam);
            case "com.github.android":
                return getResources().getString(R.string.icon_github);
            default:
                return defIcon;
        }
    }

    private String getSsid() {
        WifiManager wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        WifiInfo wifiInfo;
        wifiInfo = wifiManager.getConnectionInfo();

        return wifiInfo.getSupplicantState() == SupplicantState.COMPLETED ? wifiInfo.getSSID() : MainActivity.STRING_EMPTY;
    }
}
