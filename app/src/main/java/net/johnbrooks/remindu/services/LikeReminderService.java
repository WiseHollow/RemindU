package net.johnbrooks.remindu.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import net.johnbrooks.remindu.exceptions.ReminderNotFoundException;
import net.johnbrooks.remindu.fragments.FeedFragment;
import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.ReminderFlag;
import net.johnbrooks.remindu.util.UserProfile;

/**
 * Created by John on 12/19/2016.
 */

public class LikeReminderService extends Service
{
    public LikeReminderService()
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
        Reminder.ReminderState state = Reminder.ReminderState.values()[intent.getIntExtra("state", 0)];
        if (reminder != null)
        {
            ReminderFlag flag = reminder.GetFlag(state);
            if (flag != null)
                flag.SetLiked(true);
            else
                try
                {
                    ReminderFlag.Create(intent.getIntExtra("reminder", 0), state, true);
                } catch (ReminderNotFoundException e)
                {
                    e.printStackTrace();
                }

            reminder.SetUpToDate(true);
            NotificationManager mNotificationManager = (NotificationManager) MasterScheduler.GetInstance().GetContextWrapper().getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(reminder.GetID());

            if (FeedFragment.GetInstance() != null)
                FeedFragment.GetInstance().PopulateActivity();
        }

        return super.onStartCommand(intent, flags, startId);
    }
    @Override
    public IBinder onBind(Intent arg0)
    {
        return null;
    }

}
