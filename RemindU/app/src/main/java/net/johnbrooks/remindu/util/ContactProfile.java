package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.support.v7.app.AlertDialog;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.ManageContactsActivity;
import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.requests.DeleteContactRequest;
import net.johnbrooks.remindu.schedulers.PullScheduler;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by John on 11/29/2016.
 */

public class ContactProfile
{
    public static ContactProfile GetProfile(int user_id)
    {
        for (ContactProfile cp : UserProfile.PROFILE.GetContacts())
            if (cp.GetID() == user_id)
                return cp;
        return null;
    }

    private int ID;
    private String Email;

    public ContactProfile(int id, String email)
    {
        ID = id;
        Email = email;
    }
    public final int GetID() { return ID; }
    public final String GetEmail() { return Email; }
    public String GetUsername() { return "null"; }
    public String GetFullName() { return "null"; }
    public String GetDisplayName() { return Email; }
    public String GetContacts() { return "null"; }
    public boolean IsContact()
    {
        if (this.getClass() == AcceptedContactProfile.class)
            return true;
        else
            return false;
    }
    public LinearLayout CreateWidget(final ManageContactsActivity activity)
    {
        // Right now we only use a default avatar for each contact.
        Bitmap bDefaultAvatar = BitmapFactory.decodeResource( activity.getResources(), R.drawable.user_48 );
        // Lets get our delete image.
        Bitmap bDelete = BitmapFactory.decodeResource( activity.getResources(), R.drawable.delete_48);
        LinearLayout layout = new LinearLayout(activity);
        layout.setLayoutParams(new LinearLayout.LayoutParams
                (
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1f
                ));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        SpannableStringBuilder spannableStringLeft = new SpannableStringBuilder();
        SpannableStringBuilder spannableStringRight = new SpannableStringBuilder();

        String line1 = "_ " + GetDisplayName();
        if (!IsContact())
            line1 += " Waiting...";
        spannableStringLeft.append(line1);
        TextView textView = new TextView(activity);
        layout.addView(textView);
        textView.setTextSize(18f);
        textView.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams
                (
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT, 0.85f
                );
        textView.setLayoutParams(textViewParams);
        spannableStringLeft.setSpan(new ImageSpan(textView.getContext(), bDefaultAvatar), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(spannableStringLeft);

        TextView button = new TextView(activity);
        button.setMovementMethod(LinkMovementMethod.getInstance());
        layout.addView(button);
        spannableStringRight.append("X");
        spannableStringRight.setSpan(new ImageSpan(button.getContext(), bDelete), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams
                (
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT, 0.15f
                );

        button.setTextColor(Color.BLACK);
        button.setLayoutParams(buttonParams);

        ClickableSpan spanDelete = new ClickableSpan()
        {
            @Override
            public void onClick(View view)
            {
                if (GetID() == -1)
                {
                    return;
                }

                //TODO: Pull data from server
                Log.d("INFO", "Requesting that contact id=" + GetID() + " be removed.");
                Response.Listener<String> responseListener = GetDeleteResponseListener(activity, GetID());

                DeleteContactRequest request = new DeleteContactRequest(UserProfile.PROFILE.GetEmail(), UserProfile.PROFILE.GetPassword(), String.valueOf(GetID()), responseListener);
                RequestQueue queue = Volley.newRequestQueue(activity);
                queue.add(request);

                //TODO: Refresh contents...
            }
        };
        spannableStringRight.setSpan(spanDelete, 0, 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        button.setText(spannableStringRight);

        return layout;
    }
    @Override
    public String toString()
    {
        return GetID() + "%" + GetEmail() + "%" + GetUsername() + "%" + GetFullName() + "%" + GetContacts();
    }
    private Response.Listener<String> GetDeleteResponseListener(final ManageContactsActivity activity, final int target)
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
                        activity.UpdateContactsList();
                    }
                    else
                    {
                        AlertDialog.Builder errorDialog = new AlertDialog.Builder(activity);
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
