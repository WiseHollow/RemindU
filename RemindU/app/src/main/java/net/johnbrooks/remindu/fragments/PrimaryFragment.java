package net.johnbrooks.remindu.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ScrollView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.util.UserProfile;

public class PrimaryFragment extends Fragment
{
    private static PrimaryFragment primaryFragment;
    public static PrimaryFragment GetInstance() { return primaryFragment; }

    private View ContentView;
    public View ContactLayout;
    public ScrollView scrollView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        primaryFragment = this;

        ContentView = inflater.inflate(R.layout.fragment_primary, container, false);
        scrollView = (ScrollView) ContentView.findViewById(R.id.UserArea_ScrollView);
        ContactLayout = getLayoutInflater(getArguments()).inflate(R.layout.widget_linear_layout, null);
        scrollView.addView(ContactLayout);

        //UpdateUserAreaScheduler.Initialize();
        MasterScheduler.GetInstance(UserAreaActivity.GetActivity()).StartRepeatingTasks();
        //UserProfile.PROFILE.RefreshReminderLayout();
        return ContentView;
    }

    @Override
    public void setMenuVisibility(final boolean visible)
    {
        super.setMenuVisibility(visible);
        if (visible)
        {
            UserAreaActivity.GetActivity().setTitle("RemindU - Home");
        }
    }
}
