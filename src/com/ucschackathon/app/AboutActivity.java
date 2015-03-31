package com.ucschackathon.app;

import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.Html;
import android.widget.TextView;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.Toolbar;

/**
 * About screen menu for the Trail App with Material Design
 */
public class AboutActivity extends ActionBarActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_main);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.tool_bar);
        setSupportActionBar(mToolbar);

        String mAbout = getString(R.string.aboutText);
        TextView mTv = (TextView)findViewById(R.id.about);
        mTv.setText(Html.fromHtml(mAbout));
        mTv.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
