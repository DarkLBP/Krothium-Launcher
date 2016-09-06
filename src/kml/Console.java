package kml;

import java.io.File;
import java.io.FileOutputStream;
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
    private FileOutputStream data;
    private GZIPOutputStream cdata;
    private Date date = new Date();
    private final Kernel kernel;
    public Console(Kernel instance){
        this.kernel = instance;
        this.cdata = null;
        this.data = null;
        try {
            File log = new File(this.kernel.getWorkingDir() + File.separator + "logs" + File.separator + "weblauncher.log.gz");
            this.data = new FileOutputStream(log);
            this.cdata = new GZIPOutputStream(data);
        } catch (IOException ex) {
            this.enabled = false;
        }
    }
    public boolean isEnabled(){return this.enabled;}
    public void setEnabled(boolean value){
        if (this.cdata == null || this.data == null){
            this.enabled = false;
        } else {
            this.enabled = value;
        }
    }
    public void includeTimestamps(boolean value){this.timestamps = value;}
    public void printInfo(Object info){
        if (this.enabled){
            date = new Date();
            Object inf = (timestamps) ? ("[" + dateFormat.format(date) + "] " + info) : info;
            try {
                byte[] raw = (inf.toString() + System.lineSeparator()).getBytes();
                cdata.write(raw);
            } catch (IOException ex) {}
            System.out.println(inf);
            System.out.flush();
        }
    }
    public void printError(Object error){
        if (this.enabled){
            date = new Date();
            Object err = (timestamps) ? ("[" + dateFormat.format(date) + "] " + error) : error;
            try {
                byte[] raw = (err.toString() + System.lineSeparator()).getBytes();
                cdata.write(raw);
            } catch (IOException ex) {}
            System.err.println(err);
            System.err.flush();
        }
    }
    public boolean close(){
        try{
            this.cdata.close();
            return true;
        } catch (Exception ex){
            return false;
        }
    }
}
