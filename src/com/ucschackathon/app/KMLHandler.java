package com.ucschackathon.app;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;

import com.google.android.gms.maps.model.LatLng;
import android.graphics.Color;

/**
 * To parse the KML file, we are using the Java API for XML Processing.
 * We extend the DefaultHandler class in order to implement the ContentHandler interface for the SAXParser
 */

public class KMLHandler extends DefaultHandler{
    private HashMap<String,Color> styles; //hold pairs of styles and associated colors - each trail has a style
    private HashMap<String,ArrayList<LatLng>> map;
    private ArrayList<String> tobeDrawn; //Names of things to be drawn
    private Color trailColor;
    private String currProcessing;
    private boolean in_placemarktag, in_nametag, in_descriptiontag, in_linestringtag, in_pointtag, in_coordinatestag, in_styletag, in_linearringtag;

    //Define URLs ahead of time to allow association of icons, trails
    private final String theKML = "http://www.watsonvillewetlandswatch.org/sloughs/EntireMapWeb.kml";
    private final String entranceIconURL = "http://www.watsonvillewetlandswatch.org/sloughs/SloughTrailEntrances.png";
    private final String bathroomIconURL = "http://www.watsonvillewetlandswatch.org/sloughs/Bathrooms.png";

    public Color getColor(String requesting) {
        return styles.get((String) requesting);
    }

    public ArrayList<LatLng> getCoords(String requesting) {
       /* ArrayList<LatLng> arr = new ArrayList<LatLng>();
        in_coordinatestag = true;
        if(currProcessing.equals("trail")) { //Getting coordinates for a trail

        }
        else { //Getting coordinates for markers

        }

        return arr;*/
        return map.get((String) requesting); //returns the LatLng list associated with requesting String
    }

    @Override
    public void startDocument() throws SAXException {
        //init all data structures that will be used by TrailActivity to draw everything (use getter methods)
        map = new HashMap<String, ArrayList<LatLng>>(); //For now assume you put trail info here
        styles = new HashMap<String, Color>();
        tobeDrawn = new ArrayList<String>();

    }

    @Override
    public void endDocument() throws SAXException {
        // Nothing to do
    }

    @Override
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws SAXException {
        switch(localName) {
            case "name":
                in_nametag = true;
                break;
            case "Placemark":
                in_placemarktag = true;
                break;
            case "Description":
                in_descriptiontag = true;
                break;
            case "Point":
                in_pointtag = true;
                break;
            case "LineString":
                in_linestringtag = true;
                break;
            case "LinearRing":
                in_linearringtag = true;
                break;
            case "Style": //Prepare to get a Style! Need to get info in the attribute
                in_styletag = true;
                break;
            case "coordinates": //Prepare to get coordinates!
                in_coordinatestag = true;
                break;
            default:
                break;
        }
    }

    @Override
    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        switch(localName) {
            case "name":
                in_nametag = false;
                break;
            case "Placemark":
                in_placemarktag = false;
                break;
            case "Description":
                in_descriptiontag = false;
                break;
            case "Point":
                in_pointtag = false;
                break;
            case "LineString":
                in_linestringtag = false;
                break;
            case "LinearRing":
                in_linearringtag = false;
                break;
            case "Style":
                in_styletag = false;
                break;
            case "coordinates":
                in_coordinatestag = false;
                break;
            default:
                break;
        }
    }

    @Override
    public void characters(char ch[], int start, int length) { //Take specific action for a tag!
        if(in_nametag &&in_placemarktag)  //get the name for something, but first figure out outer tag
            tobeDrawn.add(new String(ch)); //ch should contain the name in the name tag. use a map instead?

        if(in_styletag) { //get the contents in the PolyStyle tag. Do we know what style?

        }

        if(in_coordinatestag){ //Add all coordinates inside
            boolean got_latlng = false, first_comma = false, second_comma = false;
            int i = start;
            double lat = 0, Long = 0;

            //First check for a -, then find the first comma. Use Array.copy to get char from - to before the comma, this is Long.
            //Then find second comma, get char from after the first comma to before the second one, this is lat.
            //Finally, increment index by 3 to get to the
            while(i < length) {

            }
        }
    }
}
