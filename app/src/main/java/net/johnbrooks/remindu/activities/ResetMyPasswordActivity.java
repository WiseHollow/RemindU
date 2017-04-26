package net.johnbrooks.remindu.activities;

import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.requests.ResetPasswordRequest;

public class ResetMyPasswordActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_my_password);

        final String email = getIntent().getStringExtra("email");
        final int code = getIntent().getIntExtra("code", 0);

        if (email == null || email.equalsIgnoreCase("") || code == 0)
        {
            finish();
            return;
        }

        findViewById(R.id.button_Reset_Password).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String password = ((EditText) findViewById(R.id.editText_Password)).getText().toString();
                final String confirmPassword = ((EditText) findViewById(R.id.editText_Password_Confirm)).getText().toString();

                if (!password.equals(confirmPassword))
                {
                    Snackbar.make(getCurrentFocus(), "Passwords do not match.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                if (password.length() < 6)
                {
                    Snackbar.make(getCurrentFocus(), "Password must be at least 6 characters in length.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                boolean numberFound = false;
                for (char c : password.toCharArray())
                {
                    try
                    {
                        Integer.valueOf(c);
                        numberFound = true;
                    }
                    catch (Exception ex) { }
                }

                if (!numberFound)
                {
                    Snackbar.make(getCurrentFocus(), "Password must contain at least one number.", Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                    return;
                }

                ResetPasswordRequest.SendRequest(ResetMyPasswordActivity.this, email, code, password);
            }
        });
    }
}
