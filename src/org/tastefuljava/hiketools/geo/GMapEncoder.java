package org.tastefuljava.hiketools.geo;

import java.awt.Color;

public class GMapEncoder {
    private static final char HEX[] = "0123456789ABCDEF".toCharArray();

    private GMapEncoder() {
        throw new UnsupportedOperationException("Instanciation not allowed");
    }

    public static String encodePoints(TrackPoint track[]) {
        return encodePoints(track, 0, track.length);
    }

    public static String encodePoints(TrackPoint[] track, int start, int count) {
        StringBuilder buf = new StringBuilder();
        if (count > 0) {
            int end = start+count;
            int prevLat = 0;
            int prevLng = 0;

            for (int i = start; i < end; i++) {
                TrackPoint point = track[i];

                int lat = (int) Math.floor(point.getLat()*1E5);
                int lng = (int) Math.floor(point.getLng()*1E5);

                encodeSigned(lat - prevLat, buf);
                encodeSigned(lng - prevLng, buf);

                prevLat = lat;
                prevLng = lng;
            }
        }
        return buf.toString();
    }

    private static void encodeSigned(int num, StringBuilder buf) {
        int sgn_num = num << 1;
        if (num < 0) {
            sgn_num = ~sgn_num;
        }
        encodeUnsigned(sgn_num, buf);
    }

    public static void encodeUnsigned(int num, StringBuilder buf) {
        while ((num & 0xFFFFFFE0) != 0) {
            buf.append((char) ((num & 0x1f | 0x20) + 63));
            num >>>= 5;
        }
        buf.append((char) (num + 63));
    }

    public static String encodeColor(Color col) {
        int rgb = col == null ? 0 : col.getRGB() & 0x00FFFFFF;
        char chars[] = new char[6];
        for (int i = 6; --i >= 0; ) {
            chars[i] = HEX[rgb%16];
            rgb /= 16;
        }
        StringBuilder buf = new StringBuilder();
        buf.append('#');
        buf.append(chars);
        return buf.toString();
    }

    public static String escapeQuotes(String s) {
        StringBuilder buf = new StringBuilder();
        for (char c: s.toCharArray()) {
            if (c == '"' || c == '\'' || c == '\\') {
                buf.append('\\');
            }
            buf.append(c);
        }
        return buf.toString();
    }
}
