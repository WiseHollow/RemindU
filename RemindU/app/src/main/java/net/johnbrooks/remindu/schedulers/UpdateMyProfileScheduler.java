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
            // Get TextViews
            //

            TextView tvFullName = (TextView) Activity.findViewById(R.id.textView_Profile_FullName);
            TextView tvUsername = (TextView) Activity.findViewById(R.id.textView_Profile_Username);
            TextView tvEmail = (TextView) Activity.findViewById(R.id.textView_Profile_Email);

            TextView tvActiveSentReminders = (TextView) Activity.findViewById(R.id.textView_Profile_ActiveSentReminders);
            TextView tvPendingReceivedReminders = (TextView) Activity.findViewById(R.id.textView_Profile_ActiveReceivedReminders);

            TextView tvPointsRemaining = (TextView) Activity.findViewById(R.id.textView_Profile_PointsRemaining);
            TextView tvPointsReceived = (TextView) Activity.findViewById(R.id.textView_Profile_PointsReceived);
            TextView tvPointsSent = (TextView) Activity.findViewById(R.id.textView_Profile_PointsSent);

            //
            // Fill TextView information
            //

            tvFullName.setText("Full Name: " + UserProfile.PROFILE.GetFullName());
            tvUsername.setText("Username: " + UserProfile.PROFILE.GetUsername());
            tvEmail.setText("Email: " + UserProfile.PROFILE.GetEmail());

            tvActiveSentReminders.setText("Active Sent Reminders: 0");
            tvPendingReceivedReminders.setText("Active Received Reminders: 0");

            tvPointsRemaining.setText("Points Remaining: " + UserProfile.PROFILE.GetPointsRemaining());
            tvPointsReceived.setText("Points Received: " + UserProfile.PROFILE.GetPointsReceived());
            tvPointsSent.setText("Points Sent: " + UserProfile.PROFILE.GetPointsSent());
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
