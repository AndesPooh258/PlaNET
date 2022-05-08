package edu.cuhk.csci3310.planet.reminder;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import edu.cuhk.csci3310.planet.util.NotificationUtils;

/**
 * ReminderBroadcast class to display reminder.
 */
public class ReminderBroadcast extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // get intent
        String title = (String) intent.getExtras().get(NotificationUtils.NAME_MESSAGE);
        String reminder_time = (String) intent.getExtras().get(NotificationUtils.TIME_MESSAGE);
        // build and display notification
        NotificationUtils notificationUtils = new NotificationUtils(context);
        NotificationCompat.Builder builder = notificationUtils.setNotification(
                "Deadline approaching", title + " dues in " + reminder_time);
        notificationUtils.getManager().notify(42, builder.build());
    }
}
