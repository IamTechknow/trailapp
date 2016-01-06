package com.ucschackathon.app;

import android.content.res.Resources;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Detailed view for the info markers. Checks for an argument string to determine what data to show here.
 * Default information from the layout XML is shown is the argument is not matched.
 */

public class DetailActivity extends AppCompatActivity {
    private static final String EXTRA_KEY = "title";

    //Private variables to
    private CollapsingToolbarLayout collapsingToolbar;
    private ImageView image;
    private TextView detail1, detail2, header1, header2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        Bundle extras = getIntent().getExtras();
        String title = null;
        if(extras != null)
            title = extras.getString(EXTRA_KEY);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.detail_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar bar = getSupportActionBar();
        if(bar != null)
            bar.setDisplayHomeAsUpEnabled(true);

        //Get UI elements
        collapsingToolbar = (CollapsingToolbarLayout) findViewById(R.id.collapsing_toolbar);
        image = (ImageView) findViewById(R.id.image);
        detail1 = (TextView) findViewById(R.id.detail_text1);
        detail2 = (TextView) findViewById(R.id.detail_text2);
        header1 = (TextView) findViewById(R.id.detail_header1);
        header2 = (TextView) findViewById(R.id.detail_header2);

        //Dynamically set content
        setContent(title);
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

    /**
     * Sets the content on the text views based on the marker title.
     * @param title Title of the marker
     */

    private void setContent(String title) {
        Resources res = getResources();
        int id = getIDfromTitle(title);

        collapsingToolbar.setTitle(getString(id));

        switch(id) {
            case R.string.cattail:
                image.setImageDrawable(res.getDrawable(R.drawable.cattail));
                detail1.setText(R.string.cattail_characteristics);
                detail2.setText(R.string.cattail_where);
                break;
            case R.string.tarplant:
                image.setImageDrawable(res.getDrawable(R.drawable.santacruztarplant));
                detail1.setText(R.string.tarplant_characteristics);
                detail2.setText(R.string.tarplant_where);
                break;
            case R.string.fitz:
                image.setBackgroundColor(res.getColor(android.R.color.holo_blue_light)); //No image, set background
                header1.setText(R.string.detail_about);
                header2.setText(R.string.detail_more);
                detail1.setText(R.string.fitz_about);
                detail2.setText(Html.fromHtml(getString(R.string.fitz_more)));
                break;
            case R.string.nature_center: //TODO: Link to access on GMaps
                image.setBackgroundColor(res.getColor(android.R.color.holo_blue_light));
                header1.setText(R.string.detail_about);
                header2.setText(R.string.detail_more);
                detail1.setText(R.string.nature_center_about);
                detail2.setText(Html.fromHtml(getString(R.string.nature_center_more)));
                break;
            default:
                image.setBackgroundColor(res.getColor(android.R.color.holo_blue_light));
                break;
        }
    }

    /**
     * Maps a Marker title string to a resource string ID. This is necessary because the marker data from the
     * database file comes from the KML, which may not change.
     * @param title The Marker title
     * @return ID for the title
     */

    private int getIDfromTitle(String title) {
        int ID;
        switch(title) {
            case "Cattails":
                ID = R.string.cattail;
                break;
            case "Tarplant Hill":
                ID = R.string.tarplant;
                break;
            case "Fitz WERC":
                ID = R.string.fitz;
                break;
            case "Wetlands of Watsonville Nature Center":
                ID = R.string.nature_center;
                break;
            default:
                ID = R.string.placeholder;
        }
        return ID;
    }
}
