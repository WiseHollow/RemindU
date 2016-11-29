package net.johnbrooks.remindu;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.requests.DeleteContactRequest;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

public class ManageContactsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        LinearLayout layout = (LinearLayout) findViewById(R.id.Manage_Contacts_Scroll_View_Layout);

        Bitmap bDefaultAvatar = BitmapFactory.decodeResource( getResources(), R.drawable.user_48 );
        Bitmap bDelete = BitmapFactory.decodeResource( getResources(), R.drawable.delete_48);

        //
        // For each profile in contacts, lets make a textview for that profile.
        //
        for (ContactProfile profile : UserProfile.PROFILE.GetContacts())
        {
            final int targetID = profile.GetID();

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
                builder.setSpan(new ForegroundColorSpan(Color.BLACK), 0, builder.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
            builder.setSpan(new ImageSpan(view.getContext(), bDefaultAvatar), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            builder.setSpan(new ImageSpan(view.getContext(), bDelete), 2, 3, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            ClickableSpan spanDelete = new ClickableSpan()
            {
                @Override
                public void onClick(View view)
                {
                    //TODO: Refresh data from server
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

            layout.addView(view);
        }
    }

    private Response.Listener<String> GetResponseListener(final int targetID)
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

                    if (success)
                    {
                        UserProfile.PROFILE.RemoveContact(targetID);
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
}
