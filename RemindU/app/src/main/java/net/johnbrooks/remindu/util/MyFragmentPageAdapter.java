package net.johnbrooks.remindu.util;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.ListFragment;

/**
 * Created by ieatl on 2/2/2017.
 */

public class MyFragmentPageAdapter extends FragmentPagerAdapter
{
    public MyFragmentPageAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        return ArrayListFragment.newInstance(position);
    }

    @Override
    public int getCount()
    {
        return 2;
    }

    public static class ArrayListFragment extends ListFragment
    {
        int mNum;

        /**
         * Create a new instance of CountingFragment, providing "num"
         * as an argument.
         */
        static ArrayListFragment newInstance(int num) {
            ArrayListFragment f = new ArrayListFragment();

            // Supply num input as an argument.
            Bundle args = new Bundle();
            args.putInt("num", num);
            f.setArguments(args);

            return f;
        }
    }
}
