package net.johnbrooks.remindu.activities;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import net.johnbrooks.remindu.R;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        TextView tv_version = (TextView) findViewById(R.id.textView_about_version);
        TextView tv_website = (TextView) findViewById(R.id.textView_about_website);
        tv_website.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Uri uri = Uri.parse("http://www.johnbrooks.net/");
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        PackageInfo pInfo;

        try
        {
            pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            String version = pInfo.versionName;
            tv_version.setText("Version: " + version);
        } catch (PackageManager.NameNotFoundException e)
        {
            e.printStackTrace();
        }
    }
}
