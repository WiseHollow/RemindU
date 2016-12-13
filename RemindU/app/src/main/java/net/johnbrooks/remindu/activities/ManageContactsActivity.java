package net.johnbrooks.remindu.activities;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.requests.AddContactRequest;
import net.johnbrooks.remindu.schedulers.PullScheduler;
import net.johnbrooks.remindu.schedulers.UpdateManageContactsScheduler;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

public class ManageContactsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);

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

                final EditText et_Email = (EditText) dialog.findViewById(R.id.editText_AddContact_Email);
                Button button = (Button) dialog.findViewById(R.id.button_AddContact_Submit);

                button.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View view)
                    {
                        String email = et_Email.getText().toString();

                        Log.d("INFO", "Requesting that contact email=" + email + " be added.");
                        Response.Listener<String> responseListener = GetResponseListener(email);

                        // Send request to server for contact adding.
                        AddContactRequest request = new AddContactRequest(UserProfile.PROFILE.GetEmail(), UserProfile.PROFILE.GetPassword(), email, responseListener);
                        RequestQueue queue = Volley.newRequestQueue(ManageContactsActivity.this);
                        queue.add(request);

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

        //TODO: Make work with custom avatars.
        // Right now we only use a default avatar for each contact.
        Bitmap bDefaultAvatar = BitmapFactory.decodeResource( getResources(), R.drawable.user_48 );
        // Lets get our delete image.
        Bitmap bDelete = BitmapFactory.decodeResource( getResources(), R.drawable.delete_48);
        //
        // For each profile in contacts, lets make a textview for that profile.
        //
        for (ContactProfile profile : UserProfile.PROFILE.GetContacts())
        {
            layout.addView(profile.CreateWidget(ManageContactsActivity.this));

            /*final int targetID = profile.GetID();
            final String targetEmail = profile.GetEmail();

            TextView view = new TextView(ManageContactsActivity.this);
            view.setMovementMethod(LinkMovementMethod.getInstance());

            // Use a spannablestringbuilder to keep control over the color, font size, and images in the string.
            SpannableStringBuilder builder = new SpannableStringBuilder();
            builder.append("_ _ ");
            builder.append(profile.GetFullName());
            if (!profile.IsContact())
            {
                builder.append(" - Waiting...");
            }
            else
            {
                // We shall make the text black if the contact is mutual.
                builder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            // Insert the images in required positions.
            builder.setSpan(new ImageSpan(view.getContext(), bDefaultAvatar), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            builder.setSpan(new ImageSpan(view.getContext(), bDelete), 2, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            //
            // Span Delete will be placed on top of the image for delete.
            // Using it will send a request to the server to remove a contact.
            //
            ClickableSpan spanDelete = new ClickableSpan()
            {
                @Override
                public void onClick(View view)
                {
                    if (targetID == -1)
                    {
                        //UserProfile.PROFILE.RemoveContact(targetEmail);
                        //UpdateContactsList();
                        return;
                    }

                    //TODO: Pull data from server
                    Log.d("INFO", "Requesting that contact id=" + targetID + " be removed.");
                    Response.Listener<String> responseListener = GetResponseListener(targetID);

                    DeleteContactRequest request = new DeleteContactRequest(UserProfile.PROFILE.GetEmail(), UserProfile.PROFILE.GetPassword(), String.valueOf(targetID), responseListener);
                    RequestQueue queue = Volley.newRequestQueue(ManageContactsActivity.this);
                    queue.add(request);

                    //TODO: Refresh contents...
                }
            };
            builder.setSpan(spanDelete, 2, 3, Spanned.SPAN_INCLUSIVE_INCLUSIVE);

            view.setPadding(5, 15, 5, 15);
            view.setTextSize(16f);
            view.setText(builder);

            layout.addView(view);*/
        }
    }

    private Response.Listener<String> GetResponseListener(final int target)
{
    Response.Listener<String> responseListener = new Response.Listener<String>()
    {
        @Override
        public void onResponse(String response)
        {
            try
            {
                JSONObject jsonResponse = new JSONObject(response);
                boolean success = jsonResponse.getBoolean("success");

                Log.d("INFO", "Received response: " + success);

                if (success)
                {
                    UserProfile.PROFILE.RemoveContact(target);
                    PullScheduler.Call();
                    UpdateContactsList();
                }
                else
                {
                    AlertDialog.Builder errorDialog = new AlertDialog.Builder(ManageContactsActivity.this);
                    errorDialog.setMessage("Server not reached. Error!")
                            .setNegativeButton("Close", null)
                            .create()
                            .show();
                }
            } catch (JSONException e)
            {
                e.printStackTrace();
            }
        }
    };

    return responseListener;
}

    private Response.Listener<String> GetResponseListener(final String target)
    {
        Response.Listener<String> responseListener = new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    Log.d("INFO", "Received response: " + success);

                    if (success)
                    {
                        Snackbar.make(findViewById(R.id.content_manage_contacts), "Added user of email: " + target, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        UserProfile.PROFILE.AddContact(new ContactProfile(-1, target));
                        UpdateContactsList();
                        PullScheduler.Call();
                    }
                    else
                    {
                        Snackbar.make(findViewById(R.id.content_manage_contacts), "Email is not registered, or are already added.", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        };

        return responseListener;
    }
}
