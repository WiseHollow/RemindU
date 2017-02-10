package net.johnbrooks.remindu.requests;

import android.support.v7.app.AlertDialog;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.activities.ManageContactsActivity;
import net.johnbrooks.remindu.schedulers.MasterScheduler;
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

public class DeleteContactRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/removeContact.php";
    private Map<String, String> params;

    public DeleteContactRequest(String email, String password, String targetID, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Request.Method.POST, REQUEST_URL, listener, null);
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

    private static Response.Listener<String> GetDeleteResponseListener(final ManageContactsActivity activity, final int target)
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
                        MasterScheduler.GetInstance(activity).Call();
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

    public static void SendRequest(final ManageContactsActivity activity, final int id)
    {
        if (!Network.IsConnected(activity)) { return; }

        Response.Listener<String> responseListener = GetDeleteResponseListener(activity, id);

        DeleteContactRequest request = new DeleteContactRequest(UserProfile.PROFILE.GetEmail(), UserProfile.PROFILE.GetPassword(), String.valueOf(id), responseListener);
        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }
}
