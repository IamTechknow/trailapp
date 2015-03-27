package com.ucschackathon.app;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.text.Html;
import android.widget.TextView;

/**
 * About screen menu for the Trail App
 */
public class AboutActivity extends Activity {
    private TextView tv;
    String about;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_main);

        about = getString(R.string.aboutText);
        tv = (TextView)findViewById(R.id.about);
        tv.setText(Html.fromHtml(about));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
