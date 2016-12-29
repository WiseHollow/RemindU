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

import com.baoyz.widget.PullRefreshLayout;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.schedulers.PullScheduler;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

import java.util.Collections;
import java.util.List;

public class ReminderListActivity extends AppCompatActivity
{
    private static ReminderListActivity activity = null;
    public static ReminderListActivity GetActivity() { return activity; }

    private PullRefreshLayout PullRefreshLayout;
    private LinearLayout ReminderLayout;
    private ContactProfile ContactProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reminder_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
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

        int ContactID = getIntent().getIntExtra("contactID", 0);
        ContactProfile = UserProfile.PROFILE.GetContact(ContactID);
        if (ContactProfile == null)
            finish();

        PullRefreshLayout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        ReminderLayout = (LinearLayout) findViewById(R.id.scrollView_Reminders_Layout);

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
        });



        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view) {
                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                if (!Network.IsConnected(ReminderListActivity.this))
                    return;

                if (!ContactProfile.IsContact())
                    return;

                Intent intent = new Intent(ReminderListActivity.this, CreateReminderActivity.class);
                intent.putExtra("user_id_to", ContactProfile.GetID());
                intent.putExtra("user_to_fullname", ContactProfile.GetFullName());
                ReminderListActivity.this.startActivity(intent);
            }
        });
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        ReminderLayout.removeAllViews();
        List<Reminder> reminders = ContactProfile.GetReminders();
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
                ReminderLayout.addView(layout);
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
