package net.johnbrooks.remindu.activities;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import net.johnbrooks.remindu.R;
import net.johnbrooks.remindu.util.AvatarsImageAdapter;
import net.johnbrooks.remindu.util.UserProfile;

public class AvatarSelectActivity extends AppCompatActivity
{
    private GridView gridView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar_select);


        //
        // Prepare back button
        //

        ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);

        //

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

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
