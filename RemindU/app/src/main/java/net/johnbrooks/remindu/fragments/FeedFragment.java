package net.johnbrooks.remindu.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.ReminderFeedActivity;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.util.AvatarImageUtil;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

import java.util.Collections;

public class FeedFragment extends Fragment
{
    private static FeedFragment feedFragment;
    public static FeedFragment GetInstance() { return feedFragment; }

    private View ContentView;
    private View ContactLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        feedFragment = this;
        ContentView = inflater.inflate(R.layout.fragment_feed, container, false);
        ScrollView scrollView = (ScrollView) ContentView.findViewById(R.id.Feed_ScrollView);
        ContactLayout = getLayoutInflater(getArguments()).inflate(R.layout.widget_linear_layout, null);
        scrollView.addView(ContactLayout);

        PopulateActivity();

        return ContentView;
    }

    public void PopulateActivity()
    {
        UserProfile.PROFILE.sortRemindersByDueDate = false;
        Collections.sort(UserProfile.PROFILE.GetReminders());
        ((ViewGroup) ContactLayout).removeAllViews();

        for (Reminder r : UserProfile.PROFILE.GetReminders())
        {
            Log.d("INFO", "HIT");
            if (r.GetDateInProgress() == null && r.GetDateComplete() == null)
                continue;
            if (r.GetTo() == UserProfile.PROFILE.GetUserID())
                continue;
            Log.d("INFO", "Starting");

            LinearLayout widget = (LinearLayout) getLayoutInflater(getArguments()).inflate(R.layout.widget_reminder_in_feed, null);
            ((ViewGroup) ContactLayout).addView(widget);

            final ImageView iv_avatar = (ImageView) widget.findViewById(R.id.feed_element_avatar);

            final TextView tv_fullName = (TextView) widget.findViewById(R.id.feed_element_fullName);
            final TextView tv_state = (TextView) widget.findViewById(R.id.feed_element_state);
            final TextView tv_activityInfo = (TextView) widget.findViewById(R.id.feed_element_activityInfo);
            final TextView tv_time = (TextView) widget.findViewById(R.id.feed_element_time);

            ContactProfile cp;
            if (r.GetFrom() == UserProfile.PROFILE.GetUserID())
                cp = ContactProfile.GetProfile(r.GetTo());
            else
                cp = ContactProfile.GetProfile(r.GetFrom());
            iv_avatar.setBackground(AvatarImageUtil.GetAvatar(UserAreaActivity.GetActivity(), cp.GetAvatarID()));

            tv_fullName.setText(r.GetFullName());
            String state = r.GetState().name().replace("_", " ").toLowerCase();
            state = state.substring(0, 1).toUpperCase() + state.substring(1);

            tv_state.setText(state);
            tv_activityInfo.setText(r.GetMessage());

            if (r.GetDateComplete() != null)
                tv_time.setText(r.GetDateComplete());
            else
                tv_time.setText(r.GetDateInProgress());
            Log.d("INFO", "Added");
        }

        UserProfile.PROFILE.sortRemindersByDueDate = true;
    }
}
