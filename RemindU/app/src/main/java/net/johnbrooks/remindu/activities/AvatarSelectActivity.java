package net.johnbrooks.remindu.activities;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.requests.UpdateSettingsRequest;
import net.johnbrooks.remindu.schedulers.UpdateMyProfileScheduler;
import net.johnbrooks.remindu.util.AvatarsImageAdapter;
import net.johnbrooks.remindu.util.UserProfile;

import java.util.ArrayList;
import java.util.List;

public class AvatarSelectActivity extends AppCompatActivity
{
    private GridView gridView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_select);


        final AvatarsImageAdapter adapter = new AvatarsImageAdapter(this);
        gridView = (GridView) findViewById(R.id.grid_view_avatar_select);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id)
            {
                int rId = adapter.mThumbIds[position];
                String name = getResources().getResourceName(rId).split("/")[1];

                UserProfile.PROFILE.SetAvatarID(name);

                Toast.makeText(AvatarSelectActivity.this, "Avatar Updated",
                        Toast.LENGTH_SHORT).show();

                finish();
            }
        });

    }
}
