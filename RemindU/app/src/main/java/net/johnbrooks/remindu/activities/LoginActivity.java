package net.johnbrooks.remindu.activities;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.util.AcceptedContactProfile;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.requests.LoginRequest;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.UserProfile;

import java.text.ParseException;
import java.util.Set;

public class LoginActivity extends AppCompatActivity
{
    public ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        MasterScheduler.GetInstance(LoginActivity.this);

        progressBar = (ProgressBar) findViewById(R.id.login_progress_bar);
        progressBar.setVisibility(View.INVISIBLE);

        AttemptAutoLogin();

        final EditText etEmail = (EditText) findViewById(R.id.editText_Login_Email);
        final EditText etPassword = (EditText) findViewById(R.id.editText_Login_Password);
        final Button bLogin = (Button) findViewById(R.id.button_Login);
        final TextView tvRegister = (TextView) findViewById(R.id.textView_Register);
        final TextView tvForgotPassword = (TextView) findViewById(R.id.textView_Forgot_Password);

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
                finish();
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

                progressBar.setVisibility(View.VISIBLE);
                LoginRequest.SendRequest(LoginActivity.this, email, password);
            }
        });

        tvForgotPassword.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(LoginActivity.this, ForgotMyPasswordActivity.class);
                startActivity(intent);
            }
        });
    }

    //
    // Checks for saved login data, for instant login. Run this when the activity is created.
    //

    private void AttemptAutoLogin()
    {
        SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences("profile", Context.MODE_PRIVATE);

        if (getIntent().getBooleanExtra("signOut", false) == true)
        {
            UserProfile.CleanupLocalFiles();

            return;
        }

        final String email = sharedPref.getString("email", "null");
        final String password = sharedPref.getString("password", "null");

        AttemptLoadSavedProfile();

        if (!Network.IsConnected())
        {
            Intent intent = new Intent(LoginActivity.this, UserAreaActivity.class);
            LoginActivity.this.startActivity(intent);
            finish();
        }
        else if (!email.equalsIgnoreCase("null") && !password.equalsIgnoreCase("null"))
        {
            progressBar.setVisibility(View.VISIBLE);
            LoginRequest.SendRequest(LoginActivity.this, email, password);
        }
    }

    public static boolean AttemptLoadSavedProfile(Service service)
    {
        SharedPreferences sharedPref = service.getSharedPreferences("profile", Context.MODE_PRIVATE);
        final String email = sharedPref.getString("email", "null");
        final String password = sharedPref.getString("password", "null");
        final String fullname = sharedPref.getString("fullname", "null");
        final String username = sharedPref.getString("username", "null");
        final int id = sharedPref.getInt("id", 0);
        final boolean active = sharedPref.getBoolean("active", false);
        final int coins = sharedPref.getInt("coins", 0);
        final Set<String> contactsString = sharedPref.getStringSet("contacts", null);
        final String avatarID = sharedPref.getString("avatar", "default");

        if (email.equalsIgnoreCase("null") || password.equalsIgnoreCase("null") || fullname.equalsIgnoreCase("null") ||
                username.equalsIgnoreCase("null") || id == 0 || email.equalsIgnoreCase("null"))
        {
            Log.d("INFO", "COULD NOT LOAD SAVED PROFILE");
            return false;
        }

        UserProfile.PROFILE = new UserProfile(id, (active == true) ? 1 : 0, fullname, username, email, password, coins, avatarID);

        if (contactsString != null)
        {
            for(String s : contactsString)
            {
                try
                {
                    String[] element = s.split("%");
                    int cID = Integer.parseInt(element[0]);
                    String cEmail = element[1];
                    String cUsername = element[2];
                    String cFullName = element[3];
                    String cContacts = element[4];
                    String cAvatarID = element[5];
                    int reputation = Integer.parseInt(element[6]);

                    if (cFullName.equalsIgnoreCase("null"))
                        UserProfile.PROFILE.AddContact(new ContactProfile(cID, cEmail));
                    else
                        UserProfile.PROFILE.AddContact(new AcceptedContactProfile(cID, cEmail, cFullName, cUsername, cContacts, cAvatarID, reputation));
                }
                catch (IndexOutOfBoundsException ex)
                {
                    ex.printStackTrace();
                }
                catch (NumberFormatException ex)
                {
                    ex.printStackTrace();
                }
            }
        }
        else
            Log.d("WARNING", "No contacts to load. ");

        UserProfile.PROFILE.LoadRemindersFromFile();
        UserProfile.PROFILE.LoadReminderFlagsFromFile();
        UserProfile.PROFILE.LoadReminderIgnoresFromFile();

        return true;
    }

    private boolean AttemptLoadSavedProfile()
    {
        SharedPreferences sharedPref = LoginActivity.this.getSharedPreferences("profile", Context.MODE_PRIVATE);
        final String email = sharedPref.getString("email", "null");
        final String password = sharedPref.getString("password", "null");
        final String fullname = sharedPref.getString("fullname", "null");
        final String username = sharedPref.getString("username", "null");
        final int id = sharedPref.getInt("id", 0);
        final boolean active = sharedPref.getBoolean("active", false);
        final int coins = sharedPref.getInt("coins", 0);
        final Set<String> contactsString = sharedPref.getStringSet("contacts", null);
        final String avatarID = sharedPref.getString("avatar", "default");

        if (email.equalsIgnoreCase("null") || password.equalsIgnoreCase("null") || fullname.equalsIgnoreCase("null") ||
                username.equalsIgnoreCase("null") || id == 0 || email.equalsIgnoreCase("null"))
        {
            Log.d("WARNING", "Invalid saved login parameters.");
            return false;
        }

        UserProfile.PROFILE = new UserProfile(id, (active == true) ? 1 : 0, fullname, username, email, password, coins, avatarID);

        if (contactsString != null)
        {
            for(String s : contactsString)
            {
                try
                {
                    String[] element = s.split("%");
                    int cID = Integer.parseInt(element[0]);
                    String cEmail = element[1];
                    String cUsername = element[2];
                    String cFullName = element[3];
                    String cContacts = element[4];
                    String cAvatarID = element[5];
                    int reputation = Integer.parseInt(element[6]);

                    if (cFullName.equalsIgnoreCase("null"))
                        UserProfile.PROFILE.AddContact(new ContactProfile(cID, cEmail));
                    else
                        UserProfile.PROFILE.AddContact(new AcceptedContactProfile(cID, cEmail, cFullName, cUsername, cContacts, cAvatarID, reputation));
                }
                catch (IndexOutOfBoundsException ex)
                {
                    ex.printStackTrace();
                }
                catch (NumberFormatException ex)
                {
                    ex.printStackTrace();
                }
            }
        }

        UserProfile.PROFILE.LoadRemindersFromFile();
        UserProfile.PROFILE.LoadReminderFlagsFromFile();
        UserProfile.PROFILE.LoadReminderIgnoresFromFile();

        return true;
    }
}
