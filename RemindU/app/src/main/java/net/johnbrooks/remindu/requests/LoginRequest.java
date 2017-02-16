package net.johnbrooks.remindu.requests;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.activities.LoginActivity;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.util.AcceptedContactProfile;
import net.johnbrooks.remindu.util.ContactProfile;
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

public class LoginRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/login.php";
    private Map<String, String> params;

    public LoginRequest(String email, String password, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Request.Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();

        password = PasswordHash.Hash(password);
        if (password == null)
            return;

        params.put("email", email);
        params.put("password", password);
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetLoginResponseListener(final LoginActivity activity, final String password)
    {
        return new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response)
            {
                if (UserAreaActivity.GetActivity() != null)
                    return;

                try
                {
                    JSONObject jsonResponse = new JSONObject(response);
                    boolean success = jsonResponse.getBoolean("success");

                    if (success)
                    {
                        // If we successfully login, lets get all information passed from server.
                        final int id = jsonResponse.getInt("userID");
                        final int active = jsonResponse.getInt("active");

                        final String fullName = jsonResponse.getString("fullname");
                        final String email = jsonResponse.getString("email");
                        final String username = jsonResponse.getString("username");

                        final int coins = jsonResponse.getInt("coins");

                        final String contacts = jsonResponse.getString("contacts");
                        final String avatarID = jsonResponse.getString("avatar");

                        // Using pulled information, we can create a profile for the user.

                        UserProfile.PROFILE = new UserProfile(id, active, fullName, username, email, password, coins, avatarID);
                        UserProfile.PROFILE.LoadRemindersFromFile();
                        UserProfile.PROFILE.LoadReminderFlagsFromFile();
                        UserProfile.PROFILE.LoadReminderIgnoresFromFile();

                        // Next, lets make sense of the contacts string given by the server.
                        // It will pass either a AcceptedContactProfile info, or just limited information used to make a ContactProfile.

                        for (String contact : contacts.split("&"))
                        {
                            if (contact == "" || contact == " ")
                                continue;
                            String[] key = contact.split("%");
                            if (key[0].equalsIgnoreCase("0")) // 0 = The contact doesn't have us added.
                            {
                                UserProfile.PROFILE.AddContact(new ContactProfile(Integer.parseInt(key[1]), key[2]));
                            }
                            else if (key[0].equalsIgnoreCase("1")) // 1 = mutually contacts.
                            {
                                if (key.length >= 7)
                                    UserProfile.PROFILE.AddContact(new AcceptedContactProfile(Integer.parseInt(key[1]), key[2], key[3], key[4], key[5], key[6]));
                                else if (key.length == 6)
                                    UserProfile.PROFILE.AddContact(new AcceptedContactProfile(Integer.parseInt(key[1]), key[2], key[3], key[4], "", key[6]));

                            }


                        }

                        //
                        // Save login data for instant login next time
                        //

                        UserProfile.PROFILE.SaveCredentials(activity, email, password, fullName, username, id, active, coins);

                        //
                        // Let's now to to the User Area now that we have logged in.
                        //

                        Intent intent = new Intent(activity, UserAreaActivity.class);

                        activity.progressBar.setVisibility(View.INVISIBLE);
                        activity.startActivity(intent);
                        activity.finish();
                    }
                    else
                    {
                        activity.progressBar.setVisibility(View.INVISIBLE);
                        AlertDialog.Builder errorDialog = new AlertDialog.Builder(activity);
                        errorDialog.setMessage("Login credential error.")
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

    private static Response.Listener<String> GetLoginResponseListener(final Service service, final String password)
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

                    if (success)
                    {
                        // If we successfully login, lets get all information passed from server.
                        final int id = jsonResponse.getInt("userID");
                        final int active = jsonResponse.getInt("active");

                        final String fullName = jsonResponse.getString("fullname");
                        final String email = jsonResponse.getString("email");
                        final String username = jsonResponse.getString("username");

                        final int coins = jsonResponse.getInt("coins");

                        final String likes = jsonResponse.getString("likes");
                        final String contacts = jsonResponse.getString("contacts");
                        final String avatarID = jsonResponse.getString("avatar");

                        // Using pulled information, we can create a profile for the user.

                        UserProfile.PROFILE = new UserProfile(id, active, fullName, username, email, password, coins, avatarID);

                        // Next, lets make sense of the contacts string given by the server.
                        // It will pass either a AcceptedContactProfile info, or just limited information used to make a ContactProfile.

                        for (String contact : contacts.split("&"))
                        {
                            if (contact == "" || contact == " ")
                                continue;
                            String[] key = contact.split("%");
                            if (key[0].equalsIgnoreCase("0")) // 0 = The contact doesn't have us added.
                            {
                                UserProfile.PROFILE.AddContact(new ContactProfile(Integer.parseInt(key[1]), key[2]));
                            }
                            else if (key[0].equalsIgnoreCase("1")) // 1 = mutually contacts.
                            {
                                if (key.length >= 6)
                                    UserProfile.PROFILE.AddContact(new AcceptedContactProfile(Integer.parseInt(key[1]), key[2], key[3], key[4], key[5], key[6]));
                                else if (key.length == 5)
                                    UserProfile.PROFILE.AddContact(new AcceptedContactProfile(Integer.parseInt(key[1]), key[2], key[3], key[4], "", key[6]));

                            }


                        }

                        UserProfile.PROFILE.LoadRemindersFromFile();
                        UserProfile.PROFILE.LoadReminderFlagsFromFile();
                        UserProfile.PROFILE.LoadReminderIgnoresFromFile();

                        //
                        // Save login data for instant login next time
                        //

                        SharedPreferences sharedPref = service.getSharedPreferences("profile", Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("email", email);
                        editor.putString("password", password);
                        editor.putString("fullname", fullName);
                        editor.putString("username", username);
                        editor.putInt("id", id);
                        editor.putBoolean("active", (active > 0) ? true : false);
                        editor.putInt("coins", coins);
                        editor.putStringSet("contacts", UserProfile.PROFILE.GetContactStringSet());
                        editor.putString("avatar", UserProfile.PROFILE.GetAvatarID());

                        editor.commit();
                    }
                } catch (JSONException e)
                {
                    e.printStackTrace();
                }
            }
        };
    }

    public static void SendRequest(final LoginActivity activity, final String email, final String password)
    {
        if (!Network.IsConnected()) { return; }

        Response.Listener<String> responseListener = GetLoginResponseListener(activity, password);

        LoginRequest request = new LoginRequest(email, password, responseListener);
        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }
}
