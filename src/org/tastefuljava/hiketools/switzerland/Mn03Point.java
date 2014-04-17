/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tastefuljava.hiketools.switzerland;

import java.util.Date;
import org.tastefuljava.hiketools.geo.TrackPoint;

public class Mn03Point {
    private double x;
    private double y;
    private double h;
    private Date time;

    public static TrackPoint toWGS84(double x, double y, double h, Date time) {
                y = y*1.E-6 - 0.2;
        x = x*1.E-6 - 0.6;
        double x2 = y*y;
        double x3 = x2*y;
        double y2 = x*x;
        double y3 = y2*x;
        double lon = 2.6779094
            + 4.728982*x
            + 0.791484*x*y
            + 0.1306*x*x2
            - 0.0436*y3;
        double lat = 16.9023892
            + 3.238272*y
            - 0.270978*y2
            - 0.002528*x2
            - 0.0447*y2*y
            - 0.0140*x3;
        return new TrackPoint(lat/0.3600, lon/0.3600, h, time);
    }

    public Mn03Point(double x, double y, double h, Date time) {
        this.x = x;
        this.y = y;
        this.h = h;
        this.time = time;
    }

    public Mn03Point(TrackPoint pt) {
        fromWGS84(pt.getLat(), pt.getLng(), pt.getH(), pt.getTime());
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getH() {
        return h;
    }

    public Date getTime() {
        return time;
    }

    public TrackPoint toWGS84() {
        return Mn03Point.toWGS84(x, y, h, time);
    }

    private void fromWGS84(double lat, double lon, double ele,
            Date tm) {
        lat = lat*0.36-16.902866;
        lon = lon*0.36-2.67825;
        double lat2 = lat*lat;
        double lat3 = lat2*lat;
        double lon2 = lon*lon;
        double lon3 = lon2*lon;
        x = 600072.37
                + 211455.93*lon
                - 10938.51*lon*lat
                - 0.36*lon*lat2
                - 44.54*lon3;
        y = 200147.07
                + 308807.95*lat
                + 3745.25*lon2
                + 76.63*lat2
                - 194.56*lon2*lat
                + 119.79*lat3;
        h = ele;
        time = tm;
    }
}
