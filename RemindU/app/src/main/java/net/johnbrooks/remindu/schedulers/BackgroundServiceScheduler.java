package net.johnbrooks.remindu.schedulers;

import android.app.Service;
import android.os.Handler;
import android.util.Log;

import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

/**
 * Created by ieatl on 12/6/2016.
 */

public class BackgroundServiceScheduler
{
    private static BackgroundServiceScheduler scheduler;
    public static BackgroundServiceScheduler GetScheduler() { return scheduler; }
    public static void Initialize(Service service)
    {
        if (scheduler == null)
        {
            scheduler = new BackgroundServiceScheduler(service);
            scheduler.startRepeatingTask();
        }
    }
    public static void Cancel()
    {
        scheduler.stopRepeatingTask();
        scheduler = null;
    }

    private final int mInterval = 60000; // 1 minute 60000
    private Handler mHandler;
    private Service service;

    public BackgroundServiceScheduler(Service service)
    {
        mHandler = new Handler();
        this.service = service;
    }

    public Service GetService() { return service; }

    Runnable mStatusChecker = new Runnable()
    {
        @Override
        public void run() {
            try
            {
                Update();
            } finally
            {
                if (scheduler != null)
                    mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private void Update()
    {
        if (service == null)
        {
            Log.d("SEVERE", "background service is null! Cancelling task...");
            Cancel();
        }
        else
        {

            UserProfile.PROFILE.Pull(service);

            if (UserProfile.PROFILE != null)
            {
                for (Reminder r : UserProfile.PROFILE.GetReminders())
                {
                    r.ProcessReminderNotifications(service);
                }
            }
            else
            {
                Log.d("SEVERE", "null profile");
            }
        }
    }

    private void startRepeatingTask()
    {
        mStatusChecker.run();
    }

    public void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
