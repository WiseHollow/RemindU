package net.johnbrooks.remindu.requests;

import android.app.Activity;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class UpdateSettingsRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/updateSettings.php";
    private Map<String, String> params;

    public UpdateSettingsRequest(boolean receiveEmails, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("user_id", String.valueOf(UserProfile.PROFILE.GetUserID()));
        params.put("password", UserProfile.PROFILE.GetPassword());
        params.put("receive_emails", (receiveEmails) ? "1" : "0");
        params.put("avatar", UserProfile.PROFILE.GetAvatarID());
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetResponseListener()
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

                    if (!success)
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

    public static void SendRequest(final Activity activity)
    {
        boolean receiveEmails;
        SharedPreferences sharedPrefs = (activity == null) ? PreferenceManager.getDefaultSharedPreferences(UserAreaActivity.GetActivity()) : PreferenceManager.getDefaultSharedPreferences(activity);
        receiveEmails = !sharedPrefs.getBoolean("receive_emails_switch", true);

        Response.Listener<String> responseListener = GetResponseListener();
        UpdateSettingsRequest request = new UpdateSettingsRequest(receiveEmails, responseListener);
        RequestQueue queue = (activity == null) ? Volley.newRequestQueue(UserAreaActivity.GetActivity()) : Volley.newRequestQueue(activity);
        queue.add(request);
    }
}
