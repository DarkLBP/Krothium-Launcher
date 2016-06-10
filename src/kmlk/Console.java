package kmlk;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @website http://krotium.com
 * @author DarkLBP
 */

public class Console {
    private boolean enabled = false;
    private boolean timestamps = false;
    private DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private Date date = new Date();
    private final Kernel kernel;
    public Console (Kernel k)
    {
        this.kernel = k;
    }
    public void setEnabled(boolean value)
    {
        this.enabled = value;
    }
    public void includeTimestamps(boolean value)
    {
        this.timestamps = value;
    }
    public void printInfo(Object info)
    {
        if (enabled)
        {
            date = new Date();
            Object inf = (timestamps) ? ("[" + dateFormat.format(date) + "] " + info) : info;
            System.out.println(inf);
            System.out.flush();
        }
    }
    public void printError(Object error)
    {
        if (enabled)
        {
            date = new Date();
            Object err = (timestamps) ? ("[" + dateFormat.format(date) + "] " + error) : error;
            System.err.println(err);
            System.err.flush();
        }
    }
}
