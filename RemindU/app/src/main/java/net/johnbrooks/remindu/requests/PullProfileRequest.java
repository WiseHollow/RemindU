package net.johnbrooks.remindu.requests;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.activities.ActivateAccountActivity;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.schedulers.ShowCoinGainScheduler;
import net.johnbrooks.remindu.util.AcceptedContactProfile;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by John on 11/23/2016.
 */

public class PullProfileRequest extends StringRequest
{
    private static final String REQUEST_URL = "http://johnbrooks.net/remindu/scripts/login.php";
    private Map<String, String> params;

    public PullProfileRequest(String email, String password, Response.Listener<String> listener)
    {
        //TODO: Give error listener instead of null
        super(Method.POST, REQUEST_URL, listener, null);
        params = new HashMap<>();
        params.put("email", email);
        params.put("password", password);
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    private static Response.Listener<String> GetPullResponseListener()
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
                        final int id = jsonResponse.getInt("userID");
                        final int active = jsonResponse.getInt("active");

                        final String fullName = jsonResponse.getString("fullname");
                        final String email = jsonResponse.getString("email");
                        final String username = jsonResponse.getString("username");

                        final int coins = jsonResponse.getInt("coins");

                        final String contacts = jsonResponse.getString("contacts");

                        if (UserProfile.PROFILE != null && coins != UserProfile.PROFILE.GetCoins())
                        {
                            ShowCoinGainScheduler.Initialize();
                        }

                        if (UserProfile.PROFILE == null)
                            UserProfile.PROFILE = new UserProfile(id, active, fullName, username, email, UserProfile.PROFILE.GetPassword(), coins);
                        else
                            UserProfile.PROFILE.Update(id, active, fullName, username, email, UserProfile.PROFILE.GetPassword(), coins);
                        UserProfile.PROFILE.GetContacts().clear();
                        for (String contact : contacts.split("&"))
                        {
                            if (contact == "" || contact == " ")
                                continue;
                            String[] key = contact.split("%");
                            if (key[0].equalsIgnoreCase("0"))
                            {
                                UserProfile.PROFILE.AddContact(new ContactProfile(Integer.parseInt(key[1]), key[2]));
                            }
                            else if (key[0].equalsIgnoreCase("1"))
                            {
                                if (key.length >= 6)
                                    UserProfile.PROFILE.AddContact(new AcceptedContactProfile(Integer.parseInt(key[1]), key[2], key[3], key[4], key[5]));
                                else if (key.length == 5)
                                    UserProfile.PROFILE.AddContact(new AcceptedContactProfile(Integer.parseInt(key[1]), key[2], key[3], key[4], ""));
                            }


                        }

                        if (active != 1 && !ActivateAccountActivity.IsOpen())
                        {
                            Intent activateIntent = new Intent(UserAreaActivity.GetActivity(), ActivateAccountActivity.class);
                            UserAreaActivity.GetActivity().startActivity(activateIntent);
                        }
                    }
                    else
                    {
                        Log.d("SEVERE", "Profile Pull error.");
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
        Response.Listener<String> profileResponseListener = GetPullResponseListener();

        LoginRequest request = new LoginRequest(UserProfile.PROFILE.GetEmail(), UserProfile.PROFILE.GetPassword(), profileResponseListener);
        RequestQueue queue = Volley.newRequestQueue(activity);
        queue.add(request);
    }
}
