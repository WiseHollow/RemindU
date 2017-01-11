package net.johnbrooks.remindu.activities;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.util.UserProfile;

public class SettingsActivity extends AppCompatActivity
{
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        //
        // Prepare back button
        //

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //
        // Prepare switches with live settings
        //

        ((Switch) findViewById(R.id.settings_receive_emails)).setChecked(UserAreaActivity.GetActivity().SharedPreferences.getBoolean("receive_emails", true));
        ((Switch) findViewById(R.id.settings_check_for_updates)).setChecked(UserAreaActivity.GetActivity().SharedPreferences.getBoolean("check_for_updates", true));

        findViewById(R.id.settings_check_for_updates).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SaveSettings();
            }
        });

        findViewById(R.id.settings_receive_emails).setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                SaveSettings();
            }
        });
    }

    private void SaveSettings()
    {
        SharedPreferences sharedPref = UserAreaActivity.GetActivity().SharedPreferences;
        SharedPreferences.Editor editor = sharedPref.edit();

        editor.putBoolean("receive_emails", ((Switch) findViewById(R.id.settings_receive_emails)).isChecked());
        editor.putBoolean("check_for_updates", ((Switch) findViewById(R.id.settings_check_for_updates)).isChecked());

        editor.commit();
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
