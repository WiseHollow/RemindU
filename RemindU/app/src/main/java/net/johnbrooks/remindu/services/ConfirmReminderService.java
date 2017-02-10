package net.johnbrooks.remindu.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

/**
 * Created by John on 12/19/2016.
 */

public class ConfirmReminderService extends Service
{
    public ConfirmReminderService()
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
        if (MasterScheduler.GetInstance() == null || MasterScheduler.GetInstance().GetContextWrapper() == null)
        {
            Log.d("SEVERE", "No MasterSchedule instance or context wrapper. ");
            return super.onStartCommand(intent, flags, startId);
        }

        if (UserProfile.PROFILE == null)
        {
            NotificationManager mNotificationManager = (NotificationManager) MasterScheduler.GetInstance().GetContextWrapper().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancelAll();
            return super.onStartCommand(intent, flags, startId);
        }

        Reminder reminder = UserProfile.PROFILE.GetReminder(intent.getIntExtra("reminder", 0));
        if (reminder != null)
        {
            reminder.SetUpToDate(true);
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
