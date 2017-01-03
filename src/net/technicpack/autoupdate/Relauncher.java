//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package net.technicpack.autoupdate;

import kml.StubLauncher;
import net.technicpack.launcher.LauncherMain;
import net.technicpack.launchercore.install.InstallTasksQueue;
import net.technicpack.launchercore.install.LauncherDirectories;
import net.technicpack.utilslib.OperatingSystem;
import net.technicpack.utilslib.Utils;

import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;

public abstract class Relauncher {
    private String stream;
    private int currentBuild;
    private LauncherDirectories directories;
    private boolean didUpdate = false;

    public Relauncher(String stream, int currentBuild, LauncherDirectories directories) {
        this.stream = stream;
        this.currentBuild = currentBuild;
        this.directories = directories;
    }

    public int getCurrentBuild() {
        return this.currentBuild;
    }

    public String getStreamName() {
        return this.stream;
    }

    public void setUpdated() {
        this.didUpdate = true;
    }

    protected LauncherDirectories getDirectories() {
        return this.directories;
    }

    public String getRunningPath() throws UnsupportedEncodingException {
        return getRunningPath(this.getMainClass());
    }

    public static String getRunningPath(Class clazz) throws UnsupportedEncodingException {
        String path = clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
        path = path.replace("+", URLEncoder.encode("+", "UTF-8"));
        return URLDecoder.decode(path, "UTF-8");
    }

    protected abstract Class getMainClass();

    public abstract String getUpdateText();

    public abstract boolean isUpdateOnly();

    public abstract boolean isMover();

    public abstract boolean isLauncherOnly();

    public abstract InstallTasksQueue buildMoverTasks();

    public abstract InstallTasksQueue buildUpdaterTasks();

    public abstract String[] getLaunchArgs();

    public abstract void updateComplete();

    public abstract boolean canReboot();

    public boolean runAutoUpdater() throws IOException, InterruptedException {
        if(this.isLauncherOnly()) {
            return true;
        } else {
            boolean needsReboot = false;
            if(this.canReboot()) {
                if(System.getProperty("awt.useSystemAAFontSettings") != null && System.getProperty("awt.useSystemAAFontSettings").equals("lcd")) {
                    if(!Boolean.parseBoolean(System.getProperty("java.net.preferIPv4Stack"))) {
                        needsReboot = true;
                    }
                } else {
                    needsReboot = true;
                }
            }

            InstallTasksQueue updateTasksQueue = null;
            if(this.isMover()) {
                updateTasksQueue = this.buildMoverTasks();
            } else {
                if(needsReboot && this.getCurrentBuild() > 0) {
                    this.relaunch();
                    return false;
                }

                if(this.getCurrentBuild() < 1) {
                    return true;
                }

                updateTasksQueue = this.buildUpdaterTasks();
            }

            if(updateTasksQueue == null) {
                return true;
            } else {
                updateTasksQueue.runAllTasks();
                this.updateComplete();
                return !this.didUpdate && !this.isUpdateOnly();
            }
        }
    }

    public void relaunch() {
        this.launch(null, this.getLaunchArgs());
    }

    public File getTempLauncher() {
        String runningPath = null;

        try {
            runningPath = this.getRunningPath();
        } catch (UnsupportedEncodingException var4) {
            var4.printStackTrace();
            return null;
        }

        File dest;
        if(runningPath.endsWith(".exe")) {
            dest = new File(this.directories.getLauncherDirectory(), "temp.exe");
        } else {
            dest = new File(this.directories.getLauncherDirectory(), "temp.jar");
        }

        return dest;
    }

    public void launch(String launchPath, String[] args) {
        if(launchPath == null) {
            try {
                launchPath = this.getRunningPath();
            } catch (UnsupportedEncodingException var10) {
                return;
            }
        }
        ProcessBuilder processBuilder = new ProcessBuilder();
        ArrayList<String> commands = new ArrayList<>();
        if(!launchPath.endsWith(".exe")) {
            commands.add(OperatingSystem.getJavaDir());
            commands.add("-Xmx256m");
            commands.add("-verbose");
            commands.add("-Djava.net.preferIPv4Stack=true");
            commands.add("-Dawt.useSystemAAFontSettings=lcd");
            commands.add("-Dswing.aatext=true");
            commands.add("-cp");
            try {
                File stubPath = new File(StubLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                File technicPath = new File(LauncherMain.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                String separator = System.getProperty("path.separator");
                commands.add("\"" + stubPath.getAbsolutePath() + separator + technicPath.getAbsolutePath() + "\"");
                Utils.getLogger().info("Stub injected.");
            } catch (Exception ex){
                Utils.getLogger().info("Failed to inject stub.");
            }
            commands.add("kml.StubStarter");
            commands.add(this.getMainClass().getName());
        } else {
            commands.add(launchPath);
        }

        commands.addAll(Arrays.asList(args));
        String command = "";

        String token;
        for(Iterator e = commands.iterator(); e.hasNext(); command = command + token + " ") {
            token = (String)e.next();
        }

        Utils.getLogger().info("Launching command: \'" + command + "\'");
        processBuilder.command(commands);

        try {
            processBuilder.start();
        } catch (IOException var8) {
            JOptionPane.showMessageDialog(null, "Your OS has prevented this relaunch from completing.  You may need to add an exception in your security software.", "Relaunch Failed", 0);
            var8.printStackTrace();
        } catch (Exception var9) {
            var9.printStackTrace();
        }

        System.exit(0);
    }

    public String[] buildMoverArgs() throws UnsupportedEncodingException {
        ArrayList<String> outArgs = new ArrayList<>();
        outArgs.add("-movetarget");
        outArgs.add(this.getRunningPath());
        outArgs.add("-moveronly");
        outArgs.addAll(Arrays.asList(this.getLaunchArgs()));
        return (String[])outArgs.toArray(new String[outArgs.size()]);
    }

    public String[] buildLauncherArgs(boolean isLegacy) {
        ArrayList<String> outArgs = new ArrayList<>();
        if(!isLegacy) {
            outArgs.add("-launcheronly");
        } else {
            outArgs.add("-launcher");
        }

        outArgs.addAll(Arrays.asList(this.getLaunchArgs()));
        outArgs.remove("-moveronly");
        return (String[])outArgs.toArray(new String[outArgs.size()]);
    }
}