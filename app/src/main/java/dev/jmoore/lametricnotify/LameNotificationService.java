package dev.jmoore.lametricnotify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class LameNotificationService extends NotificationListenerService {
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        String notificationPackage = sbn.getPackageName();

        SharedPreferences settings = getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE);
        String address = settings.getString("address", "IP Address");
        String api = settings.getString("api", "Device API Key");

        try {
            if (settings.getBoolean(notificationPackage, false)) {
                String icon = getIcon(notificationPackage);

                String nTitle = sbn.getNotification().extras.getString("android.title");
                String nText = sbn.getNotification().extras.getString("android.text");
                Lametric lametric = new Lametric(getApplicationContext(), address, api);
                lametric.sendNotification(icon, nText == null ? nTitle : nText);
            }
        } catch(Exception ex) {
            ex.printStackTrace();
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
