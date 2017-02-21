package net.johnbrooks.remindu.fragments;

import android.app.Activity;
import android.app.Dialog;
import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.requests.UpdateReminderLikeRequest;
import net.johnbrooks.remindu.util.AvatarImageUtil;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.ReminderFlag;
import net.johnbrooks.remindu.util.UserProfile;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class FeedFragment extends Fragment
{
    private static FeedFragment feedFragment;
    public static FeedFragment GetInstance() { return feedFragment; }

    private View ContentView;
    private View ContactLayout;

    private ImageView iv_compass;
    private ImageView iv_feed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        feedFragment = this;
        iv_compass = (ImageView) UserAreaActivity.GetActivity().findViewById(R.id.fragment_nav_button_compass);
        iv_feed = (ImageView) UserAreaActivity.GetActivity().findViewById(R.id.fragment_nav_button_feed);
        ContentView = inflater.inflate(R.layout.fragment_feed, container, false);
        ScrollView scrollView = (ScrollView) ContentView.findViewById(R.id.Feed_ScrollView);
        ContactLayout = getLayoutInflater(getArguments()).inflate(R.layout.widget_linear_layout, null);
        scrollView.addView(ContactLayout);

        ContentView.findViewById(R.id.fab).setOnClickListener(UserAreaActivity.GetActivity().OnClickSelectRecipients());

        return ContentView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        //MasterScheduler.GetInstance().Call();
    }

    @Override
    public void setMenuVisibility(final boolean visible)
    {
        super.setMenuVisibility(visible);
        if (visible)
        {
            if (iv_compass != null)
                iv_compass.setImageResource(R.drawable.compass_64_grey);
            if (iv_feed != null)
                iv_feed.setImageResource(R.drawable.activity_feed_64_blue);
            UserAreaActivity.GetActivity().setTitle("RemindU");
        }
    }

    public void PopulateActivity()
    {
        if (UserAreaActivity.GetActivity().GetLayoutInflater() == null)
            return;

        final List<ReminderFlag> flags = UserProfile.PROFILE.GetReminderFlags();
        Collections.sort(flags);
        ((ViewGroup) ContactLayout).removeAllViews();

        int realIndex = 0;

        ContentView.findViewById(R.id.feed_fragment_layout).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                //When anywhere else is clicked
                UserProfile.PROFILE.SetActiveReminderFlag(null);
                RefreshReminderLayout();
            }
        });

        for (final ReminderFlag flag : flags)
        {
            if (flag.GetDateOfFlag() == null)
                continue;

            if (!UserAreaActivity.GetActivity().SharedPreferences.getBoolean("settings_display_creations", false) &&
                    flag.GetState() == Reminder.ReminderState.NOT_STARTED &&
                    flag.GetReminder().GetFrom() == UserProfile.PROFILE.GetUserID())
                continue;

            realIndex++;

            LinearLayout widget = flag.CreateWidget(UserAreaActivity.GetActivity());
            ((ViewGroup) ContactLayout).addView(widget);
            if (realIndex % 2 != 0)
                widget.findViewById(R.id.feed_element_layout).setBackgroundColor(Color.parseColor("#eaf7ff"));
            else
                widget.findViewById(R.id.feed_element_layout).setBackgroundColor(Color.parseColor("#FCFCFC"));
        }

        if (realIndex == 0)
        {
            TextView tv = new TextView(UserAreaActivity.GetActivity());
            tv.setText("Nothing to see here.");
            tv.setGravity(Gravity.CENTER);
            tv.setPadding(0, 50, 0, 0);
            ((ViewGroup) ContactLayout).addView(tv);
        }
    }

    public void RefreshReminderLayout()
    {
        PopulateActivity();
    }
}
