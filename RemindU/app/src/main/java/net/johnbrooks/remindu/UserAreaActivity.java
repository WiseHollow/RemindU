package net.johnbrooks.remindu;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
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

import net.johnbrooks.remindu.schedulers.UpdateUserAreaScheduler;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.schedulers.PullScheduler;
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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();

            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        //
        // Gets
        //

        final TextView etName = (TextView) findViewById(R.id.textView_Name);
        reminderLayout = (LinearLayout) findViewById(R.id.scrollView_Reminders_Layout);

        final Menu menu = navigationView.getMenu();
        contactsSubMenu = menu.addSubMenu("Contacts");

        pullRefreshLayout = (PullRefreshLayout) findViewById(R.id.swipeRefreshLayout);

        if (UserProfile.PROFILE == null || reminderLayout == null)
            return;

        //
        // Run schedules
        //

        PullScheduler.Initialize(UserAreaActivity.this);
        UpdateUserAreaScheduler.Initialize(UserAreaActivity.this);

        //
        // Sets
        //

        etName.setText(UserProfile.PROFILE.GetFullName());
        UserProfile.PROFILE.RefreshReminderLayout();
        SetupContacts();

        //
        // Create Listeners
        //

        pullRefreshLayout.setOnRefreshListener(new PullRefreshLayout.OnRefreshListener()
        {
            @Override
            public void onRefresh()
            {
                PullScheduler.Call();
                pullRefreshLayout.postDelayed(new Runnable() {
                    @Override
                    public void run()
                    {
                        pullRefreshLayout.setRefreshing(false);
                    }
                }, 1500);
            }
        });
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user_area, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
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

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void SetupContacts()
    {
        contactsSubMenu.clear();
        for(final ContactProfile contact : UserProfile.PROFILE.GetContacts())
        {
            MenuItem item = contactsSubMenu.add(contact.GetFullName());
            item.setIcon(R.drawable.user_48);
            item.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    if (!contact.IsContact())
                        return false;

                    Intent intent = new Intent(UserAreaActivity.this, CreateReminderActivity.class);
                    intent.putExtra("user_id_to", contact.GetID());
                    intent.putExtra("user_to_fullname", contact.GetFullName());
                    UserAreaActivity.this.startActivity(intent);

                    return true;
                }
            });
        }
    }
}
