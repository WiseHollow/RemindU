package net.johnbrooks.remindu.schedulers;

import android.app.Activity;
import android.os.Handler;

import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.util.UserProfile;

/**
 * Created by John on 11/30/2016.
 */

public class PullScheduler
{
    private static PullScheduler scheduler = null;
    public static void Initialize()
    {
        if (scheduler == null)
            scheduler = new PullScheduler(UserAreaActivity.GetActivity());
    }
    public static void Cancel()
    {
        scheduler.stopRepeatingTask();
        scheduler = null;
    }
    public static void Call()
    {
        Cancel();
        Initialize();
    }

    private final int mInterval = 60000; // milliseconds. 60 seconds
    private Handler mHandler;
    private Activity Activity;

    private PullScheduler(Activity activity)
    {
        mHandler = new Handler();
        Activity = activity;
        startRepeatingTask();
    }

    Runnable mStatusChecker = new Runnable()
    {
        @Override
        public void run() {
            try
            {
                UserProfile.PROFILE.Pull(Activity);
            } finally
            {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private void startRepeatingTask()
    {
        mStatusChecker.run();
    }

    public void stopRepeatingTask()
    {
        mHandler.removeCallbacks(mStatusChecker);
    }
}
