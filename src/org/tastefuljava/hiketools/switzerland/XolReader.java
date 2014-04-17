/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tastefuljava.hiketools.switzerland;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Stack;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.tastefuljava.hiketools.geo.TrackPoint;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 *
 * @author perrym
 */
public class XolReader extends DefaultHandler {
    private static final DateFormat TIME_FORMAT
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DateFormat TIME_FORMAT2
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");

    private List<TrackPoint> track = new ArrayList<TrackPoint>();
    private double x;
    private double y;
    private double h;
    private Date time;
    private Stack<String> shapeTypes = new Stack();

    public static TrackPoint[] readTrack(InputStream in) throws IOException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setValidating(true);
            SAXParser parser = factory.newSAXParser();
            XolReader reader = new XolReader();
            parser.parse(in, reader);
            return reader.getTrack();
        } catch (ParserConfigurationException e) {
            throw new IOException(e.getMessage());
        } catch (SAXException e) {
            throw new IOException(e.getMessage());
        }
    }

    public static TrackPoint[] readTrack(File file) throws IOException {
        InputStream in = new FileInputStream(file);
        try {
            return readTrack(in);
        } finally {
            in.close();
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName,
            Attributes attributes) throws SAXException {
        if (qName.equals("point")) {
            x = Double.parseDouble(attributes.getValue("x"));
            y = Double.parseDouble(attributes.getValue("y"));
        } else if (qName.equals("shape")) {
            String type = attributes.getValue("type");
            if ("waypoint".equals(type) && !shapeTypes.isEmpty()
                    && "polyline".equals(shapeTypes.peek())) {
                String s = attributes.getValue("timestamp");
                if (s != null) {
                    try {
                        time = TIME_FORMAT.parse(s);
                    } catch (ParseException e) {
                        try {
                            time = TIME_FORMAT2.parse(s);
                        } catch (ParseException f) {
                            throw new SAXException("Invalid timestamp " + s);
                        }
                    }
                }
                s = attributes.getValue("alt");
                if (s != null) {
                    h = s == null ? 0 : Double.parseDouble(s);
                }
            }
            shapeTypes.add(type);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName)
            throws SAXException {
        if (qName.equals("shape")) {
            String type = shapeTypes.pop();
            if ("waypoint".equals(type) && !shapeTypes.isEmpty()
                    && "polyline".equals(shapeTypes.peek())) {
                track.add(Mn03Point.toWGS84(x, y, h, time));
            }
        }
    }

    private TrackPoint[] getTrack() {
        return track.toArray(new TrackPoint[track.size()]);
    }
}
