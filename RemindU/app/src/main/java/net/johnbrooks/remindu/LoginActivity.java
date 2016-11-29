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
import net.johnbrooks.remindu.util.LoginRequest;
import net.johnbrooks.remindu.util.UserProfile;

import org.json.JSONException;
import org.json.JSONObject;

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

        tvRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent registerIntent = new Intent(LoginActivity.this, RegisterActivity.class);
                LoginActivity.this.startActivity(registerIntent);
            }
        });

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

    private void AttemptAutoLogin()
    {
        SharedPreferences sharedPref = LoginActivity.this.getPreferences(Context.MODE_PRIVATE);
        final String email = sharedPref.getString("email", "null");
        final String password = sharedPref.getString("password", "null");

        if (email != "null" && password != "null")
        {
            Response.Listener<String> responseListener = GetLoginResponseListener(email, password);

            LoginRequest request = new LoginRequest(email, password, responseListener);
            RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
            queue.add(request);
        }
    }

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
                        final int id = jsonResponse.getInt("userID");
                        final int active = jsonResponse.getInt("active");

                        final String fullName = jsonResponse.getString("fullname");
                        final String email = jsonResponse.getString("email");
                        final String username = jsonResponse.getString("username");

                        final int pointsTotal = jsonResponse.getInt("pointsRemaining");
                        final int pointsGiven = jsonResponse.getInt("pointsSent");
                        final int pointsReceived = jsonResponse.getInt("pointsReceived");

                        final String contacts = jsonResponse.getString("contacts");

                        UserProfile.PROFILE = new UserProfile(id, active, fullName, username, email, password, pointsTotal, pointsReceived, pointsGiven);
                        for (String contact : contacts.split("&"))
                        {
                            Log.d("INFO", contact);
                            if (contact == "" || contact == " ")
                                continue;
                            String[] key = contact.split("%");
                            /*if (key.length < 4)
                            {
                                Log.d("INFO", "Contact request is still pending.");
                                continue;
                            }*/

                            if (key[0] == "0")
                            {
                                UserProfile.PROFILE.AddContact(new ContactProfile(Integer.parseInt(key[1]), key[2]));
                            }
                            else if (key[0] == "1")
                            {
                                if (key.length == 5)
                                    UserProfile.PROFILE.AddContact(new AcceptedContactProfile(Integer.parseInt(key[1]), key[2], key[3], key[4], key[5]));
                                else if (key.length == 4)
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
                        editor.commit();

                        //
                        //
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
