package net.johnbrooks.remindu;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import net.johnbrooks.remindu.schedulers.UpdateMyProfileScheduler;
import net.johnbrooks.remindu.util.UserProfile;

public class MyProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        //
        // Get TextViews
        //

        TextView tvFullName = (TextView) findViewById(R.id.textView_Profile_FullName);
        TextView tvUsername = (TextView) findViewById(R.id.textView_Profile_Username);
        TextView tvEmail = (TextView) findViewById(R.id.textView_Profile_Email);

        TextView tvActiveSentReminders = (TextView) findViewById(R.id.textView_Profile_ActiveSentReminders);
        TextView tvPendingReceivedReminders = (TextView) findViewById(R.id.textView_Profile_ActiveReceivedReminders);

        TextView tvPointsRemaining = (TextView) findViewById(R.id.textView_Profile_PointsRemaining);
        TextView tvPointsReceived = (TextView) findViewById(R.id.textView_Profile_PointsReceived);
        TextView tvPointsSent = (TextView) findViewById(R.id.textView_Profile_PointsSent);

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

        //
        // Set scheduler
        //

        UpdateMyProfileScheduler.Initialize(MyProfileActivity.this);
    }
}
