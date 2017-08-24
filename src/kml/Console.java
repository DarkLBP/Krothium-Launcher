package kml;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Objects;

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
        File logFolder = new File(Constants.APPLICATION_WORKING_DIR, "logs");
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
        log = new File(Constants.APPLICATION_WORKING_DIR, "logs" + File.separator + "krothium-unclosed-" + System.currentTimeMillis() + ".log");
        if (!log.getParentFile().exists()) {
            log.getParentFile().mkdirs();
        }
        try {
            this.data = new FileOutputStream(log);
        } catch (IOException ex) {
            this.enabled = false;
        }
    }

    public void printInfo(Object info) {
        if (this.enabled) {
            writeData("[" + dateFormat.format(new Date()) + "] " + info);
            System.out.println(info);
        }
    }

    public void printError(Object error) {
        if (this.enabled) {
            writeData("[" + dateFormat.format(new Date()) + "] " + error);
            System.err.println(error);
        }
    }

    private void writeData(Object data) {
        try {
            byte[] raw = (data + System.lineSeparator()).getBytes();
            this.data.write(raw);
        } catch (IOException ignored) {
            System.err.println("Failed to write log data.");
            this.enabled = false;
        }
    }

    public void close() {
        if (this.enabled) {
            try {
                this.data.close();
                this.log.renameTo(new File(this.log.getAbsolutePath().replace("-unclosed", "")));
            } catch (Exception ignored) {
            }
        } else {
            try {
                this.data.close();
                this.log.delete();
            } catch (Exception ignored) {
            }
        }
    }
}
