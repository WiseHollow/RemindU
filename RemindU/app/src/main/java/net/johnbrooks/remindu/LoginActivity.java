package net.johnbrooks.remindu;

import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import net.johnbrooks.remindu.util.LoginRequest;

import org.json.JSONException;
import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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

                Response.Listener<String> responseListener = new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response)
                    {
                        try
                        {
                            JSONObject jsonResponse = new JSONObject(response);
                            boolean success = jsonResponse.getBoolean("success");

                            if (success)
                            {
                                final int active = jsonResponse.getInt("active");

                                final String fullName = jsonResponse.getString("fullname");
                                final String email = jsonResponse.getString("email");
                                final String username = jsonResponse.getString("username");

                                final int pointsTotal = jsonResponse.getInt("pointsTotal");
                                final int pointsGiven = jsonResponse.getInt("pointsGiven");
                                final int pointsReceived = jsonResponse.getInt("pointsReceived");

                                Intent intent = new Intent(LoginActivity.this, UserAreaActivity.class);
                                intent.putExtra("active", active);
                                intent.putExtra("fullname", fullName);
                                intent.putExtra("email", email);
                                intent.putExtra("username", username);
                                intent.putExtra("pointsTotal", pointsTotal);
                                intent.putExtra("pointsGiven", pointsGiven);
                                intent.putExtra("pointsReceived", pointsReceived);

                                LoginActivity.this.startActivity(intent);
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

                LoginRequest request = new LoginRequest(email, password, responseListener);
                RequestQueue queue = Volley.newRequestQueue(LoginActivity.this);
                queue.add(request);
            }
        });
    }
}
