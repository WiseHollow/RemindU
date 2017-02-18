package net.johnbrooks.remindu.fragments;

import android.graphics.Color;
import android.support.v4.app.Fragment;
import android.os.Bundle;
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

        List<ReminderFlag> flags = UserProfile.PROFILE.GetReminderFlags();
        Collections.sort(flags);
        ((ViewGroup) ContactLayout).removeAllViews();

        int realIndex = 0;

        for (final ReminderFlag flag : flags)
        {
            if (flag.GetDateOfFlag() == null || flag.GetReminder().GetTo() == UserProfile.PROFILE.GetUserID())
                continue;

            realIndex++;

            LinearLayout widget = (LinearLayout) UserAreaActivity.GetActivity().GetLayoutInflater().inflate(R.layout.widget_reminder_in_feed, null);
            ((ViewGroup) ContactLayout).addView(widget);
            widget.findViewById(R.id.feed_element_layout_desc).setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    flag.GetReminder().ClickLogButton(UserAreaActivity.GetActivity());
                }
            });

            final View view = widget.findViewById(R.id.feed_element_layout);
            if (realIndex % 2 != 0)
                view.setBackgroundColor(Color.parseColor("#eaf7ff"));
            else
                view.setBackgroundColor(Color.parseColor("#FCFCFC"));

            final ImageView iv_avatar = (ImageView) widget.findViewById(R.id.feed_element_avatar);
            final TextView tv_fullName = (TextView) widget.findViewById(R.id.feed_element_fullName);
            final TextView tv_state = (TextView) widget.findViewById(R.id.feed_element_state);
            final TextView tv_activityInfo = (TextView) widget.findViewById(R.id.feed_element_activityInfo);
            final TextView tv_time = (TextView) widget.findViewById(R.id.feed_element_time);
            final ImageView iv_like = (ImageView) widget.findViewById(R.id.feed_element_like);

            if (flag.IsLiked())
                iv_like.setBackgroundResource(R.drawable.like_it_filled_48);
            else
                iv_like.setBackgroundResource(R.drawable.like_it_48);
            iv_like.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    flag.SetLiked(!flag.IsLiked());
                    if (flag.IsLiked())
                        iv_like.setBackgroundResource(R.drawable.like_it_filled_48);
                    else
                        iv_like.setBackgroundResource(R.drawable.like_it_48);
                    UserProfile.PROFILE.SaveReminderFlagsToFile();
                    UpdateReminderLikeRequest.SendRequest(flag);
                }
            });

            ContactProfile cp;
            if (flag.GetReminder().GetFrom() == UserProfile.PROFILE.GetUserID())
                cp = ContactProfile.GetProfile(flag.GetReminder().GetTo());
            else
                cp = ContactProfile.GetProfile(flag.GetReminder().GetFrom());
            iv_avatar.setBackground(AvatarImageUtil.GetAvatar(cp.GetAvatarID()));

            tv_fullName.setText(flag.GetReminder().GetFullName());
            String state = flag.GetState().toString();
            state = state.substring(0, 1).toUpperCase() + state.substring(1);

            tv_state.setText(state);
            tv_activityInfo.setText(flag.GetReminder().GetMessage());

            DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
            Date reminderDateToCompare;
            String timeLeft;

            try
            {
                reminderDateToCompare = formatter.parse(flag.GetDateOfFlag());
                timeLeft = flag.GetReminder().GetRoughTimeSince(reminderDateToCompare);
            } catch (ParseException e)
            {
                e.printStackTrace();
                timeLeft = "error";
            }

            tv_time.setText(timeLeft);
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
}
