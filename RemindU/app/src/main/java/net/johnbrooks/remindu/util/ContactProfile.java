package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.v7.widget.ButtonBarLayout;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ImageSpan;
import android.text.style.RelativeSizeSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import net.johnbrooks.remindu.activities.ManageContactsActivity;
import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.ReminderListActivity;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.requests.DeleteContactRequest;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Created by John on 11/29/2016.
 */

public class ContactProfile implements Comparable<ContactProfile>
{
    public static ContactProfile GetProfile(int user_id)
    {
        for (ContactProfile cp : UserProfile.PROFILE.GetContacts())
            if (cp.GetID() == user_id)
                return cp;
        return null;
    }

    private int ID;
    private String Email;

    public ContactProfile(int id, String email)
    {
        ID = id;
        Email = email;
    }
    public final int GetID() { return ID; }
    public final String GetEmail() { return Email; }
    public String GetUsername() { return "null"; }
    public String GetFullName() { return "null"; }
    public String GetDisplayName() { return Email; }
    public String GetContacts() { return "null"; }
    public String GetAvatarID() { return "default"; }
    public boolean IsContact()
    {
        if (this.getClass() == AcceptedContactProfile.class)
            return true;
        else
            return false;
    }
    public final int GetAmountOfReminders()
    {
        int i = 0;

        if (UserProfile.PROFILE == null)
            return 0;
        else
            for (Reminder r : UserProfile.PROFILE.GetReminders())
                if (r.GetTo() == GetID() || r.GetFrom() == GetID())
                    i++;
        return i;
    }

    public final List<Reminder> GetReminders()
    {
        final List<Reminder> list = new ArrayList<>();

        for (Reminder r : UserProfile.PROFILE.GetReminders())
            if (r.GetFrom() == GetID() || r.GetTo() == GetID())
                list.add(r);

        return list;
    }

    public LinearLayout CreateCategoryWidget(final Activity activity)
    {
        LinearLayout layout = new LinearLayout(activity);
        layout.setLayoutParams(new LinearLayout.LayoutParams
            (
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT, 1.0f
            ));
        layout.setOrientation(LinearLayout.HORIZONTAL);

        ImageView avatar = new ImageView(activity);
        avatar.setLayoutParams(new GridView.LayoutParams(250, 250));
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setPadding(8, 8, 8, 8);

        avatar.setBackground(AvatarImageUtil.GetAvatar(activity, GetAvatarID()));
        layout.addView(avatar);

        TextView info = new TextView(activity);
        String text = "";

        text+=GetFullName();
        text+="\n";
        text+="Active Reminders: " + GetAmountOfReminders();
        info.setText(text);

        layout.addView(info);
        final ContactProfile cp = this;
        layout.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent(UserAreaActivity.GetActivity(), ReminderListActivity.class);
                intent.putExtra("contactID", cp.GetID());
                UserAreaActivity.GetActivity().startActivity(intent);
            }
        });

        return layout;
    }
    public LinearLayout CreateWidget(final ManageContactsActivity activity)
    {
        LinearLayout layout = new LinearLayout(activity);
        layout.setLayoutParams(new LinearLayout.LayoutParams
                (
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1f
                ));
        layout.setOrientation(LinearLayout.HORIZONTAL);

        ImageView avatar = new ImageView(activity);

        avatar.setLayoutParams(new GridView.LayoutParams(250, 250));
        avatar.setScaleType(ImageView.ScaleType.CENTER_CROP);
        avatar.setPadding(8, 8, 8, 8);
        avatar.setBackground(AvatarImageUtil.GetAvatar(activity, GetAvatarID()));
        layout.addView(avatar);

        TextView text = new TextView(activity);
        String lore = "";
        if (!IsContact())
        {
            // limited display
            lore+="Email: " + GetEmail();
            lore+="\n";
            lore+="Request Pending...";
        }
        else
        {
            lore+="Email: " + GetEmail();
            lore+="\n";
            lore+="Name: " + GetFullName();
            lore+="\n";
            lore+="Username: " + GetUsername();
        }
        text.setText(lore);
        LinearLayout.LayoutParams TextParams = new TableRow.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.85f);
        text.setLayoutParams(TextParams);
        layout.addView(text);

        Button removeButton = new Button(activity);
        removeButton.setText("X");
        LinearLayout.LayoutParams RemoveBtnParams = new TableRow.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 0.15f);
        removeButton.setLayoutParams(RemoveBtnParams);
        removeButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (GetID() == -1)
                {
                    return;
                }

                Log.d("INFO", "Requesting that contact id=" + GetID() + " be removed.");
                DeleteContactRequest.SendRequest(activity, GetID());
            }
        });

        layout.addView(removeButton);

        return layout;
    }

    @Override
    public String toString()
    {
        return GetID() + "%" + GetEmail() + "%" + GetUsername() + "%" + GetFullName() + "%" + GetContacts() + "%" + GetAvatarID();
    }

    @Override
    public int compareTo(ContactProfile o)
    {
        int compare = GetAmountOfReminders() > o.GetAmountOfReminders() ? + 1 : GetAmountOfReminders() < o.GetAmountOfReminders() ? -1 : 0;
        return -compare;
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof ContactProfile)
        {
            ContactProfile cp = (ContactProfile) o;
            if (cp.GetID() == GetID())
                return true;
        }
        return false;
    }
}
