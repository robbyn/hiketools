/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.tastefuljava.hiketools.hikegen;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;

/**
 *
 * @author maurice
 */
public class Util {
    public long round(double value) {
        return Math.round(value);
    }

    public String formatDouble(double value, String pattern) {
        DecimalFormat format = getDecimalFormat(pattern);
        return format.format(value);
    }

    private static DecimalFormat getDecimalFormat(String pattern) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols();
        symbols.setDecimalSeparator('.');
        symbols.setGroupingSeparator('\'');
        DecimalFormat format = new DecimalFormat(pattern);
        format.setParseBigDecimal(true);
        format.setDecimalFormatSymbols(symbols);
        return format;
    }
}
