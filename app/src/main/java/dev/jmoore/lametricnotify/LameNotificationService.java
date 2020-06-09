package dev.jmoore.lametricnotify;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LameNotificationService extends NotificationListenerService {
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        String notificationPackage = sbn.getPackageName();

        SharedPreferences settings = getApplicationContext().getSharedPreferences("settings", MODE_PRIVATE);
        String address = settings.getString("address", "IP Address");
        String api = settings.getString("api", "Device API Key");

        try {
            if (settings.getBoolean(notificationPackage, false)) {
                Toast.makeText(getApplicationContext(), String.valueOf(settings.getBoolean(notificationPackage, false)), Toast.LENGTH_SHORT).show();
                int icon = 11252;
                Lametric lametric = new Lametric(getApplicationContext(), address, api);
                lametric.sendNotification(icon, sbn.getNotification().extras.getString("android.title"));
            }
        } catch(Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        // Implement what you want here
    }
}
