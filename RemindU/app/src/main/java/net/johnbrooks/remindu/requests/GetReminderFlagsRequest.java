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
import net.johnbrooks.remindu.util.ReminderFlag;
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

public class GetReminderFlagsRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/getReminderFlags.php";
    private Map<String, String> params;

    public GetReminderFlagsRequest(Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("user_id", String.valueOf(UserProfile.PROFILE.GetUserID()));
        params.put("password", PasswordHash.Hash(UserProfile.PROFILE.GetPassword()));
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetUpdateResponseListener()
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
                        String s = jsonResponse.getString("likes");
                        for (String element : s.split("&"))
                        {
                            String[] parts = element.split("-");
                            if (parts == null || "".equals(parts[0]))
                                continue;
                            Reminder reminder = UserProfile.PROFILE.GetReminder(Integer.parseInt(parts[0]));
                            if (reminder != null)
                            {
                                int o = Integer.parseInt(parts[1]);
                                Reminder.ReminderState state = Reminder.ReminderState.values()[o];
                                if (state != null)
                                {
                                    ReminderFlag flag = reminder.GetFlag(state);
                                    flag.SetLiked(true);
                                }
                            }
                        }
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

    public static void SendRequest()
    {
        if (!Network.IsConnected()) { return; }

        Response.Listener<String> responseListener = GetUpdateResponseListener();
        GetReminderFlagsRequest request = new GetReminderFlagsRequest(responseListener);
        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(UserAreaActivity.GetActivity());
        queue.add(request);
    }
}
