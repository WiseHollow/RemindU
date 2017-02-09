package net.johnbrooks.remindu.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import net.johnbrooks.remindu.activities.LoginActivity;
import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

/**
 * Created by John on 12/19/2016.
 */

public class CancelReminderService extends Service
{
    public CancelReminderService()
    {
        super();
    }

    @Override
    public void onCreate()
    {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        Reminder reminder = UserProfile.PROFILE.GetReminder(intent.getIntExtra("reminder", 0));
        if (reminder != null)
        {
            UserProfile.PROFILE.DeleteReminder(reminder);
            NotificationManager mNotificationManager = (NotificationManager) MasterScheduler.GetInstance().GetContextWrapper().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(reminder.GetID());
        }

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

}
