package kml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
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
    private Date date;
    private File log;
    public Console(Kernel instance){
        final Kernel kernel = instance;
        this.cdata = null;
        this.data = null;
        File logFolder = new File(kernel.getWorkingDir() + File.separator + "logs");
        if (logFolder.exists() && logFolder.isDirectory()){
            File[] logFiles = logFolder.listFiles();
            if (logFiles != null && logFiles.length > 0){
                Arrays.sort(logFiles);
                int count = 0;
                for (File f : logFiles){
                    if (f.isFile()){
                        String name = f.getName();
                        if (name.startsWith("weblauncher-unclosed")){
                            if (f.delete()){
                                this.printInfo("Successfully deleted unclosed log file: " + name);
                            } else {
                                this.printError("Failed to delete unclosed log file: " + name);
                            }
                        } else if (name.startsWith("weblauncher")){
                            count++;
                        }
                    }
                }
                if (count > Constants.KEEP_OLD_LOGS){
                    int toDelete = count - Constants.KEEP_OLD_LOGS;
                    for (int i = 0; i < toDelete; i++){
                        for (File f : logFiles){
                            if (f.isFile()){
                                String name = f.getName();
                                if (name.startsWith("weblauncher") && name.contains("-unclosed-")){
                                    if (f.delete()){
                                        this.printInfo("Successfully deleted old log file: " + name);
                                    } else {
                                        this.printError("Failed to delete old log file: " + name);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        try {
            log = new File(kernel.getWorkingDir() + File.separator + "logs" + File.separator + "weblauncher-unclosed-" + String.valueOf(System.nanoTime()) + ".log.gz");
            if (!log.getParentFile().exists()){
                log.getParentFile().mkdirs();
            }
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
            } catch (IOException ignored){
                System.out.println("Failed to write log data.");
                this.enabled = false;
            }
            System.out.println(inf);
        }
    }
    public void printError(Object error){
        if (this.enabled){
            date = new Date();
            Object err = (timestamps) ? ("[" + dateFormat.format(date) + "] " + error) : error;
            try {
                byte[] raw = (err.toString() + System.lineSeparator()).getBytes();
                cdata.write(raw);
            } catch (IOException ignored) {
                System.out.println("Failed to write log data.");
                this.enabled = false;
            }
            System.err.println(err);
        }
    }
    public boolean close(){
        if (this.enabled){
            try{
                this.cdata.close();
                return this.log.renameTo(new File(this.log.getAbsolutePath().replace("-unclosed", "")));
            } catch (Exception ex){
                return false;
            }
        } else {
            try{
                this.cdata.close();
                return this.log.delete();
            } catch (Exception ex){
                return false;
            }
        }
    }
}
