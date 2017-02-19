package net.johnbrooks.remindu.util;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import net.johnbrooks.remindu.fragments.FeedFragment;
import net.johnbrooks.remindu.fragments.DiscoverFragment;

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
                return new DiscoverFragment();
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
