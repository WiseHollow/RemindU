package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.app.Service;
import android.graphics.drawable.Drawable;
import android.util.Log;

import net.johnbrooks.remindu.schedulers.MasterScheduler;

/**
 * Created by John on 12/27/2016.
 */

public class AvatarImageUtil
{
    public static Drawable GetAvatar(String AvatarID)
    {
        if (MasterScheduler.GetInstance() == null)
            return null;

        if (MasterScheduler.GetInstance().GetActivity() != null)
            return GetAvatar(MasterScheduler.GetInstance().GetActivity(), AvatarID);
        else if (MasterScheduler.GetInstance().GetService() != null)
            return GetAvatar(MasterScheduler.GetInstance().GetService(), AvatarID);
        else
            return null;
    }

    private static Drawable GetAvatar(Activity activity, String AvatarID)
    {
        if (AvatarID.equalsIgnoreCase("default"))
            AvatarID = "avatar_generic_default";
        int id = activity.getResources().getIdentifier(AvatarID, "drawable", activity.getPackageName());
        Drawable drawable = activity.getResources().getDrawable(id);
        if (drawable == null)
            Log.d("SEVERE", "Avatar image is a null object.");
        return drawable;
    }

    private static Drawable GetAvatar(Service service, String AvatarID)
    {
        if (AvatarID.equalsIgnoreCase("default"))
            AvatarID = "avatar_generic_default";
        int id = service.getResources().getIdentifier(AvatarID, "drawable", service.getPackageName());
        Drawable drawable = service.getResources().getDrawable(id);
        if (drawable == null)
            Log.d("SEVERE", "Avatar image is a null object.");
        return drawable;
    }


}
