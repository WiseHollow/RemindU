package net.johnbrooks.remindu.activities;

import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;

import net.johnbrooks.remindu.R;

public class MainActivity extends FragmentActivity
{
    private ViewPager viewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        viewPager = (ViewPager) findViewById(R.id.activity_main_pager);
        PagerAdapter pagerAdapter = new net.johnbrooks.remindu.util.PagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(pagerAdapter);
    }
}
