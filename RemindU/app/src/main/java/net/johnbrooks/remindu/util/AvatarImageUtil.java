package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.graphics.drawable.Drawable;

import net.johnbrooks.remindu.R;

import java.lang.reflect.Field;

/**
 * Created by John on 12/27/2016.
 */

public class AvatarImageUtil
{
    private static String GetAvatarPath(String AvatarID)
    {
        if (AvatarID.equalsIgnoreCase("default"))
            return "@drawable/avatar_generic_default";

        return "@drawable/" + AvatarID;
    }

    public static Drawable GetAvatar(Activity activity, String AvatarID)
    {
        if (AvatarID.equalsIgnoreCase("default"))
            AvatarID = "avatar_generic_default";
        int id = activity.getResources().getIdentifier(AvatarID, "drawable", activity.getPackageName());
        Drawable drawable = activity.getResources().getDrawable(id);
        return drawable;
    }

    public static int GetDrawableResourceID(Activity activity, String id)
    {
        return activity.getResources().getIdentifier(id, "drawable", activity.getPackageName());
    }
}
