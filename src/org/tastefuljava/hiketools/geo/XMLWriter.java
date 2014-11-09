package org.tastefuljava.hiketools.geo;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Stack;

public class XMLWriter {
    private final PrintWriter _out;
    private final Stack _tags = new Stack();
    private boolean _format = true;
    private boolean _inAttributes = false;
    private boolean _hasSubtags = false;
    private boolean _lnBefore = false;

    public XMLWriter(PrintWriter out) {
        _out = out;
    }

    public XMLWriter(Writer out) {
        this(new PrintWriter(out));
    }

    public XMLWriter(OutputStream out) {
        this(new OutputStreamWriter(out));
    }

    public XMLWriter(OutputStream out, String encoding) throws IOException {
        this(new OutputStreamWriter(out, encoding));
    }

    public XMLWriter(File file) throws IOException {
        this(new FileWriter(file));
    }

    public void close() {
        _out.close();
    }

    public boolean getFormat() {
        return _format;
    }

    public void setFormat(boolean newValue) {
        _format = newValue;
    }

    public void startTag(String name) {
        if (_inAttributes) {
            _out.print('>');
        }
        if (_format && !_lnBefore) {
            _out.println();
        }
        indent();
        _out.print('<');
        _out.print(name);
        _inAttributes = true;
        _hasSubtags = false;
        _lnBefore = false;
        _tags.push(name);
    }

    public void attribute(String name, String value) {
        if (!_inAttributes) {
            throw new RuntimeException("Attributes not allowed here");
        }
        if (value != null) {
            _out.print(" ");
            _out.print(name);
            _out.print("=\"");
            printEscaped(value);
            _out.print('"');
        }
    }

    public void data(String data) {
        if (_inAttributes) {
            _out.print('>');
            _inAttributes = false;
        }
        printEscaped(data);
    }

    public void endTag() {
        String name = (String) _tags.pop();
        if (_inAttributes) {
            _out.print(" />");
            _inAttributes = false;
        } else {
            if (_format && _hasSubtags) {
                if (!_lnBefore) {
                    _out.println();
                }
                indent();
            }
            _out.print("</");
            _out.print(name);
            _out.print('>');
        }
        if (_format) {
            _out.println();
        }
        _lnBefore = true;
        _hasSubtags = true;
    }

    public void writeTagged(String tag, String data) {
        if (data != null) {
            startTag(tag);
            data(data);
            endTag();
        }
    }

    private void indent() {
        if (_format) {
            for (int i = 0; i < _tags.size(); ++i) {
                _out.print("   ");
            }
        }
    }

    private void printEscaped(String value) {
        char chars[] = value.toCharArray();
        for (int i = 0; i < chars.length; ++i) {
            char c = chars[i];
            _lnBefore = false;
            switch (c) {
                case '<':
                    _out.print("&lt;");
                    break;
                case '>':
                    _out.print("&gt;");
                    break;
                case '&':
                    _out.print("&amp;");
                    break;
                case '"':
                    _out.print("&quot;");
                    break;
                case '\n':
                    _out.println();
                    _lnBefore = true;
                    break;
                default:
                    if (c >= 32 && c < 127) {
                        _out.print(c);
                    } else {
                        _out.print("&#" + Integer.toString(c) + ";");
                    }
                    break;
            }
        }
    }
}
