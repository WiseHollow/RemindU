package net.johnbrooks.remindu.util;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.View;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import net.johnbrooks.remindu.activities.ManageContactsActivity;
import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.activities.ReminderListActivity;
import net.johnbrooks.remindu.activities.UserAreaActivity;
import net.johnbrooks.remindu.requests.DeleteContactRequest;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by John on 11/29/2016.
 */

public class ContactProfile implements Comparable<ContactProfile>
{
    public static ContactProfile GetProfile(final int user_id)
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
    public String GetShortName() { return "null"; }
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

    public RelativeLayout CreateCategoryWidgetForGrid(final Activity activity)
    {
        RelativeLayout layout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.widget_contact_icon, null);

        TextView tv_name = (TextView) layout.findViewById(R.id.contact_name);
        TextView tv_reminders = (TextView) layout.findViewById(R.id.contact_reminders);
        ImageView iv_avatar = (ImageView) layout.findViewById(R.id.contact_avatar);

        tv_name.setText(GetShortName());
        tv_reminders.setText("" + GetAmountOfReminders());
        iv_avatar.setBackground(AvatarImageUtil.GetAvatar(activity, GetAvatarID()));

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

    public LinearLayout CreateCategoryWidget(final Activity activity)
    {
        LinearLayout layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.widget_contact_details, null);

        TextView tv_name = (TextView) layout.findViewById(R.id.Contact_Profile_Full_Name);
        TextView tv_reminders = (TextView) layout.findViewById(R.id.Contact_Profile_Reminders);
        ImageView iv_avatar = (ImageView) layout.findViewById(R.id.Contact_Profile_Avatar);

        iv_avatar.setBackground(AvatarImageUtil.GetAvatar(activity, GetAvatarID()));
        tv_name.setText(GetFullName());
        tv_reminders.setText("Reminders: " + GetAmountOfReminders());

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
        LinearLayout layout = (LinearLayout) activity.getLayoutInflater().inflate(R.layout.widget_contact_details_manage, null);

        ImageView avatarView = (ImageView) layout.findViewById(R.id.Contact_Profile_Avatar);
        TextView fullnameView = (TextView) layout.findViewById(R.id.Contact_Profile_Full_Name);
        TextView emailView = (TextView) layout.findViewById(R.id.Contact_Profile_Email);
        TextView removeView = (TextView) layout.findViewById(R.id.Contact_Profile_Remove);

        avatarView.setBackground(AvatarImageUtil.GetAvatar(activity, GetAvatarID()));
        fullnameView.setText(GetFullName());
        emailView.setText(GetEmail());
        removeView.setOnClickListener(new View.OnClickListener()
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
        //int compare = GetAmountOfReminders() > o.GetAmountOfReminders() ? + 1 : GetAmountOfReminders() < o.GetAmountOfReminders() ? -1 : 0;
        int compare = GetFullName().compareTo(o.GetFullName());
        return compare;
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
