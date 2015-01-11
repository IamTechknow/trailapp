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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_main);
        String about = "<h1>About</h1><p>This mobile application is created during the HACK UCSC 2015 competition\n" +
                "to create a mobile interface to display trails and points of interest\n" +
                "in the Watsonville Wetlands as a project for the Watsonville Wetlands\n" +
                "Watch. As an initial prototype but now a mobile map that can be opened from a browser," +
                "we also created an interactive map to display data. We used Google Maps API for both applications. </p>" +
                "<p>Authors:<br />" +
                "Android Application Developer: Edmond Lee<br />" +
                "Web Application Developers: Matthew Smithey, Andre Nuygen Van Qui, Sergy Pretensky<br />" +
                "Other Team Members: Rosa Ayala </p><br />" +
                "<p>Watsonville Wetlands Watch members: Noelle Antolin, Darren </p>\n" +
                "<p><a href=\"https://github.com/IamTechknow/trailapp\">Trail App on Github</a><br />" +
                "<a href=\"http://watsonvillewetlandswatch.org/\">Watsonville Wetlands Watch Site</a></p>\n" +
                "<p>About Watsonville Wetlands Watch: <br />" +
                "<blockquote>\n" +
                "&quot;Watsonville Wetlands Watch advocates for wetland issues, educates elementary, middle, and high school students,\n" +
                "restores degraded habitats, preserves what remains whole, and teaches appreciation for the unique beauty and life\n" +
                "of the Pajaro Valley wetlands.In cooperation with numerous other agencies, we support studies of and planning\n" +
                "for these sites.&quot;\n" +
                "</blockquote></p>";
        tv = (TextView)findViewById(R.id.about);
        tv.setText(Html.fromHtml(about));
        tv.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
