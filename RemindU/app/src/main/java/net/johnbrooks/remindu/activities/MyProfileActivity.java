package net.johnbrooks.remindu.activities;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
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

    public TextView tvChangeAvatar = null;
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
        // Prepare back button
        //

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

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
        tvChangeAvatar = (TextView) findViewById(R.id.textView_change_my_avatar);

        //
        // Set avatar picture
        //

        avatar.setBackground(AvatarImageUtil.GetAvatar(UserProfile.PROFILE.GetAvatarID()));

        View.OnClickListener avatarClick = new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(MyProfileActivity.this, AvatarSelectActivity.class);
                startActivityForResult(intent, PICK_AVATAR_REQUEST);
            }
        };

        avatar.setOnClickListener(avatarClick);
        tvChangeAvatar.setOnClickListener(avatarClick);

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
        // Fill TextView information
        //

        tvFullName.setText("" + UserProfile.PROFILE.GetFullName());
        tvUsername.setText("" + UserProfile.PROFILE.GetUsername());
        tvEmail.setText("" + UserProfile.PROFILE.GetEmail());

        tvActiveSentReminders.setText("" + UserProfile.PROFILE.GetActiveSentReminders().size());
        tvPendingReceivedReminders.setText("" + UserProfile.PROFILE.GetActiveReceivedReminders().size());

        tvPointsRemaining.setText("" + UserProfile.PROFILE.GetCoins());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PICK_AVATAR_REQUEST)
        {
            finish();
            startActivity(getIntent());
            UserProfile.PROFILE.RefreshReminderLayout();
            UpdateSettingsRequest.SendRequest(MyProfileActivity.this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
