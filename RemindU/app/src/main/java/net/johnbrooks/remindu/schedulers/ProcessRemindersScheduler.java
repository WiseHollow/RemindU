package net.johnbrooks.remindu.schedulers;

import android.os.Handler;

import net.johnbrooks.remindu.UserAreaActivity;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

/**
 * Created by ieatl on 12/6/2016.
 */

public class ProcessRemindersScheduler
{
    private static ProcessRemindersScheduler scheduler;
    public static void Initialize(UserAreaActivity activity)
    {
        if (scheduler == null)
            scheduler = new ProcessRemindersScheduler();
    }
    public static void Cancel()
    {
        scheduler.stopRepeatingTask();
        scheduler = null;
    }

    private final int mInterval = 5000; // milliseconds
    private Handler mHandler;

    public ProcessRemindersScheduler()
    {
        mHandler = new Handler();
        startRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable()
    {
        @Override
        public void run() {
            try
            {
                Update();
            } finally
            {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private void Update()
    {
        if (UserAreaActivity.GetActivity() == null)
        {
            Cancel();
        }
        else
        {
            for (Reminder r : UserProfile.PROFILE.GetReminders())
            {
                r.Remind();
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
