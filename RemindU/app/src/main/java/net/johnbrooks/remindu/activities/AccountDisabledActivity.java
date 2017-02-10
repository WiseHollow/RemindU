package net.johnbrooks.remindu.activities;

import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.schedulers.MasterScheduler;
import net.johnbrooks.remindu.util.UserProfile;

public class AccountDisabledActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account_disabled);

        SharedPreferences.Editor editor = UserAreaActivity.GetActivity().SharedPreferences.edit();
        editor.putBoolean("readDisabledMessage", true);
        editor.commit();

        UserProfile.CleanupLocalFiles();
        MasterScheduler.GetInstance().Call();

        findViewById(R.id.button_okay).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                finish();
            }
        });
    }


    @Override
    public void onBackPressed()
    {
        return;
    }
}
