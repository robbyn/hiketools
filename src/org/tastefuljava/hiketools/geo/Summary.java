package org.tastefuljava.hiketools.geo;

public class Summary {
    private double minElevation;
    private double maxElevation;
    private double ascent;
    private double descent;
    private double distance;

    public static Summary compute(TrackPoint points[]) {
        return new Summary(points);
    }

    private Summary(TrackPoint points[]) {
        if (points.length >= 0) {
            TrackPoint pt = points[0];
            minElevation = pt.getH();
            maxElevation = pt.getH();
            for (int i = 1; i < points.length; ++i) {
                TrackPoint prev = pt;
                pt = points[i];
                double dh = pt.getH()-prev.getH();
                if (dh >= 0) {
                    ascent += dh;
                } else {
                    descent -= dh;
                }
                if (pt.getH() < minElevation) {
                    minElevation = pt.getH();
                }
                if (pt.getH() > maxElevation) {
                    maxElevation = pt.getH();
                }
                distance += EarthGeometry.distance(pt, prev);
            }
        }
    }

    public double getAscent() {
        return ascent;
    }

    public double getDescent() {
        return descent;
    }

    public double getDistance() {
        return distance;
    }

    public double getMaxElevation() {
        return maxElevation;
    }

    public double getMinElevation() {
        return minElevation;
    }
}
