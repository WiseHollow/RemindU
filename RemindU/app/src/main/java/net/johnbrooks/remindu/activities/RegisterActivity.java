package net.johnbrooks.remindu.activities;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.requests.RegisterRequest;

public class RegisterActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        final EditText etUsername = (EditText) findViewById(R.id.editText_Username);
        final EditText etFullName = (EditText) findViewById(R.id.editText_FullName);
        final EditText etEmail = (EditText) findViewById(R.id.editText_Email);
        final EditText etPassword = (EditText) findViewById(R.id.editText_Password);
        final EditText etPasswordConfirm = (EditText) findViewById(R.id.editText_Password_Confirm);

        final Button bRegister = (Button) findViewById(R.id.button_Register);
        final TextView tv_alreadyRegistered = (TextView) findViewById(R.id.textView_AlreadyRegistered);

        tv_alreadyRegistered.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                finish();
            }
        });

        bRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                // When the register button is clicked. We will pull entered information.

                final String fullname = etFullName.getText().toString();
                final String username = etUsername.getText().toString();
                final String email = etEmail.getText().toString();
                final String password = etPassword.getText().toString();
                final String passwordConfirm = etPasswordConfirm.getText().toString();

                AlertDialog.Builder errorDialog = new AlertDialog.Builder(RegisterActivity.this);
                if (!password.equals(passwordConfirm))
                {
                    etPassword.setText("");
                    etPasswordConfirm.setText("");
                    errorDialog.setMessage("Passwords do not match.")
                            .setNegativeButton("Retry", null)
                            .create()
                            .show();
                    return;
                }

                // Next we will create an error dialog to prepare for possible errors in input.

                if (!email.contains("@") || !email.contains("."))
                {
                    errorDialog.setMessage("Invalid email format.")
                            .setNegativeButton("Retry", null)
                            .create()
                            .show();
                    return;
                }

                if (username.length() < 4)
                {
                    errorDialog.setMessage("Username must be at least 4 characters.")
                            .setNegativeButton("Retry", null)
                            .create()
                            .show();
                    return;
                }

                for (char c : username.toCharArray())
                {
                    if (!Character.isLetterOrDigit(c) && c != '_')
                    {
                        errorDialog.setMessage("Username cannot contain symbols (excluding '_').")
                                .setNegativeButton("Retry", null)
                                .create()
                                .show();
                        return;
                    }
                }

                if (password.length() < 6)
                {
                    errorDialog.setMessage("Password must be at least 6 characters in length.")
                            .setNegativeButton("Retry", null)
                            .create()
                            .show();
                    return;
                }

                RegisterRequest.SendRequest(RegisterActivity.this, fullname, username, email, password);
            }
        });
    }
}
