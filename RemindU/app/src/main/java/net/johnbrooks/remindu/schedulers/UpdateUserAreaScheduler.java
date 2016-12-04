package net.johnbrooks.remindu.schedulers;

import android.os.Handler;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.UserAreaActivity;
import net.johnbrooks.remindu.util.UserProfile;

/**
 * Created by John on 11/30/2016.
 */

public class UpdateUserAreaScheduler
{
    private static UpdateUserAreaScheduler scheduler = null;
    public static void Initialize(UserAreaActivity activity)
    {
        if (scheduler == null)
            scheduler = new UpdateUserAreaScheduler(activity);
    }
    public static void Cancel()
    {
        scheduler.stopRepeatingTask();
        scheduler = null;
    }

    private UserAreaActivity Activity = null;
    private final int mInterval = 5000; // milliseconds
    private Handler mHandler;

    public UpdateUserAreaScheduler(UserAreaActivity activity)
    {
        Activity = activity;
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
                Update();
            } finally
            {
                mHandler.postDelayed(mStatusChecker, mInterval);
            }
        }
    };

    private void Update()
    {
        if (Activity == null)
        {
            Cancel();
        }
        else
        {
            final LinearLayout layout = (LinearLayout) Activity.findViewById(R.id.scrollView_Reminders_Layout);

            UserProfile.PROFILE.RefreshReminderLayout();
            Activity.SetupContacts();
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
