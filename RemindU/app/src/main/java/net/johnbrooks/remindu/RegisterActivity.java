package net.johnbrooks.remindu;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.util.RegisterRequest;

import org.json.JSONException;
import org.json.JSONObject;

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

        final Button bRegister = (Button) findViewById(R.id.button_Register);

        bRegister.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                final String fullname = etFullName.getText().toString();
                final String username = etUsername.getText().toString();
                final String email = etEmail.getText().toString();
                final String password = etPassword.getText().toString();

                AlertDialog.Builder errorDialog = new AlertDialog.Builder(RegisterActivity.this);
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

                Response.Listener<String> listener = new Response.Listener<String>()
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

                            //Log.d("INFO", "Received username " + gotUsername);
                            if (success)
                            {
                                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                                RegisterActivity.this.startActivity(intent);
                                finish();
                            }
                            else
                            {
                                String message = "Registration Failed!";
                                if (!usernameAvailable)
                                    message+="\nUsername is not available.";
                                if (!emailAvailable)
                                    message+="\nEmail is already in use.";

                                if (message == "Registration Failed!")
                                    message+= "\nUnknown error. Please contact developers about unknown registration error.";

                                AlertDialog.Builder builder = new AlertDialog.Builder(RegisterActivity.this);
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

                RegisterRequest request = new RegisterRequest(fullname, username, email, password, listener);
                RequestQueue queue = Volley.newRequestQueue(RegisterActivity.this);
                queue.add(request);
            }
        });
    }
}
