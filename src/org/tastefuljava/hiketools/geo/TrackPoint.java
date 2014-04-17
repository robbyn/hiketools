package org.tastefuljava.hiketools.geo;

import java.util.Date;

public class TrackPoint {
    private double lat;
    private double lng;
    private double h;
    private Date time;

    public TrackPoint(double lat, double lng, double h, Date time) {
        this.lat = lat;
        this.lng = lng;
        this.h = h;
        this.time = time;
    }

    public double getLat() {
        return lat;
    }

    public double getLng() {
        return lng;
    }

    public double getH() {
        return h;
    }

    public Date getTime() {
        return time;
    }
}
