package dev.jmoore.lametricnotify;

import android.content.Intent;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.widget.EditText;

public class LameNotificationService extends NotificationListenerService {
    @Override
    public IBinder onBind(Intent intent) {
        return super.onBind(intent);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        String notificationPackage = sbn.getPackageName();
        EditText txtAddress = (EditText) findViewById(R.id.textAddress);
        EditText txtApi = (EditText) findViewById(R.id.textApi);
        try {
            int icon = 11252;
            Lametric lametric = new Lametric(getBaseContext(), txtAddress.getText().toString(), txtApi.getText().toString());
            lametric.sendNotification(icon, sbn.);
        } catch(Exception ex) {
            e.printStackTrace();
        }

    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn){
        // Implement what you want here
    }
}
