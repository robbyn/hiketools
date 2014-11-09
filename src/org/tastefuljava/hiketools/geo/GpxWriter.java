package org.tastefuljava.hiketools.geo;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class GpxWriter {
    private static final DateFormat TIME_FORMAT
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DecimalFormat NUMBER_FORMAT
            = new DecimalFormat("0.00000000");

    public static void writeTrack(TrackPoint points[], File file)
            throws IOException {
        XMLWriter out = new XMLWriter(file);
        try {
            writeTrack(points, out);
        } finally {
            out.close();
        }
    }

    public static void writeTrack(TrackPoint points[], Writer writer) {
        writeTrack(points, new XMLWriter(writer));
    }

    public static void writeTrack(TrackPoint points[], PrintWriter writer) {
        writeTrack(points, new XMLWriter(writer));
    }

    public static void writeTrack(TrackPoint points[], XMLWriter out) {
        out.startTag("gpx");
        out.attribute("xmlns","http://www.topografix.com/GPX/1/1");
        out.attribute("version", "1.1");
        out.attribute("creator","GeoCode/001 http://www.perry.ch/rando");
        out.attribute("xmlns:xsi","http://www.w3.org/2001/XMLSchema-instance");
        out.attribute("xmlns:gpxdata","http://www.cluetrust.com/XML/GPXDATA/1/0");
        out.attribute("xmlns:geocache","http://www.groundspeak.com/cache/1/0");
        out.attribute("xsi:schemaLocation","http://www.topografix.com/GPX/1/1"
                + " http://www.topografix.com/GPX/1/1/gpx.xsd"
                + " http://www.cluetrust.com/XML/GPXDATA/1/0"
                + " http://www.cluetrust.com/Schemas/gpxdata10.xsd");
        out.startTag("trk");
        out.startTag("name");
        out.data("Track");
        out.endTag();
        out.startTag("trkseg");
        for (TrackPoint pt: points) {
            out.startTag("trkpt");
            out.attribute("lat", NUMBER_FORMAT.format(pt.getLat()));
            out.attribute("lon", NUMBER_FORMAT.format(pt.getLng()));
            out.startTag("ele");
            out.data(NUMBER_FORMAT.format(pt.getH()));
            out.endTag();
            if (pt.getTime() != null) {
                out.startTag("time");
                out.data(TIME_FORMAT.format(pt.getTime()));
                out.endTag();
            }
            out.endTag();
        }
        out.endTag();
        out.endTag();
        out.endTag();
    }
}
