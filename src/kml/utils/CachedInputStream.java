package kml.utils;

import java.io.*;

public class CachedInputStream extends InputStream {
    private BufferedInputStream inputStream;
    private BufferedOutputStream cachedStream;

    public CachedInputStream(InputStream in, File output) throws IOException {
        this.inputStream = new BufferedInputStream(in);
        this.cachedStream = new BufferedOutputStream(new FileOutputStream(output));
    }

    @Override
    public int read() throws IOException {
        int read = this.inputStream.read();
        if (read != -1) {
            this.cachedStream.write(read);
        }
        return read;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int read = this.inputStream.read(b, off, len);
        if (read != -1) {
            this.cachedStream.write(b, off, read);
        }
        return read;
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        int read = this.inputStream.read(buffer);
        if (read != -1) {
            this.cachedStream.write(buffer, 0, read);
        }
        return read;
    }

    @Override
    public void close() throws IOException {
        int remaining = this.inputStream.available();
        if (this.inputStream.available() > 0) {
            this.cachedStream.flush();
            byte[] r = new byte[remaining];
            this.read(r);
        }
        this.inputStream.close();
        this.cachedStream.close();
    }
}
