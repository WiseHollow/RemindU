package net.johnbrooks.remindu.requests;

import android.app.Dialog;
import android.content.Intent;
import android.support.design.widget.Snackbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.ForgotMyPasswordActivity;
import net.johnbrooks.remindu.activities.ResetMyPasswordActivity;
import net.johnbrooks.remindu.util.Network;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ieatl on 11/29/2016.
 */

public class ValidateCodeRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/validateCode.php";
    private Map<String, String> params;

    public ValidateCodeRequest(final String email, final int code, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("email", email);
        params.put("code", String.valueOf(code));
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetResponseListener(final ForgotMyPasswordActivity activity, final String email, final int code)
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

                        /*final Dialog dialog = new Dialog(activity);
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
                                int code = Integer.parseInt(inputString);
                            }
                        });*/

                        Intent intent = new Intent(activity, ResetMyPasswordActivity.class);
                        intent.putExtra("email", email);
                        intent.putExtra("code", code);
                        activity.startActivity(intent);
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

    public static void SendRequest(final ForgotMyPasswordActivity activity, final String email, final int code)
    {
        if (!Network.IsConnected(activity)) { return; }

        Response.Listener<String> responseListener = GetResponseListener(activity, email, code);

        // Send request to server for contact adding.
        ValidateCodeRequest request = new ValidateCodeRequest(email, code, responseListener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }
}
