package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.os.Handler;

/**
 * Created by John on 11/30/2016.
 */

public class PullScheduler
{
    private static PullScheduler scheduler = null;
    public static void Initialize(Activity activity)
    {
        if (scheduler == null)
            scheduler = new PullScheduler(activity);
    }
    public static void Cancel()
    {
        scheduler.stopRepeatingTask();
        scheduler = null;
    }

    private final int mInterval = 5000; // milliseconds
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
