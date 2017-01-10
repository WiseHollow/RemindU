package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.Log;

/**
 * Created by John on 12/27/2016.
 */

public class AvatarImageUtil
{
    public static Drawable GetAvatar(Activity activity, String AvatarID)
    {
        if (AvatarID.equalsIgnoreCase("default"))
            AvatarID = "avatar_generic_default";
        int id = activity.getResources().getIdentifier(AvatarID, "drawable", activity.getPackageName());
        Drawable drawable = activity.getResources().getDrawable(id);
        if (drawable == null)
            Log.d("SEVERE", "Avatar image is a null object.");
        return drawable;
    }


}
