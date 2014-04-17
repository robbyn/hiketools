package org.tastefuljava.hiketools.hikegen;

import org.tastefuljava.hiketools.geo.ElevationService;
import org.tastefuljava.hiketools.geo.GMapEncoder;
import org.tastefuljava.hiketools.geo.GpxReader;
import org.tastefuljava.hiketools.geo.GpxWriter;
import org.tastefuljava.hiketools.geo.TrackPoint;
import org.tastefuljava.hiketools.geo.Profile;
import org.tastefuljava.hiketools.geo.Summary;
import org.tastefuljava.hiketools.switzerland.Mn03Point;
import org.tastefuljava.hiketools.switzerland.XolReader;
import org.tastefuljava.hiketools.switzerland.XolWriter;
import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.tastefuljava.hiketools.geo.EarthGeometry;

public class Main {
    private static final Logger LOG = Logger.getLogger(Main.class.getName());

    public static void main(String[] args) {
        boolean gpx = false;
        boolean xol = false;
        boolean ele = false;
        boolean sim = false;
        boolean bas = false;
        double tol = 1;
        File baseDir = null;
        for (String arg: args) {
            if (sim) {
                sim = false;
                tol = Double.parseDouble(arg);
            } else if (bas) {
                bas = false;
                baseDir = new File(arg);
            } else if (arg.equalsIgnoreCase("-gpx")) {
                gpx = true;
            } else if (arg.equalsIgnoreCase("-xol")) {
                xol = true;
            } else if (arg.equalsIgnoreCase("-ele")) {
                ele = true;
            } else if (arg.equalsIgnoreCase("-sim")) {
                sim = true;
            } else if (arg.equalsIgnoreCase("-bas")) {
                bas = true;
            } else {
                try {
                    processFile(new File(arg), gpx, xol, ele, tol, baseDir);
                } catch (Throwable e) {
                    LOG.log(Level.SEVERE, "Error processing " + arg, e);
                }
            }
        }
    }

    private static void processFile(File file, boolean gpx, boolean xol,
            boolean ele, double epsilon, File baseDir) throws Exception {
        File dir = file.getParentFile();
        TrackPoint trk[];
        String name = file.getName();
        if (name.toLowerCase().endsWith(".xol")) {
            trk = XolReader.readTrack(file);
        } else {
            trk = GpxReader.readTrack(file);
        }
        int maps[] = mapNumbers(trk);
        System.out.println("Read " + trk.length + " points from " + file);
        if (epsilon > 0) {
            trk = EarthGeometry.simplify(trk, epsilon);
            System.out.println("Simplified to " + trk.length + " points");
        }
        if (ele) {
            System.out.println("Getting elevations from web service");
            ElevationService.getElevations(trk);
        }
        int lastDot = name.lastIndexOf('.');
        if (lastDot > 0) {
            name = name.substring(0, lastDot);
        }
        if (gpx) {
            GpxWriter.writeTrack(trk, new File(dir, name + ".gpx"));
        }
        if (xol) {
            XolWriter.writeTrack(trk, new File(dir, name + ".xol"));
        }
        Summary summary = Summary.compute(trk);
        Profile.writeProfile(trk, 600, 200,
                new File(dir, "profile.png"));
        VelocityContext context = new VelocityContext();
        context.put("name", name);
        context.put("title", "Map for " + name);
        if (baseDir != null) {
            context.put("album-path", backwardPath(dir, baseDir));
            context.put("path", relativePath(dir, baseDir));
        }
        context.put("track",
                GMapEncoder.escapeQuotes(GMapEncoder.encodePoints(trk)));
        context.put("color", GMapEncoder.encodeColor(Color.RED));
        context.put("weight", 5);
        context.put("opacity", "0.5");
        context.put("summary", summary);
        context.put("util", new Util());
        context.put("maps", maps);
        Properties props = loadProps();
        for (String prop: props.stringPropertyNames()) {
            context.put(prop, props.getProperty(prop));
        }
        VelocityEngine engine = new VelocityEngine();
        engine.setProperty(VelocityEngine.RESOURCE_LOADER, "classpath");
        engine.setProperty("classpath." + VelocityEngine.RESOURCE_LOADER
                + ".class", ClasspathResourceLoader.class.getName());
        OutputStream stream = new FileOutputStream(
                new File(dir, name + ".html"));
        try {
            Writer out = new OutputStreamWriter(stream, "UTF-8");
            try {
                engine.mergeTemplate(
                        "template.html", "UTF-8", context, out);
            } finally {
                out.close();
            }
        } finally {
            stream.close();
        }
    }

    private static Properties loadProps() throws IOException {
        Properties props = new Properties();
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        InputStream in = cl.getResourceAsStream("hikegen.properties");
        if (in != null) {
            try {
                props.load(in);
            } finally {
                in.close();
            }
        }
        return props;
    }

    private static int[] mapNumbers(TrackPoint pts[]) {
        Set<Integer> maps = new HashSet<Integer>();
        for (TrackPoint pt: pts) {
            maps.add(mapNumber(new Mn03Point(pt)));
        }
        int result[] = new int[maps.size()];
        int i = 0;
        for (int n: maps) {
            result[i++] = n;
        }
        Arrays.sort(result);
        return result;
    }

    private static int mapNumber(Mn03Point pt) {
        return 1000 + 20*(int)Math.floor((302000-pt.getY())/12000)
                + (int)Math.floor((pt.getX()-480000)/17500);
    }

    private static String backwardPath(File dir, File baseDir)
            throws IOException {
        dir = dir.getCanonicalFile();
        baseDir = baseDir.getCanonicalFile();
        StringBuilder buf = new StringBuilder();
        while (dir != null) {
            if (dir.equals(baseDir)) {
                return buf.toString();
            }
            buf.insert(0, "../");
            dir = dir.getParentFile();
        }
        return null;
    }

    private static String relativePath(File dir, File baseDir)
            throws IOException {
        dir = dir.getCanonicalFile();
        baseDir = baseDir.getCanonicalFile();
        StringBuilder buf = new StringBuilder();
        while (dir != null) {
            if (dir.equals(baseDir)) {
                return buf.toString();
            }
            buf.insert(0, "/");
            buf.insert(0, URLEncoder.encode(dir.getName(), "UTF-8"));
            dir = dir.getParentFile();
        }
        return null;
    }
}
