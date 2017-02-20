package net.johnbrooks.remindu.requests;

import android.support.design.widget.Snackbar;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.ManageContactsActivity;
import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.PasswordHash;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class AddContactRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/addContact.php";
    private Map<String, String> params;

    public AddContactRequest(String email, String password, String targetID, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("email", email);
        params.put("password", PasswordHash.Hash(password));
        params.put("target", targetID);
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetResponseListener(final ManageContactsActivity activity, final String target)
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
                    String message = jsonResponse.getString("message");

                    Log.d(AddContactRequest.class.getSimpleName(), "Received response: " + message);

                    if (success)
                    {
                        Snackbar.make(activity.findViewById(R.id.content_manage_contacts), "Added user of email: " + target, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        UserProfile.PROFILE.AddContact(new ContactProfile(-1, target));
                        activity.UpdateContactsList();
                        MasterScheduler.GetInstance(activity).Call();
                    }
                    else
                    {
                        Snackbar.make(activity.findViewById(R.id.content_manage_contacts), "Email is not registered, or are already added.", Snackbar.LENGTH_LONG)
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

    public static void SendRequest(final ManageContactsActivity activity, final String email)
    {
        if (!Network.IsConnected() || activity == null) { return; }

        Response.Listener<String> responseListener = GetResponseListener(activity, email);

        // Send request to server for contact adding.
        AddContactRequest request = new AddContactRequest(UserProfile.PROFILE.GetEmail(), UserProfile.PROFILE.GetPassword(), email, responseListener);
        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }
}
