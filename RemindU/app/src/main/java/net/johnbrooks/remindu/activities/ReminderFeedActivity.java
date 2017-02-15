package net.johnbrooks.remindu.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.util.AvatarImageUtil;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ReminderFeedActivity extends AppCompatActivity
{
    private LinearLayout layout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_feed);
        layout = (LinearLayout) findViewById(R.id.linear_layout_activity_feed);

        //
        // Prepare back button
        //

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    @Deprecated
    private void PopulateActivity()
    {
        UserProfile.PROFILE.sortRemindersByDueDate = false;
        Collections.sort(UserProfile.PROFILE.GetReminders());

        for (final Reminder r : UserProfile.PROFILE.GetReminders())
        {
            if (r.GetDateInProgress() == null && r.GetDateComplete() == null)
                continue;
            if (r.GetTo() == UserProfile.PROFILE.GetUserID())
                continue;

            LinearLayout widget = (LinearLayout) getLayoutInflater().inflate(R.layout.widget_reminder_in_feed, null);
            layout.addView(widget);

            final LinearLayout l_desc = (LinearLayout) widget.findViewById(R.id.feed_element_layout_desc);
            l_desc.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    Log.d("INFO", "HIT");
                    r.ClickLogButton(UserAreaActivity.GetActivity(), r);
                }
            });

            final ImageView iv_avatar = (ImageView) widget.findViewById(R.id.feed_element_avatar);

            final TextView tv_fullName = (TextView) widget.findViewById(R.id.feed_element_fullName);
            final TextView tv_state = (TextView) widget.findViewById(R.id.feed_element_state);
            final TextView tv_activityInfo = (TextView) widget.findViewById(R.id.feed_element_activityInfo);
            final TextView tv_time = (TextView) widget.findViewById(R.id.feed_element_time);

            final ImageView iv_like = (ImageView) widget.findViewById(R.id.feed_element_like);
            if (r.IsLiked())
                iv_like.setBackgroundResource(R.drawable.like_it_filled_48);
            else
                iv_like.setBackgroundResource(R.drawable.like_it_48);
            iv_like.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    r.SetLiked(!r.IsLiked());
                    if (r.IsLiked())
                        iv_like.setBackgroundResource(R.drawable.like_it_filled_48);
                    else
                        iv_like.setBackgroundResource(R.drawable.like_it_48);
                    UserProfile.PROFILE.SaveRemindersToFile();
                }
            });

            ContactProfile cp;
            if (r.GetFrom() == UserProfile.PROFILE.GetUserID())
                cp = ContactProfile.GetProfile(r.GetTo());
            else
                cp = ContactProfile.GetProfile(r.GetFrom());
            iv_avatar.setBackground(AvatarImageUtil.GetAvatar(cp.GetAvatarID()));

            tv_fullName.setText(r.GetFullName());
            String state = r.GetState().name().replace("_", " ").toLowerCase();
            state = state.substring(0, 1).toUpperCase() + state.substring(1);

            tv_state.setText(state);
            tv_activityInfo.setText(r.GetMessage());

            if (r.GetDateComplete() != null)
                tv_time.setText(r.GetDateComplete());
            else
                tv_time.setText(r.GetDateInProgress());
        }

        UserProfile.PROFILE.sortRemindersByDueDate = true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
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
