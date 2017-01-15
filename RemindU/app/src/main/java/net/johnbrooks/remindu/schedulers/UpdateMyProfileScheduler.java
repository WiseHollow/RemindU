package net.johnbrooks.remindu.schedulers;

import android.os.Handler;

import net.johnbrooks.remindu.activities.MyProfileActivity;
import net.johnbrooks.remindu.util.UserProfile;

/**
 * Created by John on 11/30/2016.
 */

public class UpdateMyProfileScheduler
{
    private static UpdateMyProfileScheduler scheduler = null;
    public static void Initialize(MyProfileActivity activity)
    {
        if (scheduler == null)
            scheduler = new UpdateMyProfileScheduler(activity);
    }
    public static void Cancel()
    {
        scheduler.stopRepeatingTask();
        scheduler = null;
    }

    private MyProfileActivity Activity = null;
    private final int mInterval = 5000; // milliseconds
    private Handler mHandler;

    public UpdateMyProfileScheduler(MyProfileActivity activity)
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
            //
            // Fill TextView information
            //

            Activity.tvFullName.setText("" + UserProfile.PROFILE.GetFullName());
            Activity.tvUsername.setText("" + UserProfile.PROFILE.GetUsername());
            Activity.tvEmail.setText("" + UserProfile.PROFILE.GetEmail());

            Activity.tvActiveSentReminders.setText("" + UserProfile.PROFILE.GetActiveSentReminders().size());
            Activity.tvPendingReceivedReminders.setText("" + UserProfile.PROFILE.GetActiveReceivedReminders().size());

            Activity.tvPointsRemaining.setText("" + UserProfile.PROFILE.GetCoins());
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
