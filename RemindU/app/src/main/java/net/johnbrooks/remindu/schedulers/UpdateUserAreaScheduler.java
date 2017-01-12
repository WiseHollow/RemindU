package net.johnbrooks.remindu.schedulers;

import android.os.Handler;
import android.util.Log;

import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.util.UserProfile;

/**
 * Created by John on 11/30/2016.
 */

public class UpdateUserAreaScheduler
{
    private static UpdateUserAreaScheduler scheduler = null;
    public static void Initialize()
    {
        if (scheduler == null)
            scheduler = new UpdateUserAreaScheduler();
    }
    public static void Cancel()
    {
        if (scheduler == null)
            return;
        scheduler.stopRepeatingTask();
        scheduler = null;
    }
    public static void Call()
    {
        Cancel();
        Initialize();
    }

    private final int mInterval = 5000; // milliseconds
    private Handler mHandler;

    public UpdateUserAreaScheduler()
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
            Log.d("INFO", "Cancelling UpdateUserAreaScheduler...");
            Cancel();
        }
        else
        {
            UserProfile.PROFILE.RefreshReminderLayout();
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
