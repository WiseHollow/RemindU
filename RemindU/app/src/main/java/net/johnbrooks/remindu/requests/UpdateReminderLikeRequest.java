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

public class UpdateReminderLikeRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/likeReminderState.php";
    private Map<String, String> params;

    public UpdateReminderLikeRequest(Reminder reminder, Reminder.ReminderState state, boolean liked, Response.Listener<String> listener)
    {
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("user_id", String.valueOf(UserProfile.PROFILE.GetUserID()));
        params.put("password", PasswordHash.Hash(UserProfile.PROFILE.GetPassword()));
        params.put("reminder_id", String.valueOf(reminder.GetID()));
        params.put("state_id", String.valueOf(state.ordinal()));
        params.put("liked", liked ? "1" : "0");
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

                    Log.d(UpdateReminderLikeRequest.class.getSimpleName(), "Received response: " + message);

                    if (success)
                    {
                        //TODO: Remove call. Shouldn't need it in real-world experience.
                        //MasterScheduler.GetInstance(activity).Call();
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

    public static void SendRequest(final ReminderFlag flag)
    {
        if (!Network.IsConnected()) { return; }

        Response.Listener<String> responseListener = GetUpdateResponseListener(UserAreaActivity.GetActivity());
        UpdateReminderLikeRequest request = new UpdateReminderLikeRequest(flag.GetReminder(), flag.GetState(), flag.IsLiked(), responseListener);
        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(UserAreaActivity.GetActivity());
        queue.add(request);
    }
}
