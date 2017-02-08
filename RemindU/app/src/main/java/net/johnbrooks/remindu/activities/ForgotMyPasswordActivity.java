package net.johnbrooks.remindu.activities;

import android.app.Dialog;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.requests.ForgotMyPasswordRequest;

public class ForgotMyPasswordActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forgot_my_password);

        //
        // Prepare back button
        //

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //
        // Setup button
        //

        findViewById(R.id.button_Request_Reset).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                String email = ((TextView) findViewById(R.id.editText_Reset_Email)).getText().toString();

                if (!email.contains("@") || !email.contains("."))
                {
                    AlertDialog.Builder errorDialog = new AlertDialog.Builder(ForgotMyPasswordActivity.this);
                    errorDialog.setMessage("That is not a valid email address.")
                            .setNegativeButton("Retry", null)
                            .create()
                            .show();
                    ((TextView) findViewById(R.id.editText_Reset_Email)).setText("");
                    return;
                }

                //TODO: Send forgot password request

                Log.d("INFO", "Sending forgot my password request to: " + email);

                ForgotMyPasswordRequest.SendRequest(ForgotMyPasswordActivity.this, email);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
