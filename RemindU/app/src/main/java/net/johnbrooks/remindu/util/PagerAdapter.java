package net.johnbrooks.remindu.util;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;

import net.johnbrooks.remindu.fragments.FeedFragment;
import net.johnbrooks.remindu.fragments.PrimaryFragment;

/**
 * Created by ieatl on 2/2/2017.
 */

public class PagerAdapter extends FragmentPagerAdapter
{
    public PagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position)
        {
            case 0:
                return new PrimaryFragment();
            case 1:
                return new FeedFragment();
            default:
                break;
        }

        return null;
    }

    @Override
    public int getCount()
    {
        return 2;
    }
}
