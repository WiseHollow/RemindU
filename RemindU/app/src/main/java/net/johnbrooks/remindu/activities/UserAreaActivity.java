package net.johnbrooks.remindu.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.requests.GetLatestVersionRequest;
import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.schedulers.ProcessRemindersScheduler;
import net.johnbrooks.remindu.util.AvatarImageUtil;
import net.johnbrooks.remindu.schedulers.PullScheduler;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.PagerAdapter;
import net.johnbrooks.remindu.util.UserProfile;

public class UserAreaActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private static UserAreaActivity activity;
    public static UserAreaActivity GetActivity() { return activity; }
    public SharedPreferences SharedPreferences;

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

        //ContactScrollView = (ScrollView) findViewById(R.id.UserArea_ScrollView);
        SharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        //pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        if (UserProfile.PROFILE == null)
        {
            Log.d("SEVERE", "UserProfile.PROFILE is null onCreate UserAreaActivity. ");
            return;
        }

        //
        // Check if account is active
        //

        if (UserProfile.PROFILE.IsActive() != 1 && !ActivateAccountActivity.IsOpen())
        {
            Intent activateIntent = new Intent(UserAreaActivity.GetActivity(), ActivateAccountActivity.class);
            UserAreaActivity.GetActivity().startActivity(activateIntent);
        }

        //
        // Run schedules
        //

        UserProfile.PROFILE.LoadRemindersFromFile();
        MasterScheduler.GetInstance(UserAreaActivity.this).StartRepeatingTasks();

        //PullScheduler.Initialize();
        //ProcessRemindersScheduler.Initialize();
        
        if (SharedPreferences.getBoolean("check_for_updates", true))
            GetLatestVersionRequest.SendRequest(UserAreaActivity.this);

        //
        // Create Pager for Fragment view
        //

        ViewPager viewPager = (ViewPager) findViewById(R.id.viewPager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        //
        // Create Listeners
        //

        findViewById(R.id.user_area_refresh).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (Network.IsConnected(UserAreaActivity.this))
                    MasterScheduler.GetInstance(UserAreaActivity.this).Call();
            }
        });

        /*pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener()
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
        });*/

        // See if there is a pending contact to view.

        int ContactID = getIntent().getIntExtra("contactID", 0);
        ContactProfile cp = UserProfile.PROFILE.GetContact(ContactID);
        if (cp != null)
        {
            Intent intent = new Intent(UserAreaActivity.this, ReminderListActivity.class);
            intent.putExtra("contactID", cp.GetID());
            startActivity(intent);
        }
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

    /*public ContactViewType GetCurrentContactViewType()
    {
        return ContactViewType.values()[SharedPreferences.getInt("VIEW", 0)];
    }

    public void ApplyContactViewType()
    {
        ContactViewType viewType = GetCurrentContactViewType();
        ContactScrollView.removeAllViewsInLayout();
        ContactLayout = ((viewType == ContactViewType.LIST) ? getLayoutInflater().inflate(R.layout.widget_linear_layout, null) : getLayoutInflater().inflate(R.layout.widget_grid_layout, null));
        ContactScrollView.addView(ContactLayout);
    }*/
}
