package kml;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
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
    private PrintWriter writer;
    private final File log;
    private final int KEEP_OLD_LOGS = 5;

    public Console() {
        File logFolder = Constants.APPLICATION_LOGS;
        if (logFolder.isDirectory()) {
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
                if (count > this.KEEP_OLD_LOGS) {
                    int toDelete = count - this.KEEP_OLD_LOGS;
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
        File parent = this.log.getParentFile();
        if (!parent.exists()) {
            parent.mkdirs();
        }
        try {
            this.writer = new PrintWriter(this.log){
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
            };
        } catch (IOException ex) {
            this.enabled = false;
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
            this.log.renameTo(new File(this.log.getAbsolutePath().replace("-unclosed", "")));
        }
    }
}
