package net.johnbrooks.remindu.requests;

import android.app.Activity;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.PasswordHash;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class DeleteReminderRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/deleteReminder.php";
    private Map<String, String> params;

    public DeleteReminderRequest(int user_id, String password, int reminderID, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("user_id", String.valueOf(user_id));
        params.put("password", PasswordHash.Hash(password));
        params.put("reminder_id", String.valueOf(reminderID));
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetDeleteResponseListener(final Activity activity)
    {
        return new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                try
                {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");
                    String message = jsonResponse.getString("message");

                    Log.d("INFO", "Received response: " + success);

                    if (success)
                    {
                        MasterScheduler.GetInstance(activity).Call();
                    }
                    else
                    {
                        Log.d("ERROR", "Message: " + message);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void SendRequest(Reminder r)
    {
        if (!Network.IsConnected(UserAreaActivity.GetActivity()) || r.IsLocal()) { return; }

        Response.Listener<String> responseListener = GetDeleteResponseListener(UserAreaActivity.GetActivity());

        DeleteReminderRequest request = new DeleteReminderRequest(UserProfile.PROFILE.GetUserID(), UserProfile.PROFILE.GetPassword(), r.GetID(), responseListener);
        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(UserAreaActivity.GetActivity());
        queue.add(request);
    }
}
