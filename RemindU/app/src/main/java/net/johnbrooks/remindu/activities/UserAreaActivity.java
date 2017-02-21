package net.johnbrooks.remindu.activities;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.requests.GetLatestVersionRequest;
import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.util.AvatarImageUtil;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.PagerAdapter;
import net.johnbrooks.remindu.util.UserProfile;

import java.util.HashMap;

public class UserAreaActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{
    private static UserAreaActivity activity;
    public static UserAreaActivity GetActivity() { return activity; }
    public SharedPreferences SharedPreferences;

    private ViewPager viewPager;
    private PagerAdapter pagerAdapter;

    private LayoutInflater layoutInflater;
    public LayoutInflater GetLayoutInflater() { return layoutInflater; }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activity = UserAreaActivity.this;
        layoutInflater = (LayoutInflater) getSystemService( Context.LAYOUT_INFLATER_SERVICE );
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

        if (UserProfile.PROFILE.GetActiveState() == 0 && !ActivateAccountActivity.IsOpen())
        {
            Intent activateIntent = new Intent(UserAreaActivity.GetActivity(), ActivateAccountActivity.class);
            UserAreaActivity.GetActivity().startActivity(activateIntent);
        }
        else if (UserProfile.PROFILE.GetActiveState() == 2 && !SharedPreferences.getBoolean("readDisabledMessage", false))
        {
            Intent disabledIntent = new Intent(UserAreaActivity.this, AccountDisabledActivity.class);
            startActivity(disabledIntent);
        }

        //
        // Run schedules
        //

        if (SharedPreferences.getBoolean("settings_check_for_updates", true))
            GetLatestVersionRequest.SendRequest(UserAreaActivity.this);

        //
        // Create Pager for Fragment view
        //

        viewPager = (ViewPager) findViewById(R.id.viewPager);
        pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);

        //
        // Listen for fragment nav button click
        //

        final ImageView feed = (ImageView) findViewById(R.id.fragment_nav_button_feed);
        feed.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                viewPager.setCurrentItem(0, true);
            }
        });

        final ImageView compass = (ImageView) findViewById(R.id.fragment_nav_button_compass);
        compass.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                viewPager.setCurrentItem(1, true);
            }
        });

        //
        // Create Listeners
        //

        findViewById(R.id.user_area_refresh).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                MasterScheduler.GetInstance(UserAreaActivity.this).Call();
            }
        });

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
        findViewById(R.id.drawer_profile_picture).setBackground(AvatarImageUtil.GetAvatar(UserProfile.PROFILE.GetAvatarID()));
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
            activity = null;
            UserAreaActivity.this.startActivity(signOutIntent);
            finish();
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

    public View.OnClickListener OnClickSelectRecipients()
    {
        return new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final Dialog dialog = new Dialog(UserAreaActivity.this);
                dialog.setTitle("Select Recipients");
                dialog.setContentView(R.layout.dialog_select_recipients);

                dialog.findViewById(R.id.dialog_select_recipients_close).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        dialog.cancel();
                    }
                });

                final HashMap<CheckBox, ContactProfile> selection = new HashMap<>();

                dialog.findViewById(R.id.dialog_select_recipients_done).setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {

                        String fullNames = "";
                        String userIds = "";

                        for (CheckBox cb : selection.keySet())
                        {
                            if (cb.isChecked())
                            {
                                ContactProfile c = selection.get(cb);
                                fullNames += c.GetFullName() + ", ";
                                userIds += c.GetID() + ", ";
                            }
                        }

                        if (userIds == "" || fullNames == "")
                        {
                            dialog.cancel();
                            return;
                        }

                        fullNames = fullNames.substring(0, fullNames.length() - 2);
                        userIds = userIds.substring(0, userIds.length() - 2);

                        Log.d("INFO", "Names: \"" + fullNames + "\"");
                        Log.d("INFO", "IDs: \"" + userIds + "\"");

                        Intent intent = new Intent(UserAreaActivity.this, CreateReminderActivity.class);
                        intent.putExtra("recipients_fullNames", fullNames);
                        intent.putExtra("recipients_ids", userIds);
                        startActivity(intent);
                        dialog.cancel();
                    }
                });

                LinearLayout layout = (LinearLayout) dialog.findViewById(R.id.dialog_select_recipients_layout);


                for (ContactProfile cp : UserProfile.PROFILE.GetContacts())
                {
                    if (cp.IsContact())
                    {
                        View view = getLayoutInflater().inflate(R.layout.widget_contact_selection, null);

                        TextView tv_fullName = (TextView) view.findViewById(R.id.widget_contact_selection_fullName);
                        TextView tv_reputation = (TextView) view.findViewById(R.id.widget_contact_selection_reputation);

                        ImageView iv_avatar = (ImageView) view.findViewById(R.id.widget_contact_selection_avatar);

                        CheckBox cb_select = (CheckBox) view.findViewById(R.id.widget_contact_selection_checkbox);
                        selection.put(cb_select, cp);

                        tv_fullName.setText(cp.GetFullName());
                        tv_reputation.setText("N/A");
                        iv_avatar.setImageDrawable(AvatarImageUtil.GetAvatar(cp.GetAvatarID()));

                        layout.addView(view);
                    }
                }

                dialog.show();
            }
        };
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
