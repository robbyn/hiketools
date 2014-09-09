package org.tastefuljava.hiketools.geo;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import javax.imageio.ImageIO;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYAreaRenderer;
import org.jfree.data.xy.DefaultTableXYDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.graphics2d.svg.SVGGraphics2D;
import org.jfree.ui.RectangleInsets;

public class Profile {
    public static void writePNG(TrackPoint points[], int width, int height,
            File file) throws IOException {
        JFreeChart chart = createChart(points);
        BufferedImage img = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        try {
            chart.draw(g, new Rectangle(0, 0, width, height));
        } finally {
            g.dispose();
        }
        ImageIO.write(img, "png", file);
    }

    public static void writeSVG(TrackPoint points[], int width, int height,
            File file) throws IOException {
        JFreeChart chart = createChart(points);
        SVGGraphics2D g = new SVGGraphics2D(width, height);
        chart.draw(g, new Rectangle(0, 0, width, height));
        OutputStream stream = new FileOutputStream(file);
        try {
            Writer out = new OutputStreamWriter(stream, "UTF-8");
            try {
                out.write(g.getSVGDocument());
            } finally {
                out.close();
            }
        } finally {
            stream.close();
        }
    }

    public static JFreeChart createChart(TrackPoint points[]) {
        return createChart(createDataset(points));
    }

    private static JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createXYAreaChart(
            null,  // chart title
            null,                       // domain axis label
            null,                       // range axis label
            dataset,                         // data
            PlotOrientation.VERTICAL,        // the plot orientation
            false,                            // legend
            false,                            // tooltips
            false                            // urls
        );
        XYPlot plot = (XYPlot) chart.getPlot();
        NumberAxis axis = (NumberAxis)plot.getRangeAxis();
        axis.setAutoRangeIncludesZero(false);
        DecimalFormatSymbols syms = new DecimalFormatSymbols();
        syms.setGroupingSeparator('\'');
        syms.setDecimalSeparator('.');
        DecimalFormat format = new DecimalFormat("#,##0'm'");
        format.setDecimalFormatSymbols(syms);
        axis.setNumberFormatOverride(format);
        axis = (NumberAxis)plot.getDomainAxis();
        format = new DecimalFormat("#,##0.#'km'");
        format.setDecimalFormatSymbols(syms);
        axis.setNumberFormatOverride(format);
        XYAreaRenderer renderer = (XYAreaRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.BLACK);
        plot.setRangeGridlinePaint(Color.BLACK);
        plot.setInsets(RectangleInsets.ZERO_INSETS);
        plot.setOutlineVisible(false);
        return chart;
    }

    private static XYDataset createDataset(TrackPoint points[]) {
        DefaultTableXYDataset dataset = new DefaultTableXYDataset();
        XYSeries series = new XYSeries("Series 1", true, false);
        TrackPoint prev = null;
        double d = 0;
        for (TrackPoint pt: points) {
            if (prev != null) {
                d += EarthGeometry.distance(pt, prev);
            }
            double km = d/1000.0;
            if (series.indexOf(km) < 0) {
                series.add(km, pt.getH());
            }
            prev = pt;
        }
        dataset.addSeries(series);
        System.out.println("Distance: " + d);
        return dataset;
    }
}
