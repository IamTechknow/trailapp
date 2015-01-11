package com.ucschackathon.app;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.text.Html;
import android.widget.TextView;

/**
 * About screen menu for the Trail App
 */
public class AboutActivity extends Activity {
    private TextView tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_main);
        String about = "<h1>About</h1><br><p>This mobile application is created during the HACK UCSC 2015 competition\n" +
                "to create a fast mobile interface to display trails and points of interest\n" +
                "in the Watsonville Wetlands as a project for the Watsonville Wetlands\n" +
                "Watch.</p>" +
                "<p>Authors:<br />" +
                "Android Developer: Edmond Lee<br />" +
                "Web application Developers: Matthew Smithey, Andre Nuygen Van Qui<br />" +
                "Other Team Members: Sergy Pretensky, Rosa Ayala </p><br />" +
                "<p>Watsonville Wetlands Watch members: Noelle Antolin, Darren </p>\n" +
                "<p><a href=\"https://github.com/IamTechknow/trailapp\">Trail App on Github</a><br />" +
                "<a href=\"http://watsonvillewetlandswatch.org/\">Watsonville Wetlands Watch Site</a></p>\n" +
                "<p>From the About page of the website:<br />" +
                "<blockquote>\n" +
                "Watsonville Wetlands Watch advocates for wetland issues, educates elementary, middle, and high school students,\n" +
                "restores degraded habitats, preserves what remains whole, and teaches appreciation for the unique beauty and life\n" +
                "of the Pajaro Valley wetlands.In cooperation with numerous other agencies, we support studies of and planning\n" +
                "for these sites.\n" +
                "</blockquote></p>";
        tv = (TextView)findViewById(R.id.about);
        tv.setText(Html.fromHtml(about));
    }
}
