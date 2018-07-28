package kml.console;

import java.io.*;

public class ConsoleWriter extends PrintWriter {
    public ConsoleWriter(Writer out) {
        super(out);
    }

    public ConsoleWriter(Writer out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public ConsoleWriter(OutputStream out) {
        super(out);
    }

    public ConsoleWriter(OutputStream out, boolean autoFlush) {
        super(out, autoFlush);
    }

    public ConsoleWriter(String fileName) throws FileNotFoundException {
        super(fileName);
    }

    public ConsoleWriter(String fileName, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(fileName, csn);
    }

    public ConsoleWriter(File file) throws FileNotFoundException {
        super(file);
    }

    public ConsoleWriter(File file, String csn) throws FileNotFoundException, UnsupportedEncodingException {
        super(file, csn);
    }

    @Override
    public void println(boolean x) {
        System.out.println(x);
        super.println(x);
    }

    @Override
    public void println(char x) {
        System.out.println(x);
        super.println(x);
    }

    @Override
    public void println(int x) {
        System.out.println(x);
        super.println(x);
    }

    @Override
    public void println(long x) {
        System.out.println(x);
        super.println(x);
    }

    @Override
    public void println(float x) {
        System.out.println(x);
        super.println(x);
    }

    @Override
    public void println(double x) {
        System.out.println(x);
        super.println(x);
    }

    @Override
    public void println(char[] x) {
        System.out.println(x);
        super.println(x);
    }

    @Override
    public void println(String x) {
        System.out.println(x);
        super.println(x);
    }

    @Override
    public void println(Object x) {
        System.out.println(x);
        super.println(x);
    }

    @Override
    public void println() {
        super.println();
        super.flush();
    }
}
