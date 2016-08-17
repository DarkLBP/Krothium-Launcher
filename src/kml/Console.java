package kml;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class Console {
    private boolean enabled = true;
    private boolean timestamps = false;
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private final ByteArrayOutputStream data = new ByteArrayOutputStream();
    private Date date = new Date();
    
    public void setEnabled(boolean value){this.enabled = value;}
    public void includeTimestamps(boolean value){this.timestamps = value;}
    public void printInfo(Object info){
        if (enabled){
            date = new Date();
            Object inf = (timestamps) ? ("[" + dateFormat.format(date) + "] " + info) : info;
            try {
                data.write((inf.toString() + System.lineSeparator()).getBytes());
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
                data.write((err.toString() + System.lineSeparator()).getBytes());
            } catch (IOException ex) {}
            System.err.println(err);
            System.err.flush();
        }
    }
    public byte[] getData(){
        return data.toByteArray();
    }
}
