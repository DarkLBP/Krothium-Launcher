package kml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.zip.GZIPOutputStream;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class Console {
    private boolean enabled = true;
    private boolean timestamps = false;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private final ByteArrayOutputStream data = new ByteArrayOutputStream();
    private GZIPOutputStream cdata;
    private boolean compressed;
    private Date date = new Date();
    public Console(){
        cdata = null;
        try {
            cdata = new GZIPOutputStream(data);
            compressed = true;
        } catch (IOException ex) {
            compressed = false;
        }
    }
    public void setEnabled(boolean value){this.enabled = value;}
    public void includeTimestamps(boolean value){this.timestamps = value;}
    public void printInfo(Object info){
        if (enabled){
            date = new Date();
            Object inf = (timestamps) ? ("[" + dateFormat.format(date) + "] " + info) : info;
            try {
                byte[] raw = (inf.toString() + System.lineSeparator()).getBytes();
                if (compressed){
                    cdata.write(raw);
                } else {
                    data.write(raw);
                }
            } catch (IOException ex) {}
            System.out.println(inf);
            System.out.flush();
        }
    }
    public void printError(Object error){
        if (enabled){
            date = new Date();
            Object err = (timestamps) ? ("[" + dateFormat.format(date) + "] " + error) : error;
            try {
                byte[] raw = (err.toString() + System.lineSeparator()).getBytes();
                if (compressed){
                    cdata.write(raw);
                } else {
                    data.write(raw);
                }
            } catch (IOException ex) {}
            System.err.println(err);
            System.err.flush();
        }
    }
    public void stopCompressing(){
        if (compressed){
            try{
                cdata.close();
                compressed = false;
            } catch (Exception ex){}
        }
    }
    public byte[] getData(){
        return data.toByteArray();
    }
    public boolean usesCompression(){return this.compressed;}
}
