package net.johnbrooks.remindu.activities;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.util.Quotes;

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

        ((TextView) findViewById(R.id.splash_quote)).setText(Quotes.GetRandom());
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
