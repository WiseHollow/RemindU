package net.johnbrooks.remindu.schedulers;

import android.os.Handler;
import android.widget.TextView;

import net.johnbrooks.remindu.ManageContactsActivity;
import net.johnbrooks.remindu.MyProfileActivity;
import net.johnbrooks.remindu.R;
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

            Activity.tvFullName.setText("Full Name: " + UserProfile.PROFILE.GetFullName());
            Activity.tvUsername.setText("Username: " + UserProfile.PROFILE.GetUsername());
            Activity.tvEmail.setText("Email: " + UserProfile.PROFILE.GetEmail());

            Activity.tvActiveSentReminders.setText("Active Sent Reminders: 0");
            Activity.tvPendingReceivedReminders.setText("Active Received Reminders: 0");

            Activity.tvPointsRemaining.setText("Coins Remaining: " + UserProfile.PROFILE.GetCoins());
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
