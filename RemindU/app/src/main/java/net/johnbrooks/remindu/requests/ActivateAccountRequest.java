package net.johnbrooks.remindu.requests;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.activities.ActivateAccountActivity;
import net.johnbrooks.remindu.R;
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

public class ActivateAccountRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/activateAccount.php";
    private Map<String, String> params;

    private ActivateAccountRequest(final String code, final Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("user_id", String.valueOf(UserProfile.PROFILE.GetUserID()));
        params.put("code", code);
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetResponseListener(final ActivateAccountActivity activity)
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
                        //PullScheduler.Call();
                        MasterScheduler.GetInstance(activity).Call();
                        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                        dialog.setTitle("Activation")
                                .setMessage("Activation was successful!")
                                .setNegativeButton("Okay", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        if (activity != null)
                                        {
                                            activity.finish();
                                        }
                                    }
                                })
                                .create()
                                .show();
                    }
                    else
                    {
                        Log.d("SEVERE", message);
                        AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                        dialog.setTitle("Activation")
                                .setMessage("Invalid activation code.")
                                .setNegativeButton("Retry", new DialogInterface.OnClickListener()
                                {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {
                                        if (activity != null)
                                        {
                                            EditText et_code = (EditText) activity.findViewById(R.id.editText_activation_code);
                                            if (et_code == null)
                                                return;
                                            et_code.setText("");
                                        }
                                    }
                                })
                                .create()
                                .show();
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void SendRequest(final ActivateAccountActivity activity, final String code)
    {
        if (!Network.IsConnected(activity)) { return; }

        Response.Listener<String> listener = GetResponseListener(activity);
        ActivateAccountRequest request = new ActivateAccountRequest(code, listener);

        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }
}
