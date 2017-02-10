package net.johnbrooks.remindu.requests;

import android.app.Dialog;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.ForgotMyPasswordActivity;
import net.johnbrooks.remindu.activities.ManageContactsActivity;
import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class ForgotMyPasswordRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/forgotPassword.php";
    private Map<String, String> params;

    public ForgotMyPasswordRequest(String email, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("email", email);
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetResponseListener(final ForgotMyPasswordActivity activity, final String target)
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

                        Snackbar.make(activity.getCurrentFocus(), "Password reset code has been sent to the email: " + target, Snackbar.LENGTH_LONG)
                                .setAction("Action", null).show();

                        final Dialog dialog = new Dialog(activity);
                        dialog.setTitle("Confirm Reset Code");
                        dialog.setContentView(R.layout.dialog_forgot_my_password);
                        dialog.show();

                        final TextView input = (TextView) dialog.findViewById(R.id.editText_code);
                        dialog.findViewById(R.id.button_Confirm).setOnClickListener(new View.OnClickListener()
                        {
                            @Override
                            public void onClick(View v)
                            {
                                String inputString = input.getText().toString();
                                final int code = Integer.parseInt(inputString);

                                //TODO: Confirm that the code is correct.
                                ValidateCodeRequest.SendRequest(activity, target, code);
                            }
                        });
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

    public static void SendRequest(final ForgotMyPasswordActivity activity, final String email)
    {
        if (!Network.IsConnected(activity)) { return; }

        Response.Listener<String> responseListener = GetResponseListener(activity, email);

        // Send request to server for contact adding.
        ForgotMyPasswordRequest request = new ForgotMyPasswordRequest(email, responseListener);
        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }
}
