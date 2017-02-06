package net.johnbrooks.remindu.activities;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.ViewPager;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.util.PagerAdapter;

public class MainActivity extends FragmentActivity
{
    private static MainActivity mainActivity;
    public static MainActivity GetInstance() { return mainActivity; }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ViewPager viewPager = (ViewPager) findViewById(R.id.activity_main_pager);
        PagerAdapter pagerAdapter = new PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
        mainActivity = this;
    }
}
