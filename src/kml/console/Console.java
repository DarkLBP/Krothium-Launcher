package kml.console;

import kml.Constants;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Console {
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private boolean enabled = true;
    private PrintWriter writer;
    private final File log;

    public Console() {
        this.cleanOldLogs();
        this.log = new File(Constants.APP_LOGS, "krothium-unclosed-" + System.currentTimeMillis() + ".log");
        try {
            this.writer = new ConsoleWriter(this.log);
        } catch (IOException ex) {
            this.enabled = false;
            System.out.println("Failed to start log system.");
            ex.printStackTrace();
        }
    }

    private void cleanOldLogs() {
        File[] logFiles = Constants.APP_LOGS.listFiles();
        if (logFiles != null) {
            Arrays.sort(logFiles, Collections.reverseOrder());
            int count = 0;
            for (File file : logFiles) {
                String name = file.getName();
                if (name.startsWith("krothium-unclosed")) {
                    if (!file.delete()) {
                        System.out.println("Failed to delete unclosed log file: " + name);
                    }
                } else if (name.startsWith("krothium")) {
                    if (count == Constants.APP_KEEP_LOGS - 1) {
                        if (!file.delete()) {
                            System.out.println("Failed to delete old log file: " + name);
                        }
                    } else {
                        count++;
                    }
                }
            }
        }
    }

    /**
     * Prints something to the output channel
     * @param info The Object to be printed
     */
    public final void print(Object info) {
        if (this.enabled) {
            this.writer.println('[' + this.dateFormat.format(new Date()) + "] " + info);
        }
    }

    /**
     * Gets the console writer
     * @return The console writer
     */
    public PrintWriter getWriter() {
        return this.writer;
    }

    /**
     * Closes the log file
     */
    public final void close() {
        if (this.enabled) {
            this.writer.close();
            File target = new File(Constants.APP_LOGS, this.log.getName().replace("-unclosed", ""));
            this.log.renameTo(target);
        }
    }
}
