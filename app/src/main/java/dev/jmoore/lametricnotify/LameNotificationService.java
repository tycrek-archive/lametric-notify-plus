package dev.jmoore.lametricnotify;

import android.content.Intent;
import android.content.SharedPreferences;
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
        String notificationPackage = sbn.getPackageName();
        String nTitle = sbn.getNotification().extras.getString("android.title");
        String nText = sbn.getNotification().extras.getString("android.text");

        // Get the current encoded data and timestamp for duplicate prevention
        String currentContent = Base64.encodeToString((nTitle + ":" + nText).getBytes(), Base64.NO_WRAP);
        Long currentTimestamp = sbn.getPostTime();

        // 1. If both are null, they haven't been set (this is the first notification the service gets after starting)
        // 2. New content and Timestamps have a difference of at least 1000 milliseconds
        if ((previousContent == null || previousTimestamp == null) || (!currentContent.equals(previousContent) && (currentTimestamp - previousTimestamp > DUPLICATE_THRESHOLD))) {
            previousContent = currentContent;
            previousTimestamp = currentTimestamp;

            SharedPreferences settings = getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE);
            String address = settings.getString("address", "IP Address");
            String api = settings.getString("api", "Device API Key");

            try {
                if (settings.getBoolean(notificationPackage, false)) {
                    String icon = getIcon(notificationPackage);
                    Lametric lametric = new Lametric(getApplicationContext(), address, api);
                    lametric.sendNotification(icon, nText == null ? nTitle : nText);
                }
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {}

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
}
