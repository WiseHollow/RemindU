package net.johnbrooks.remindu.activities;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.requests.AddContactRequest;
import net.johnbrooks.remindu.schedulers.UpdateManageContactsScheduler;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.UserProfile;

import java.util.Collections;
import java.util.List;

public class ManageContactsActivity extends AppCompatActivity
{
    private static ManageContactsActivity activity;
    public static ManageContactsActivity GetInstance() { return activity; }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        activity = this;
        setContentView(R.layout.activity_manage_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

        //
        // Prepare back button
        //

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //
        // fab will be treated as a new contact button. When clicked, popup a dialog to enter
        // an email address.
        //

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                //        .setAction("Action", null).show();
                final Dialog dialog = new Dialog(ManageContactsActivity.this);
                dialog.setTitle("Add Contact");
                dialog.setContentView(R.layout.dialog_add_contact);
                dialog.show();

                final EditText et_Target = (EditText) dialog.findViewById(R.id.editText_AddContact_Email);
                Button button = (Button) dialog.findViewById(R.id.button_AddContact_Submit);

                button.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        String target = et_Target.getText().toString();

                        Log.d("INFO", "Requesting that contact email/username=" + target + " be added.");

                        AddContactRequest.SendRequest(ManageContactsActivity.this, target);

                        dialog.cancel();
                    }
                });
            }
        });

        //
        // Set scheduler to update contact list every set period of time.
        //

        UpdateManageContactsScheduler.Initialize(ManageContactsActivity.this);

        //
        // Fill up the contact list for the first time.
        //

        UpdateContactsList();
    }

    public void UpdateContactsList()
    {
        LinearLayout layout = (LinearLayout) findViewById(R.id.Manage_Contacts_Scroll_View_Layout);
        layout.removeAllViews();

        List<ContactProfile> contacts = UserProfile.PROFILE.GetContacts();
        Collections.sort(contacts);
        for (ContactProfile profile : contacts)
        {
            layout.addView(profile.CreateWidget(ManageContactsActivity.this));
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
}
