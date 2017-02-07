package net.johnbrooks.remindu.requests;

import android.app.Activity;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class UpdateReminderRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/updateReminder.php";
    private Map<String, String> params;

    public UpdateReminderRequest(Reminder reminder, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("user_id", String.valueOf(UserProfile.PROFILE.GetUserID()));
        params.put("password", UserProfile.PROFILE.GetPassword());
        params.put("reminder_id", String.valueOf(reminder.GetID()));
        params.put("state", String.valueOf(reminder.GetStateOrdinal()));

        final Date now = new Date();
        final DateFormat formatter = new SimpleDateFormat("MM-dd-yyyy HH:mm:ss");
        final String dateString = formatter.format(now);
        params.put("date", dateString);
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetUpdateResponseListener(final Activity activity)
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

    public static void SendRequest(final Reminder r)
    {
        if (!Network.IsConnected(UserAreaActivity.GetActivity())) { return; }

        Response.Listener<String> responseListener = GetUpdateResponseListener(UserAreaActivity.GetActivity());
        UpdateReminderRequest request = new UpdateReminderRequest(r, responseListener);
        RequestQueue queue = Volley.newRequestQueue(UserAreaActivity.GetActivity());
        queue.add(request);
    }
}
