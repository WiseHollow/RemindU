package net.johnbrooks.remindu.activities;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.requests.ActivateAccountRequest;
import net.johnbrooks.remindu.requests.RequestActivationEmailRequest;

public class ActivateAccountActivity extends AppCompatActivity
{
    public static final boolean IsOpen() { return open; }
    private static boolean open;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_activate_account);

        open = true;

        final EditText et_code = (EditText) findViewById(R.id.editText_activation_code);
        final Button btn_activate = (Button) findViewById(R.id.button_activation_send);
        final TextView tv_resend = (TextView) findViewById(R.id.textView_activation_resent);

        btn_activate.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                final String code = et_code.getText().toString();
                ActivateAccountRequest.SendRequest(ActivateAccountActivity.this, code);
            }
        });

        tv_resend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ActivateAccountActivity.this);
                dialog.setTitle("Activation Email")
                        .setMessage("Activation Email has been sent!")
                        .setNegativeButton("Okay", null)
                        .create()
                        .show();
                tv_resend.setEnabled(false);
                RequestActivationEmailRequest.SendRequest(ActivateAccountActivity.this);
            }
        });
    }

    @Override
    public void onBackPressed()
    {
        return;
    }

    @Override
    public void onDestroy()
    {
        open = false;
        super.onDestroy();
    }
}
