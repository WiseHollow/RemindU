package net.johnbrooks.remindu.requests;

import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.activities.ForgotMyPasswordActivity;
import net.johnbrooks.remindu.activities.ResetMyPasswordActivity;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.PasswordHash;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class ResetPasswordRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/resetPassword.php";
    private Map<String, String> params;

    public ResetPasswordRequest(final String email, final int code, final String password, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("email", email);
        params.put("code", String.valueOf(code));
        params.put("password", PasswordHash.Hash(password));
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetResponseListener(final ResetMyPasswordActivity activity, final String email, final int code)
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

                    Log.d("INFO", "Received response: " + success);

                    if (success)
                    {
                        if (activity == null)
                            return;

                        Snackbar.make(activity.getCurrentFocus(), "Your password has been reset!", Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        activity.finish();
                    }
                    else
                    {
                        Snackbar.make(activity.getCurrentFocus(), message, Snackbar.LENGTH_LONG)
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

    public static void SendRequest(final ResetMyPasswordActivity activity, final String email, final int code, final String password)
    {
        if (!Network.IsConnected(activity)) { return; }

        Response.Listener<String> responseListener = GetResponseListener(activity, email, code);

        // Send request to server for contact adding.
        ResetPasswordRequest request = new ResetPasswordRequest(email, code, password, responseListener);
        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }
}
