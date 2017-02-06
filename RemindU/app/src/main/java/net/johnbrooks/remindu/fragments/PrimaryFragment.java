package net.johnbrooks.remindu.fragments;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import net.johnbrooks.remindu.R;

public class PrimaryFragment extends Fragment
{
    private static PrimaryFragment primaryFragment;
    public static PrimaryFragment GetInstance() { return primaryFragment; }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        primaryFragment = this;
        return inflater.inflate(R.layout.fragment_primary, container, false);
    }
}
