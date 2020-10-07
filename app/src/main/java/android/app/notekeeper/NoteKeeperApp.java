package android.app.notekeeper;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

public class NoteKeeperApp extends Application {

    public static final String CHANNEL_ID = "NoteKeeperReminder";

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
    }

    private void createNotificationChannel() {
        //Check if API level is greater or equal to 26
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //create notification channel
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID, getString(R.string.channel_name), NotificationManager.IMPORTANCE_DEFAULT);

            //set channel description
            channel.setDescription(getString(R.string.channel_description));

            //register the channel with the system
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }
}
