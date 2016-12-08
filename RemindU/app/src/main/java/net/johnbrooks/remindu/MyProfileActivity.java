package net.johnbrooks.remindu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import net.johnbrooks.remindu.schedulers.UpdateMyProfileScheduler;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

public class MyProfileActivity extends AppCompatActivity
{

    public TextView tvFullName = null;
    public TextView tvUsername = null;
    public TextView tvEmail = null;

    public TextView tvActiveSentReminders = null;
    public TextView tvPendingReceivedReminders = null;

    public TextView tvPointsRemaining = null;
    public TextView tvPointsReceived = null;
    public TextView tvPointsSent = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        // This activity is the home of our personal information.

        //
        // Get needed information
        //

        //TODO: If you go in MyProfile quickly after the app starts, these values are 0. :(

        int activeReceivedReminders = 0;
        int activeSentReminders = 0;

        for (Reminder r : UserProfile.PROFILE.GetReminders())
        {
            if (r.GetFrom() == UserProfile.PROFILE.GetUserID())
                activeSentReminders++;
            else if (r.GetTo() == UserProfile.PROFILE.GetUserID())
                activeReceivedReminders++;
        }

        //
        // Get TextViews
        //

        tvFullName = (TextView) findViewById(R.id.textView_Profile_FullName);
        tvUsername = (TextView) findViewById(R.id.textView_Profile_Username);
        tvEmail = (TextView) findViewById(R.id.textView_Profile_Email);

        tvActiveSentReminders = (TextView) findViewById(R.id.textView_Profile_ActiveSentReminders);
        tvPendingReceivedReminders = (TextView) findViewById(R.id.textView_Profile_ActiveReceivedReminders);

        tvPointsRemaining = (TextView) findViewById(R.id.textView_Profile_PointsRemaining);
        tvPointsReceived = (TextView) findViewById(R.id.textView_Profile_PointsReceived);
        tvPointsSent = (TextView) findViewById(R.id.textView_Profile_PointsSent);

        //
        // Fill TextView information
        //

        tvFullName.setText("Full Name: " + UserProfile.PROFILE.GetFullName());
        tvUsername.setText("Username: " + UserProfile.PROFILE.GetUsername());
        tvEmail.setText("Email: " + UserProfile.PROFILE.GetEmail());

        tvActiveSentReminders.setText("Active Sent Reminders: " + activeSentReminders);
        tvPendingReceivedReminders.setText("Active Received Reminders: " + activeReceivedReminders);

        tvPointsRemaining.setText("Points Remaining: " + UserProfile.PROFILE.GetPointsRemaining());
        tvPointsReceived.setText("Points Received: " + UserProfile.PROFILE.GetPointsReceived());
        tvPointsSent.setText("Points Sent: " + UserProfile.PROFILE.GetPointsSent());

        //
        // Set scheduler
        //

        UpdateMyProfileScheduler.Initialize(MyProfileActivity.this);
    }
}
