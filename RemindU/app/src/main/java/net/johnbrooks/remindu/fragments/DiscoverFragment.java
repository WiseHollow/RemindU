package net.johnbrooks.remindu.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.schedulers.MasterScheduler;

public class DiscoverFragment extends Fragment
{
    private static DiscoverFragment discoverFragment;
    public static DiscoverFragment GetInstance() { return discoverFragment; }

    private View ContentView;
    public View ContactLayout;
    public ScrollView scrollView;

    private ImageView iv_compass;
    private ImageView iv_feed;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        discoverFragment = this;
        iv_compass = (ImageView) UserAreaActivity.GetActivity().findViewById(R.id.fragment_nav_button_compass);
        iv_feed = (ImageView) UserAreaActivity.GetActivity().findViewById(R.id.fragment_nav_button_feed);

        ContentView = inflater.inflate(R.layout.fragment_discover, container, false);
        scrollView = (ScrollView) ContentView.findViewById(R.id.UserArea_ScrollView);
        ContactLayout = getLayoutInflater(getArguments()).inflate(R.layout.widget_linear_layout, null);
        scrollView.addView(ContactLayout);

        ContentView.findViewById(R.id.fab).setOnClickListener(UserAreaActivity.GetActivity().OnClickSelectRecipients());

        MasterScheduler.GetInstance(UserAreaActivity.GetActivity()).StartRepeatingTasks();
        return ContentView;
    }

    @Override
    public void setMenuVisibility(final boolean visible)
    {
        super.setMenuVisibility(visible);
        if (visible)
        {
            if (iv_compass != null)
                iv_compass.setImageResource(R.drawable.compass_64_blue);
            if (iv_feed != null)
                iv_feed.setImageResource(R.drawable.activity_feed_64_grey);
            UserAreaActivity.GetActivity().setTitle("RemindU");
        }
    }
}
