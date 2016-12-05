package net.johnbrooks.remindu;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.util.AcceptedContactProfile;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.requests.LoginRequest;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.Reminder;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Set;

public class LoginActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        AttemptAutoLogin();

        final EditText etEmail = (EditText) findViewById(R.id.editText_Login_Email);
        final EditText etPassword = (EditText) findViewById(R.id.editText_Login_Password);
        final Button bLogin = (Button) findViewById(R.id.button_Login);
        final TextView tvRegister = (TextView) findViewById(R.id.textView_Register);

        //
        // What happens when the register button is clicked.
        //
        tvRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });

        //
        // This is what happens when login button is clicked.
        //

        bLogin.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String email = etEmail.getText().toString();
                final String password = etPassword.getText().toString();

                Response.Listener<String> responseListener = GetLoginResponseListener(email, password);

                LoginRequest request = new LoginRequest(email, password, responseListener);
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                queue.add(request);
            }
        });
    }

    //
    // Checks for saved login data, for instant login. Run this when the activity is created.
    //
    private void AttemptAutoLogin()
    {
        SharedPreferences sharedPref = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
        if (getIntent().getBooleanExtra("signOut", false) == true)
        {
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("email", "null");
            editor.putString("password", "null");
            editor.commit();
            getIntent().putExtra("SignOut", false);
            Log.d("INFO", "Removing saved sign in credentials. ");
            return;
        }

        final String email = sharedPref.getString("email", "null");
        final String password = sharedPref.getString("password", "null");

        if (!Network.IsConnected(LoginActivity.this) && AttemptLoadSavedProfile())
        {


            Intent intent = new Intent(LoginActivity.this, UserAreaActivity.class);
            LoginActivity.this.startActivity(intent);
            finish();

            return;
        }

        if (!email.equalsIgnoreCase("null") && !password.equalsIgnoreCase("null"))
        {
            Response.Listener<String> responseListener = GetLoginResponseListener(email, password);

            LoginRequest request = new LoginRequest(email, password, responseListener);
            RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
            queue.add(request);
        }
    }

    private boolean AttemptLoadSavedProfile()
    {
        SharedPreferences sharedPref = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
        final String email = sharedPref.getString("email", "null");
        final String password = sharedPref.getString("password", "null");
        final String fullname = sharedPref.getString("fullname", "null");
        final String username = sharedPref.getString("username", "null");
        final int id = sharedPref.getInt("id", 0);
        final boolean active = sharedPref.getBoolean("active", false);
        final int pointsTotal = sharedPref.getInt("pointsTotal", 0);
        final int pointsSent = sharedPref.getInt("pointsSent", 0);
        final int pointsReceived = sharedPref.getInt("pointsReceived", 0);
        final Set<String> contactsString = sharedPref.getStringSet("contacts", null);

        if (email.equalsIgnoreCase("null") || password.equalsIgnoreCase("null") || fullname.equalsIgnoreCase("null") ||
                username.equalsIgnoreCase("null") || id == 0 || email.equalsIgnoreCase("null"))
            return false;

        UserProfile.PROFILE = new UserProfile(id, (active == true) ? 1 : 0, fullname, username, email, password, pointsTotal, pointsReceived, pointsSent);
        for(String s : contactsString)
        {
            String[] element = s.split("%");
            int cID = Integer.parseInt(element[0]);
            String cEmail = element[1];
            String cUsername = element[2];
            String cFullName = element[3];
            String cContacts = element[4];

            if (cFullName.equalsIgnoreCase("null"))
                UserProfile.PROFILE.AddContact(new ContactProfile(cID, cEmail));
            else
                UserProfile.PROFILE.AddContact(new AcceptedContactProfile(cID, cEmail, cFullName, cUsername, cContacts));
        }

        return true;
    }

    @Deprecated
    private Response.Listener<String> GetLoginResponseListener(final String email, final String password)
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

                    if (success)
                    {
                        // If we successfully login, lets get all information passed from server.
                        final int id = jsonResponse.getInt("userID");
                        final int active = jsonResponse.getInt("active");

                        final String fullName = jsonResponse.getString("fullname");
                        final String email = jsonResponse.getString("email");
                        final String username = jsonResponse.getString("username");

                        final int pointsTotal = jsonResponse.getInt("pointsRemaining");
                        final int pointsSent = jsonResponse.getInt("pointsSent");
                        final int pointsReceived = jsonResponse.getInt("pointsReceived");

                        final String contacts = jsonResponse.getString("contacts");

                        // Using pulled information, we can create a profile for the user.

                        UserProfile.PROFILE = new UserProfile(id, active, fullName, username, email, password, pointsTotal, pointsReceived, pointsSent);

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
                                    UserProfile.PROFILE.AddContact(new AcceptedContactProfile(Integer.parseInt(key[1]), key[2], key[3], key[4], key[5]));
                                else if (key.length == 5)
                                    UserProfile.PROFILE.AddContact(new AcceptedContactProfile(Integer.parseInt(key[1]), key[2], key[3], key[4], ""));

                            }


                        }

                        //
                        // Save login data for instant login next time
                        //

                        SharedPreferences sharedPref = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        editor.putString("email", email);
                        editor.putString("password", password);
                        editor.putString("fullname", fullName);
                        editor.putString("username", username);
                        editor.putInt("id", id);
                        editor.putBoolean("active", (active > 0) ? true : false);
                        editor.putInt("pointsTotal", pointsTotal);
                        editor.putInt("pointsSent", pointsSent);
                        editor.putInt("pointsReceived", pointsReceived);
                        editor.putStringSet("contacts", UserProfile.PROFILE.GetContactStringSet());

                        editor.commit();

                        //
                        // Let's now to to the User Area now that we have logged in.
                        //

                        Intent intent = new Intent(LoginActivity.this, UserAreaActivity.class);

                        LoginActivity.this.startActivity(intent);
                        finish();
                    }
                    else
                    {
                        AlertDialog.Builder errorDialog = new AlertDialog.Builder(LoginActivity.this);
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

        return responseListener;
    }


}
