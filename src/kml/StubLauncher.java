package kml;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.*;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class StubLauncher {
    public static void load(File f, String[] args){
        System.out.println("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        try{ 
            String r = Utils.sendPost(Constants.GETLATEST_URL, new byte[0], new HashMap());
            String[] data = r.split(":");
            int version = Integer.parseInt(Utils.fromBase64(data[0]));
            if (version > Constants.KERNEL_BUILD){
                System.out.println("New launcher build available!");
                System.out.println("Your build: " + Constants.KERNEL_BUILD);
                System.out.println("New build: " + version);
            }
            Constants.UPDATE_CHECKED = true;
        } catch (Exception ex){
            System.out.println("Failed to get latest version. " + ex.getMessage());
        }
        if (!f.exists()){
            System.out.println("Specified file " + f.getAbsolutePath() + " does not exist!");
        } else {
            System.out.println("Launching: " + f.getAbsolutePath());
            try{
                List<String> serverArgs = new ArrayList();
                serverArgs.add(Utils.getJavaDir());
                serverArgs.add("-cp");
                StringBuilder libraries = new StringBuilder();
                libraries.append("\"");
                String separator = System.getProperty("path.separator");
                try {
                    File launchPath = new File(GameLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                    libraries.append(launchPath.getAbsolutePath() + separator);
                } catch (URISyntaxException ex) {
                    System.out.println("Failed to load StubStarter.");
                }
                libraries.append(f.getAbsolutePath());
                libraries.append("\"");
                serverArgs.add(libraries.toString());
                serverArgs.add("kml.StubStarter");
                JarFile jar = new JarFile(f);
                Attributes atrb = jar.getManifest().getMainAttributes();
                if (atrb.containsKey(Attributes.Name.MAIN_CLASS)){
                    serverArgs.add(atrb.getValue(Attributes.Name.MAIN_CLASS));
                    Collections.addAll(serverArgs, args);
                    ProcessBuilder pb = new ProcessBuilder(serverArgs);
                    pb.directory(f.getParentFile());
                    try{
                        final Process process = pb.start();
                        Thread log_info = new Thread(){
                            @Override
                            public void run(){
                                InputStreamReader isr = new InputStreamReader(process.getInputStream());
                                BufferedReader br = new BufferedReader(isr);
                                String lineRead;
                                try{
                                    while (this.isStarted()){
                                        if ((lineRead = br.readLine()) != null){
                                            System.out.println(lineRead);
                                        }
                                    }
                                } catch (Exception ex){
                                    System.out.println("Server stopped unexpectedly.");
                                }
                            }
                            public boolean isStarted(){
                                try {
                                    if (process != null){
                                        process.exitValue();
                                    }
                                    return false;
                                } catch (Exception ex){
                                    return true;
                                }
                            }
                        };
                        log_info.start();
                        Thread log_error = new Thread(){
                            @Override
                            public void run(){
                                InputStreamReader isr = new InputStreamReader(process.getErrorStream());
                                BufferedReader br = new BufferedReader(isr);
                                String lineRead;
                                try{
                                    while (isStarted()){
                                        if ((lineRead = br.readLine()) != null){
                                            System.out.println(lineRead);
                                        }
                                    }
                                } catch (Exception ex){}
                            }
                            public boolean isStarted(){
                                try {
                                    if (process != null){
                                        process.exitValue();
                                    }
                                    return false;
                                } catch (Exception ex){
                                    return true;
                                }
                            }
                        };
                        log_error.start();
                    }catch (Exception ex){
                        System.out.println("Server returned an error code.");
                    }
                } else {
                    System.out.println(f.getAbsolutePath() + " does not have a Main Class!");
                }
            } catch (Exception ex){
                System.out.println("Failed to start the server.");
                ex.printStackTrace();
            }
        }
    }
}
