package org.tastefuljava.hiketools.geo;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;
import org.xml.sax.helpers.XMLReaderFactory;

public class ElevationService {
    private static final Logger LOG
            = Logger.getLogger(ElevationService.class.getName());

    private static final int CONNECT_TIMEOUT = 10000;
    private static final int READ_TIMEOUT = 10000;
    private static final int MAX_COUNT = 100;
    private static final String SERVICE_URL
            = "http://maps.google.com/maps/api/elevation/xml";

    public static void getElevations(TrackPoint[] results)
            throws IOException {
        int pos = 0;
        while (pos+MAX_COUNT <= results.length) {
            getElevations(results, pos, MAX_COUNT);
            pos += MAX_COUNT;
        }
        if (pos < results.length) {
            getElevations(results, pos, results.length-pos);
        }
    }

    private static void getElevations(TrackPoint trkpts[], int start,
            int count) throws IOException {
        for (int i = 0; i < 20; ++i) {
            String status = getElevations1(trkpts, start, count);
            if (status.equals("OK")) {
                return;
            } else if (status.equals("OVER_QUERY_LIMIT")) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                throw new IOException("Error getting elevations: " + status);
            }
        }
        throw new IOException("Error getting elevations: too many retries");
    }
        
    private static String getElevations1(TrackPoint trkpts[], int start,
            int count) throws IOException {
        String enc = GMapEncoder.encodePoints(trkpts, start, count);
        String url = SERVICE_URL + "?locations=enc:"
                + URLEncoder.encode(enc, "ASCII")
                + "&sensor=false";
        URL uploadUrl = new URL(url);
        HttpURLConnection con = (HttpURLConnection)uploadUrl.openConnection();
        try {
            con.setRequestMethod("GET");
            con.addRequestProperty("Content-Type",
                    "application/x-www-form-urlencoded");
            con.setConnectTimeout(CONNECT_TIMEOUT);
            con.setReadTimeout(READ_TIMEOUT);
            int code = con.getResponseCode();
            if (code != HttpURLConnection.HTTP_OK) {
                String msg = con.getResponseMessage();
                throw new IOException("HTTP Error " + code + ": " + msg);
            }
            InputStream stream = con.getInputStream();
            try {
                return parseResults(trkpts, start, stream);
            } finally {
                stream.close();
            }
        } catch (SAXException e) {
            LOG.log(Level.SEVERE, "Error parsing results", e);
            throw new IOException(e.getMessage());
        } finally {
            con.disconnect();
        }
    }

    private static String parseResults(TrackPoint trkpts[], int start,
            InputStream in) throws IOException, SAXException {
        XMLReader reader = null;
        try {
            reader = XMLReaderFactory.createXMLReader();
        } catch (SAXException ex) {
            reader = XMLReaderFactory.createXMLReader(
                    "org.apache.crimson.parser.XMLReaderImpl");
        }

        try {
            reader.setFeature("http://xml.org/sax/features/validation", false);
        } catch (SAXException e) {
            LOG.warning("Parser does not support validation feature");
        }

        Handler handler = new Handler(trkpts, start);
        reader.setContentHandler(handler);
        reader.setErrorHandler(handler);
        reader.parse(new InputSource(in));
        return handler.status;
    }

    private static class Handler extends DefaultHandler {
        private final TrackPoint[] trkpts;
        private int current = 0;
        private StringBuilder buf = new StringBuilder();
        private String status = "";

        private Handler(TrackPoint[] trkpts, int start) {
            this.trkpts = trkpts;
            current = start;
        }

        @Override
        public void startElement(String uri, String localName, String qName,
                Attributes attributes) throws SAXException {
            buf.setLength(0);
        }

        @Override
        public void endElement(String uri, String localName, String qName)
                throws SAXException {
            if (localName.equals("elevation")) {
                double ele = Double.parseDouble(buf.toString());
                TrackPoint src = trkpts[current];
                TrackPoint dst = new TrackPoint(src.getLat(), src.getLng(),
                        ele, src.getTime());
                trkpts[current] = dst;
                ++current;
            } else if (localName.equals("status")) {
                status = buf.toString();
                if (!status.equals("OK")) {
                    LOG.log(Level.SEVERE, "Status: {0}", status);
                }
            }
        }

        @Override
        public void characters(char[] ch, int start, int length)
                throws SAXException {
            buf.append(ch, start, length);
        }
    }
}
