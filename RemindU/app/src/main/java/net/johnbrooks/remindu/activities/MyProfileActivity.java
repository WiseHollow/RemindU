package net.johnbrooks.remindu.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.requests.UpdateSettingsRequest;
import net.johnbrooks.remindu.schedulers.UpdateMyProfileScheduler;
import net.johnbrooks.remindu.util.AvatarImageUtil;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

public class MyProfileActivity extends AppCompatActivity
{
    private static int PICK_AVATAR_REQUEST = 1;

    public TextView tvFullName = null;
    public TextView tvUsername = null;
    public TextView tvEmail = null;

    public TextView tvActiveSentReminders = null;
    public TextView tvPendingReceivedReminders = null;

    public TextView tvPointsRemaining = null;
    public ImageView avatar = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_profile);

        // This activity is the home of our personal information.

        //
        // Get TextViews
        //

        tvFullName = (TextView) findViewById(R.id.textView_Profile_FullName);
        tvUsername = (TextView) findViewById(R.id.textView_Profile_Username);
        tvEmail = (TextView) findViewById(R.id.textView_Profile_Email);

        tvActiveSentReminders = (TextView) findViewById(R.id.textView_Profile_ActiveSentReminders);
        tvPendingReceivedReminders = (TextView) findViewById(R.id.textView_Profile_ActiveReceivedReminders);

        tvPointsRemaining = (TextView) findViewById(R.id.textView_Profile_PointsRemaining);
        avatar = (ImageView) findViewById(R.id.imageView_my_profile_avatar);

        //
        // Set avatar picture
        //

        avatar.setBackground(AvatarImageUtil.GetAvatar(this, UserProfile.PROFILE.GetAvatarID()));

        avatar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MyProfileActivity.this, AvatarSelectActivity.class);
                startActivityForResult(intent, PICK_AVATAR_REQUEST);
            }
        });

        //
        // Set scheduler
        //

        UpdateMyProfileScheduler.Initialize(MyProfileActivity.this);
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        //
        // Get needed information
        //

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
        // Fill TextView information
        //

        tvFullName.setText("Full Name: " + UserProfile.PROFILE.GetFullName());
        tvUsername.setText("Username: " + UserProfile.PROFILE.GetUsername());
        tvEmail.setText("Email: " + UserProfile.PROFILE.GetEmail());

        tvActiveSentReminders.setText("Active Sent Reminders: " + activeSentReminders);
        tvPendingReceivedReminders.setText("Active Received Reminders: " + activeReceivedReminders);

        tvPointsRemaining.setText("Coins Remaining: " + UserProfile.PROFILE.GetCoins());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_AVATAR_REQUEST)
        {
            finish();
            startActivity(getIntent());
            UpdateSettingsRequest.SendRequest(MyProfileActivity.this);
        }
    }
}
