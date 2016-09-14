package kml;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class ServerLauncher{
    public static void load(File f, String[] args){
        System.out.println("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        try{ 
            String r = Utils.sendPost(Constants.GETLATEST_URL, new byte[0], new HashMap());
            String[] data = r.split(":");
            int version = Integer.parseInt(Utils.fromBase64(data[0]));
            if (version > Constants.KERNEL_BUILD){
                System.out.println("New server launcher version build available!");
                System.out.println("Your build: " + Constants.KERNEL_BUILD);
                System.out.println("New build: " + version);
            }
            Constants.UPDATE_CHECKED = true;
        } catch (Exception ex){
            System.out.println("Failed to get latest version. (NETWORK_ERROR)");
        }
        if (!f.exists()){
            System.out.println("Server file does not exists!");
        } else {
            System.out.println("Launching server: " + f.getAbsolutePath());
            try{
                List<String> serverArgs = new ArrayList();
                serverArgs.add(Utils.getJavaDir());
                serverArgs.add("-cp");
                String libraries = "";
                String separator = System.getProperty("path.separator");
                try {
                    File launchPath = new File(GameLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                    libraries += launchPath.getAbsolutePath() + separator;
                } catch (URISyntaxException ex) {
                    System.out.println("Failed to load ServerStarter.");
                }
                libraries += f.getAbsolutePath();
                serverArgs.add(libraries);
                serverArgs.add("kml.ServerStarter");
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
            } catch (Exception ex){
                System.out.println("Failed to start the server.");
                ex.printStackTrace();
            }
        }
    }
}
