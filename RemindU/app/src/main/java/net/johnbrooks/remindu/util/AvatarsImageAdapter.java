package net.johnbrooks.remindu.util;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

import net.johnbrooks.remindu.R;

/**
 * Created by John on 12/28/2016.
 */

public class AvatarsImageAdapter extends BaseAdapter {
    private Context mContext;

    public AvatarsImageAdapter(Context c) {
        mContext = c;
    }

    public int getCount() {
        return mThumbIds.length;
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    // create a new ImageView for each item referenced by the Adapter
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView imageView;
        if (convertView == null)
        {
            // if it's not recycled, initialize some attributes
            imageView = new ImageView(mContext);
            imageView.setLayoutParams(new GridView.LayoutParams(310, 310));
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setPadding(8, 8, 8, 8);
        }
        else
        {
            imageView = (ImageView) convertView;
        }

        imageView.setImageResource(mThumbIds[position]);
        return imageView;
    }

    // references to our images
    public Integer[] mThumbIds =
            {
                    R.drawable.avatar_generic_default,
                    R.drawable.avatar_generic_01,
                    R.drawable.avatar_generic_02,
                    R.drawable.avatar_generic_03,
                    R.drawable.avatar_generic_04,
                    R.drawable.avatar_generic_05,
                    R.drawable.avatar_generic_06,
                    R.drawable.avatar_generic_07,
                    R.drawable.avatar_generic_08,
                    R.drawable.avatar_animal_01,
                    R.drawable.avatar_animal_02,
                    R.drawable.avatar_animal_03,
                    R.drawable.avatar_animal_04,
                    R.drawable.avatar_animal_05,
                    R.drawable.avatar_animal_06,
                    R.drawable.avatar_animal_07,
                    R.drawable.avatar_animal_08,
                    R.drawable.avatar_animal_09,
                    R.drawable.avatar_animal_10,
                    R.drawable.avatar_animal_11,
                    R.drawable.avatar_animal_12,
                    R.drawable.avatar_animal_13,
                    R.drawable.avatar_animal_14
            };
}