package net.johnbrooks.remindu.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import net.johnbrooks.remindu.R;

public class SplashActivity extends AppCompatActivity
{
    private Handler Handler;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        getSupportActionBar().hide();
        Handler = new Handler();
        GoToLogin.run();
    }

    Runnable GoToLogin = new Runnable()
    {
        @Override
        public void run()
        {
            Handler.postDelayed(EndSplashScreen, 3000);
        }
    };

    Runnable EndSplashScreen = new Runnable()
    {
        @Override
        public void run()
        {
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        }
    };
}
