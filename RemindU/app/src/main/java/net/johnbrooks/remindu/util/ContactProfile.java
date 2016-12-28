package net.johnbrooks.remindu.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import net.johnbrooks.remindu.requests.DeleteContactRequest;

/**
 * Created by John on 11/29/2016.
 */

public class ContactProfile
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
    public LinearLayout CreateImageView(final ManageContactsActivity activity)
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
    public LinearLayout CreateWidget(final ManageContactsActivity activity)
    {
        // Right now we only use a default avatar for each contact.
        Bitmap bDefaultAvatar = BitmapFactory.decodeResource( activity.getResources(), R.drawable.user_48 );
        // Lets get our delete image.
        Bitmap bDelete = BitmapFactory.decodeResource( activity.getResources(), R.drawable.delete_48);
        LinearLayout layout = new LinearLayout(activity);
        layout.setLayoutParams(new LinearLayout.LayoutParams
                (
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT, 1f
                ));
        layout.setOrientation(LinearLayout.HORIZONTAL);
        SpannableStringBuilder spannableStringLeft = new SpannableStringBuilder();
        SpannableStringBuilder spannableStringRight = new SpannableStringBuilder();

        String line1 = "_ " + GetDisplayName();
        if (!IsContact())
            line1 += " Request Pending...";
        spannableStringLeft.append(line1);
        TextView textView = new TextView(activity);
        layout.addView(textView);
        textView.setTextSize(14f);
        textView.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams textViewParams = new LinearLayout.LayoutParams
                (
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT, 0.85f
                );
        textView.setLayoutParams(textViewParams);
        spannableStringLeft.setSpan(new ImageSpan(textView.getContext(), bDefaultAvatar), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        if (!IsContact())
            spannableStringLeft.setSpan(new RelativeSizeSpan(0.75f), GetDisplayName().length() + 2, line1.length() - 2, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        textView.setText(spannableStringLeft);

        TextView button = new TextView(activity);
        button.setMovementMethod(LinkMovementMethod.getInstance());
        layout.addView(button);
        spannableStringRight.append("X");
        spannableStringRight.setSpan(new ImageSpan(button.getContext(), bDelete), 0, 1, Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        LinearLayout.LayoutParams buttonParams = new LinearLayout.LayoutParams
                (
                        0,
                        LinearLayout.LayoutParams.MATCH_PARENT, 0.15f
                );

        button.setTextColor(Color.BLACK);
        button.setLayoutParams(buttonParams);

        ClickableSpan spanDelete = new ClickableSpan()
        {
            @Override
            public void onClick(View view)
            {
                if (GetID() == -1)
                {
                    return;
                }

                //TODO: Pull data from server
                Log.d("INFO", "Requesting that contact id=" + GetID() + " be removed.");

                DeleteContactRequest.SendRequest(activity, GetID());

                //TODO: Refresh contents...
            }
        };
        spannableStringRight.setSpan(spanDelete, 0, 1, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        button.setText(spannableStringRight);

        return layout;
    }
    @Override
    public String toString()
    {
        return GetID() + "%" + GetEmail() + "%" + GetUsername() + "%" + GetFullName() + "%" + GetContacts();
    }
}
