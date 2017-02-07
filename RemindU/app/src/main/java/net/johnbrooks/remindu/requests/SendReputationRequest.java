package net.johnbrooks.remindu.requests;

import android.app.Activity;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class SendReputationRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/sendCoins.php";
    private Map<String, String> params;

    private SendReputationRequest(final int user_id_from, final int user_id_to, final String password, final int coins, final Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("user_id_from", String.valueOf(user_id_from));
        params.put("user_id_to", String.valueOf(user_id_to));
        params.put("password", password);
        params.put("coins", String.valueOf(coins));
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

                    if (success)
                    {
                        MasterScheduler.GetInstance().Call();
                    }
                    else
                    {
                        Log.d("SEVERE", message);
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void SendRequest(final Activity activity, final int user_id_to, final int coins)
    {
        if (!Network.IsConnected(activity)) { return; }

        Response.Listener<String> listener = GetResponseListener();
        SendReputationRequest request = new SendReputationRequest(UserProfile.PROFILE.GetUserID(), user_id_to, UserProfile.PROFILE.GetPassword(), coins, listener);

        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }
}
