package edu.cuhk.csci3310.planet.util;

import static android.app.PendingIntent.FLAG_IMMUTABLE;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

import com.google.firebase.firestore.FirebaseFirestore;

import edu.cuhk.csci3310.planet.R;
import edu.cuhk.csci3310.planet.model.Work;
import edu.cuhk.csci3310.planet.reminder.ReminderBroadcast;

/**
 * Notification-related Utilities.
 * Modified from https://stackoverflow.com/questions/34517520/
 * how-to-give-notifications-on-android-on-specific-time
 */
public class NotificationUtils extends ContextWrapper {

    public static final String NAME_MESSAGE = "edu.cuhk.csci3310.planet.name.MESSAGE";
    public static final String TIME_MESSAGE = "edu.cuhk.csci3310.planet.time.MESSAGE";
    private final String CHANNEL_ID = "notification channel";
    private final String CHANNEL_NAME = "to-do notification";
    private final Context context;
    private NotificationManager notificationManager;

    public NotificationUtils(Context context) {
        super(context);
        this.context = context;
        createChannel();
    }

    public NotificationCompat.Builder setNotification(String title, String body) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.image_default)
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
    }

    private void createChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_DEFAULT);
        channel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
        if(notificationManager == null) {
            notificationManager = (NotificationManager)
                    getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return notificationManager;
    }

    /**
     * Set a reminder based on the name, reminder time, and display time
     */
    public void setReminder(int id, String name, String reminder_time, long timeInMillis) {
        Intent intent = new Intent(context, ReminderBroadcast.class);
        intent.putExtra(NAME_MESSAGE, name);
        intent.putExtra(TIME_MESSAGE, reminder_time);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(
                context, id, intent, FLAG_IMMUTABLE);
        AlarmManager alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);
        alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pendingIntent);
    }

    /**
     * Set reminders based on the to-do tasks in Firestore
     */
    public static void reminderNotification(Context context, FirebaseFirestore mFirestore,
                                            String email, int reminder_time) {
        if (reminder_time > 0 && mFirestore != null && context != null) {
            NotificationUtils notificationUtils = new NotificationUtils(context);
            String checkTime = WorkUtil.getCurrentTimeString(reminder_time);
            mFirestore.collection("works")
                    .whereEqualTo("email", email)
                    .whereGreaterThanOrEqualTo("deadline", checkTime)
                    .get().addOnSuccessListener(result -> {
                // set notification
                for (int i = 0; i < result.size(); i++) {
                    Work work = result.getDocuments().get(i).toObject(Work.class);
                    if (work != null && work.getProgress() < 100) {
                        long triggerReminder = WorkUtil.getDeadlineMillis(work);
                        triggerReminder -= (long) reminder_time * 60 * 1000;
                        notificationUtils.setReminder(
                                i, work.getTitle(),
                                notificationUtils.getReminderTimeString(reminder_time),
                                triggerReminder);
                    }
                }
            });
        }
    }

    /**
     * Get reminder time represented as a string
     */
    public String getReminderTimeString(int reminder_time) {
        if (reminder_time < 0){
            return "No reminder";
        } else if (reminder_time < 60) {
            return reminder_time + " minutes";
        } else if (reminder_time < 1440) {
            int hour = (reminder_time / 60);
            return hour + " hour" + (hour > 1 ? "s" : "");
        } else {
            int day = (reminder_time / 1440);
            return day + " day" + (day > 1 ? "s" : "");
        }
    }
}