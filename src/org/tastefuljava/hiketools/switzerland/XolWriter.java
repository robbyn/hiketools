package org.tastefuljava.hiketools.switzerland;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import org.tastefuljava.hiketools.geo.TrackPoint;
import org.tastefuljava.hiketools.geo.XMLWriter;

public class XolWriter {
    private static final DateFormat TIME_FORMAT
            = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
    private static final DecimalFormat NUMBER_FORMAT
            = new DecimalFormat("0.########");

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
        out.startTag("overlays");
        out.startTag("overlay");
        out.attribute("version", "1.0");
        writeCenter(points, out);
        out.startTag("shapes");
        writePolyline(points, out);
        out.endTag();
        out.endTag();
        out.endTag();
    }

    private static void writeCenter(TrackPoint points[], XMLWriter out) {
        int sumx = 0;
        int sumy = 0;
        for (TrackPoint pt: points) {
            Mn03Point tp = new Mn03Point(pt);
            sumx += tp.getX();
            sumy += tp.getY();
        }
        out.startTag("center");
        out.attribute("x", Integer.toString(sumx/points.length));
        out.attribute("y", Integer.toString(sumy/points.length));
        out.endTag();
    }

    private static void writePolyline(TrackPoint[] points, XMLWriter out) {
        out.startTag("shape");
        out.attribute("type", "polyline");
        out.attribute("name", "track");
        out.attribute("lineSize", "10");
        out.attribute("lineColor", "#FF0000");
        out.attribute("lineStyle", "solid");
        out.startTag("waypoints");
        for (TrackPoint pt: points) {
            writePoint(new Mn03Point(pt), out);
        }
        out.endTag();
        out.endTag();
    }

    private static void writePoint(Mn03Point pt, XMLWriter out) {
        out.startTag("shape");
        out.attribute("type", "waypoint");
        if (pt.getTime() != null) {
            out.attribute("timestamp", TIME_FORMAT.format(pt.getTime()));
        }
        out.attribute("alt", NUMBER_FORMAT.format(pt.getH()));
        out.startTag("points");
        out.startTag("point");
        out.attribute("x", NUMBER_FORMAT.format(pt.getX()));
        out.attribute("y", NUMBER_FORMAT.format(pt.getY()));
        out.endTag();
        out.endTag();
        out.endTag();
    }
}
