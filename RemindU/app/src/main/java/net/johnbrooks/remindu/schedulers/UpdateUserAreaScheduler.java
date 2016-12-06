package net.johnbrooks.remindu.schedulers;

import android.os.Handler;

import net.johnbrooks.remindu.UserAreaActivity;
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
        scheduler.stopRepeatingTask();
        scheduler = null;
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
            Cancel();
        }
        else
        {
            UserProfile.PROFILE.RefreshReminderLayout();
            UserAreaActivity.GetActivity().SetupContacts();
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
