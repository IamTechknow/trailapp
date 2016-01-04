package com.ucschackathon.app;

import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;

import com.ucschackathon.app.adapter.MarkerAdapter;

/**
 * Displays a list of markers after serializing them from the local database.
 * Uses a RecyclerView and a custom adapter to implement the list
 * Here is a good tutorial: <a href=http://enoent.fr/blog/2015/01/18/recyclerview-basics/>http://enoent.fr/blog/2015/01/18/recyclerview-basics/</a>
 */

public class ListMarkersActivity extends AppCompatActivity {
    private RecyclerView mRecyclerView;
    private TrailDatabaseHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        ActionBar bar = getSupportActionBar();
        if(bar != null)
            bar.setDisplayHomeAsUpEnabled(true);

        mHelper = new TrailDatabaseHelper(getApplicationContext());

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MarkerAdapter(mHelper.queryMarkers(), getResources()));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (NavUtils.getParentActivityName(this) != null)
                    NavUtils.navigateUpFromSameTask(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
