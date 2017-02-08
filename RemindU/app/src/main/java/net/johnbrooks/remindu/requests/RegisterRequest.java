package net.johnbrooks.remindu.requests;

import android.content.Intent;
import android.support.v7.app.AlertDialog;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.activities.LoginActivity;
import net.johnbrooks.remindu.activities.RegisterActivity;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.PasswordHash;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by John on 11/23/2016.
 */

public class RegisterRequest extends StringRequest
{
    private static String FullName;
    private static String Username;
    private static String Email;
    private static String Password;

    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/register.php";
    private Map<String, String> params;

    public RegisterRequest(String fullname, String username, String email, String password, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);

        params = new HashMap<>();
        password = PasswordHash.Hash(password);
        if (password == null)
            return;

        params.put("fullname", fullname);
        params.put("username", username);
        params.put("email", email);
        params.put("password", password);

        FullName = fullname;
        Username = username;
        Email = email;
        Password = password;
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetResponseListener(final RegisterActivity activity)
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

                    boolean usernameAvailable = jsonResponse.getBoolean("username_available");
                    boolean emailAvailable = jsonResponse.getBoolean("email_available");

                    // The server is able to tell us if the username and email is available.

                    if (success)
                    {
                        UserProfile.PROFILE.SaveCredentials(activity, Email, Password, FullName, Username, 0, 0, 0);

                        Intent intent = new Intent(activity, LoginActivity.class);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                    else
                    {
                        // Create a string with the proper error message and send to user.

                        String message = "Registration Failed!";
                        if (!usernameAvailable)
                            message+="\nUsername is not available.";
                        if (!emailAvailable)
                            message+="\nEmail is already in use.";

                        if (message == "Registration Failed!")
                            message+= "\nUnknown error. Please contact developers about unknown registration error.";

                        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
                        builder.setMessage(message)
                                .setNegativeButton("Retry", null)
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

    public static void SendRequest(RegisterActivity activity, final String fullname, final String username, final String email, final String password)
    {
        if (!Network.IsConnected(activity)) { return; }

        Response.Listener<String> listener = GetResponseListener(activity);

        RegisterRequest request = new RegisterRequest(fullname, username, email, password, listener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }
}
