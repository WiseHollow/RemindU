package net.johnbrooks.remindu.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReminderListActivity extends AppCompatActivity
{
    private static ReminderListActivity activity = null;
    public static ReminderListActivity GetActivity() { return activity; }
    private LinearLayout ReminderLayout;
    private ContactProfile ContactProfile;
    private int ContactID;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (UserProfile.PROFILE == null)
        {
            finish();
            return;
        }

        activity = this;

        //
        // Prepare back button
        //

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //
        // Get variables
        //

        ContactID = getIntent().getIntExtra("contactID", 0);
        if (ContactID != -1)
        {
            ContactProfile = UserProfile.PROFILE.GetContact(ContactID);
            if (ContactProfile == null)
            {
                finish();
                return;
            }
        }

        ReminderLayout = (LinearLayout) findViewById(R.id.scrollView_Reminders_Layout);
        /*PullRefreshLayout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        PullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if (!Network.IsConnected(ReminderListActivity.this))
                    PullRefreshLayout.setRefreshing(false);
                else
                {
                    PullScheduler.Call();
                    PullRefreshLayout.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            PullRefreshLayout.setRefreshing(false);
                            onStart();
                        }
                    }, 1500);
                }

            }
        });*/



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                if (!Network.IsConnected() && ContactID != -1)
                    return;

                if (ContactID != -1 && !ContactProfile.IsContact())
                    return;

                Intent intent = new Intent(ReminderListActivity.this, CreateReminderActivity.class);
                intent.putExtra("user_id_to", (ContactID != -1) ? ContactProfile.GetID() : -1);
                intent.putExtra("user_to_fullname", (ContactID != -1) ? ContactProfile.GetFullName() : "Me");
                ReminderListActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        ReminderLayout.removeAllViews();

        List<LinearLayout> remindersNotStarted = new ArrayList<>();
        List<LinearLayout> remindersStarted = new ArrayList<>();
        List<LinearLayout> remindersComplete = new ArrayList<>();

        List<Reminder> reminders;

        if (ContactID != -1)
        {
            reminders = ContactProfile.GetReminders();
        }
        else
        {
            reminders = UserProfile.PROFILE.GetPersonalReminders();
        }

        Collections.sort(reminders);
        if (reminders.size() == 0)
        {
            TextView tv = new TextView(ReminderListActivity.this);
            tv.setText("Nothing to see here.");
            ReminderLayout.addView(tv);
        }
        else
        {
            for (int i = 0; i < reminders.size(); i++)
            {
                Reminder r = reminders.get(i);
                LinearLayout layout = r.CreateWidget(ReminderListActivity.this);
                if (i % 2 != 0)
                    layout.setBackgroundColor(Color.parseColor("#EBEBEB"));
                else
                    layout.setBackgroundColor(Color.parseColor("#FCFCFC"));

                switch (r.GetState())
                {
                    case NOT_STARTED:
                        remindersNotStarted.add(layout);
                        break;
                    case IN_PROGRESS:
                        remindersStarted.add(layout);
                        break;
                    case COMPLETE:
                        remindersComplete.add(layout);
                        break;
                    default:
                        remindersNotStarted.add(layout);
                }
                //ReminderLayout.addView(layout);
            }

            if (remindersNotStarted.size() > 0)
            {
                LinearLayout separatorNotStarted = (LinearLayout) getLayoutInflater().inflate(R.layout.widget_progress_separator, null);
                ((TextView) separatorNotStarted.findViewById(R.id.Category_Separator)).setText("Not Started");

                ReminderLayout.addView(separatorNotStarted);
                for (LinearLayout l : remindersNotStarted)
                    ReminderLayout.addView(l);
            }

            if (remindersStarted.size() > 0)
            {
                LinearLayout separatorInProgress = (LinearLayout) getLayoutInflater().inflate(R.layout.widget_progress_separator, null);
                ((TextView) separatorInProgress.findViewById(R.id.Category_Separator)).setText("In Progress");

                ReminderLayout.addView(separatorInProgress);
                for (LinearLayout l : remindersStarted)
                    ReminderLayout.addView(l);
            }

            if (remindersComplete.size() > 0)
            {
                LinearLayout separatorComplete = (LinearLayout) getLayoutInflater().inflate(R.layout.widget_progress_separator, null);
                ((TextView) separatorComplete.findViewById(R.id.Category_Separator)).setText("Completed");

                ReminderLayout.addView(separatorComplete);
                for (LinearLayout l : remindersComplete)
                    ReminderLayout.addView(l);
            }
        }
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

    public void RefreshReminderLayout()
    {
        onStart();
    }
}
