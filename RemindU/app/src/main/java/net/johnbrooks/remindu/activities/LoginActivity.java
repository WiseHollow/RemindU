package net.johnbrooks.remindu.activities;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.util.AcceptedContactProfile;
import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.requests.LoginRequest;
import net.johnbrooks.remindu.util.Network;
import net.johnbrooks.remindu.util.UserProfile;

import java.io.File;
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

                LoginRequest.SendRequest(LoginActivity.this, email, password);
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
            editor.putString("fullname", "null");
            editor.putString("username", "null");
            editor.putInt("id", 0);
            editor.putBoolean("active", false);
            editor.putInt("pointsTotal", 0);
            editor.putInt("pointsSent", 0);
            editor.putInt("pointsReceived", 0);
            editor.putStringSet("contacts", null);
            editor.commit();
            getIntent().putExtra("SignOut", false);

            File fileReminders = new File(getBaseContext().getFilesDir(), "reminders.yml");
            File fileIgnores = new File(getBaseContext().getFilesDir(), "ignores.yml");
            if (fileReminders.exists())
                fileReminders.delete();
            if (fileIgnores.exists())
                fileIgnores.delete();


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
            LoginRequest.SendRequest(LoginActivity.this, email, password);
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
        final int coins = sharedPref.getInt("coins", 0);
        final Set<String> contactsString = sharedPref.getStringSet("contacts", null);


        if (email.equalsIgnoreCase("null") || password.equalsIgnoreCase("null") || fullname.equalsIgnoreCase("null") ||
                username.equalsIgnoreCase("null") || id == 0 || email.equalsIgnoreCase("null"))
            return false;

        UserProfile.PROFILE = new UserProfile(id, (active == true) ? 1 : 0, fullname, username, email, password, coins);
        UserProfile.PROFILE.LoadReminderIgnoresFromFile(LoginActivity.this);

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
}
