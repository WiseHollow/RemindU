package net.johnbrooks.remindu.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.FloatingActionButton;
import android.util.Log;
import android.view.SubMenu;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baoyz.widget.PullRefreshLayout;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.requests.GetLatestVersionRequest;
import net.johnbrooks.remindu.schedulers.ProcessRemindersScheduler;
import net.johnbrooks.remindu.schedulers.UpdateUserAreaScheduler;
import net.johnbrooks.remindu.services.PullService;
import net.johnbrooks.remindu.util.AvatarImageUtil;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.schedulers.PullScheduler;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.UserProfile;

public class UserAreaActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener
{
    private static UserAreaActivity activity;
    public static UserAreaActivity GetActivity() { return activity; }

    private SubMenu contactsSubMenu;
    private PullRefreshLayout pullRefreshLayout;
    public LinearLayout reminderLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activity = UserAreaActivity.this;
        setContentView(R.layout.activity_user_area);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {
            public void onDrawerOpened(View drawerView)
            {
                super.onDrawerOpened(drawerView);
                invalidateOptionsMenu(); // creates call to onPrepareOptionsMenu()

            }
        };
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //
        // Gets
        //

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        reminderLayout = (LinearLayout) findViewById(R.id.scrollView_Reminders_Layout);
        pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        if (UserProfile.PROFILE == null || reminderLayout == null)
            return;

        //
        // Run schedules
        //

        UserProfile.PROFILE.LoadRemindersFromFile(UserAreaActivity.this);
        PullScheduler.Initialize();
        UpdateUserAreaScheduler.Initialize();
        ProcessRemindersScheduler.Initialize();
        if (sharedPreferences.getBoolean("check_for_updates", true))
            GetLatestVersionRequest.SendRequest(UserAreaActivity.this);
        if (!sharedPreferences.getBoolean("boot_switch", true))
        {
            //Intent serviceIntent = new Intent(getBaseContext(), PullService.class);
            //getBaseContext().startService(serviceIntent);
        }

        //
        // Sets
        //

        UserProfile.PROFILE.RefreshReminderLayout();





        //
        // Create Listeners
        //

        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                if (!Network.IsConnected(UserAreaActivity.this))
                    pullRefreshLayout.setRefreshing(false);
                else
                {
                    PullScheduler.Call();
                    pullRefreshLayout.postDelayed(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            pullRefreshLayout.setRefreshing(false);
                        }
                    }, 1500);
                }

            }
        });
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        findViewById(R.id.drawer_profile_picture).setBackground(AvatarImageUtil.GetAvatar(this, UserProfile.PROFILE.GetAvatarID()));
        ((TextView) findViewById(R.id.drawer_profile_name)).setText(UserProfile.PROFILE.GetFullName());
        ((TextView) findViewById(R.id.drawer_profile_email)).setText(UserProfile.PROFILE.GetEmail());
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            moveTaskToBack(true);
            //super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.nav_profile)
        {
            Intent intent = new Intent(UserAreaActivity.this, MyProfileActivity.class);
            UserAreaActivity.this.startActivity(intent);
        }
        else if (id == R.id.nav_manage)
        {
            Intent intent = new Intent(UserAreaActivity.this, ManageContactsActivity.class);
            UserAreaActivity.this.startActivity(intent);
        }
        else if (id == R.id.nav_invite)
        {
            //TODO: Gives functionality.
        }
        else if (id == R.id.nav_settings)
        {
            Intent intent = new Intent(UserAreaActivity.this, SettingsActivity.class);
            UserAreaActivity.this.startActivity(intent);
        }
        else if (id == R.id.nav_sign_out)
        {
            Intent signOutIntent = new Intent(UserAreaActivity.this, LoginActivity.class);
            signOutIntent.putExtra("signOut", true);
            UserAreaActivity.this.startActivity(signOutIntent);
            finish();
        }
        else if (id == R.id.nav_feedback)
        {

        }
        else if (id == R.id.nav_about)
        {
            Intent intent = new Intent(UserAreaActivity.this, AboutActivity.class);
            UserAreaActivity.this.startActivity(intent);
        }
        else if (id == R.id.nav_exit)
        {
            finish();
            System.exit(0);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
