package kml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Console {
    private final DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
    private boolean enabled = true;
    private FileOutputStream data;
    private final File log;

    public Console() {
        File logFolder = Constants.APPLICATION_LOGS;
        if (logFolder.exists() && logFolder.isDirectory()) {
            File[] logFiles = logFolder.listFiles();
            if (logFiles != null && logFiles.length > 0) {
                Arrays.sort(logFiles);
                int count = 0;
                for (File f : logFiles) {
                    if (f.isFile()) {
                        String name = f.getName();
                        if (name.startsWith("krothium-unclosed")) {
                            if (f.delete()) {
                                System.out.println("Successfully deleted unclosed log file: " + name);
                            } else {
                                System.out.println("Failed to delete unclosed log file: " + name);
                            }
                        } else if (name.startsWith("krothium")) {
                            count++;
                        }
                    }
                }
                if (count > Constants.KEEP_OLD_LOGS) {
                    int toDelete = count - Constants.KEEP_OLD_LOGS;
                    for (int i = 0; i < toDelete; i++) {
                        for (File f : logFiles) {
                            if (f.isFile()) {
                                String name = f.getName();
                                if (name.startsWith("krothium") && !name.contains("-unclosed-")) {
                                    if (f.delete()) {
                                        System.out.println("Successfully deleted old log file: " + name);
                                    } else {
                                        System.out.println("Failed to delete old log file: " + name);
                                    }
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        this.log = new File(Constants.APPLICATION_LOGS, "krothium-unclosed-" + System.currentTimeMillis() + ".log");
        if (!this.log.getParentFile().exists()) {
            this.log.getParentFile().mkdirs();
        }
        try {
            this.data = new FileOutputStream(this.log);
        } catch (IOException ex) {
            this.enabled = false;
        }
    }

    /**
     * Prints something to the output channel
     * @param info The Object to be printed
     */
    public final void printInfo(Object info) {
        if (this.enabled) {
            this.writeData('[' + this.dateFormat.format(new Date()) + "] " + info);
            System.out.println(info);
        }
    }

    /**
     * Prints something to the error channel
     * @param error The Object to be printed
     */
    public final void printError(Object error) {
        if (this.enabled) {
            this.writeData('[' + this.dateFormat.format(new Date()) + "] " + error);
            System.err.println(error);
        }
    }

    /**
     * Writes data to the log
     * @param data The data to be written
     */
    private void writeData(Object data) {
        try {
            byte[] raw = (data + System.lineSeparator()).getBytes();
            this.data.write(raw);
        } catch (IOException ignored) {
            System.err.println("Failed to write log data.");
            this.enabled = false;
        }
    }

    /**
     * Closes the log file
     */
    public final void close() {
        if (this.enabled) {
            try {
                this.data.close();
                this.log.renameTo(new File(this.log.getAbsolutePath().replace("-unclosed", "")));
            } catch (IOException ignored) {
            }
        } else {
            try {
                this.data.close();
                this.log.delete();
            } catch (IOException ignored) {
            }
        }
    }
}
