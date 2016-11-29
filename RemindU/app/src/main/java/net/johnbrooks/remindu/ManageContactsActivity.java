package net.johnbrooks.remindu;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.SpannableStringBuilder;
import android.text.style.ImageSpan;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.johnbrooks.remindu.util.ContactProfile;
import net.johnbrooks.remindu.util.UserProfile;

public class ManageContactsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_contacts);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        LinearLayout layout = (LinearLayout) findViewById(R.id.Manage_Contacts_Scroll_View_Layout);

        for (ContactProfile profile : UserProfile.PROFILE.GetContacts())
        {
            TextView view = new TextView(ManageContactsActivity.this);

            SpannableStringBuilder builder = new SpannableStringBuilder();
            ImageSpan deleteImageSpan;


            view.setPadding(5, 15, 5, 15);
            view.setTextSize(14f);
            view.setText(profile.GetFullName());

            layout.addView(view);
        }
    }

}
