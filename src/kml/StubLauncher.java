package kml;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.jar.*;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */
public class StubLauncher {
    public static void load(File f, String[] args){
        System.out.println("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        File usingFile = f;
        try{ 
            String r = Utils.sendPost(Constants.GETLATEST_URL, new byte[0], new HashMap<>());
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
        if (!usingFile.exists()){
            System.out.println("Specified file " + usingFile.getAbsolutePath() + " does not exist!");
        } else {
            System.out.println("Launching: " + usingFile.getAbsolutePath());
            try{
                JarFile jar = new JarFile(usingFile);
                Enumeration<JarEntry> entries = jar.entries();
                boolean RSAProtection = false;
                while (entries.hasMoreElements()){
                    JarEntry entry = entries.nextElement();
                    if (entry.getName().contains("META-INF") && entry.getName().contains(".RSA")){
                        RSAProtection = true;
                    }
                }
                Attributes atrb = jar.getManifest().getMainAttributes();
                if (atrb.containsKey(Attributes.Name.MAIN_CLASS)){
                    if (RSAProtection){
                        System.out.println("JAR IS PROTECTED!");
                        File outJar = new File(Utils.getWorkingDirectory() + File.separator + "stub_unprotected.jar");
                        JarInputStream in = new JarInputStream(new FileInputStream(f));
                        JarOutputStream out = new JarOutputStream(new FileOutputStream(outJar));
                        JarEntry entry;
                        while ((entry = in.getNextJarEntry()) != null){
                            if (entry.getName().contains("META-INF") && entry.getName().contains(".RSA")){
                                continue;
                            }
                            out.putNextEntry(entry);
                            byte[] buffer = new byte[4096];
                            int read;
                            while ((read = in.read(buffer)) != -1){
                                out.write(buffer, 0, read);
                            }
                            in.closeEntry();
                            out.closeEntry();
                        }
                        in.close();
                        out.close();
                        usingFile = outJar;
                    }
                    List<String> serverArgs = new ArrayList<>();
                    serverArgs.add(Utils.getJavaDir());
                    serverArgs.add("-cp");
                    StringBuilder libraries = new StringBuilder();
                    String separator = System.getProperty("path.separator");
                    try {
                        File launchPath = new File(StubLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                        libraries.append(launchPath.getAbsolutePath()).append(separator);
                    } catch (URISyntaxException ex) {
                        System.out.println("Failed to load StubStarter.");
                    }
                    libraries.append(usingFile.getAbsolutePath());
                    serverArgs.add(libraries.toString());
                    serverArgs.add("kml.StubStarter");
                    serverArgs.add(atrb.getValue(Attributes.Name.MAIN_CLASS));
                    Collections.addAll(serverArgs, args);
                    ProcessBuilder pb = new ProcessBuilder(serverArgs);
                    pb.directory(usingFile.getParentFile());
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
                                    System.out.println("Stub stopped unexpectedly.");
                                }
                            }
                            public boolean isStarted(){
                                return process.isAlive();
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
                                } catch (Exception ex){
                                    System.out.println("Failed to read game error stream.");
                                }
                            }
                            public boolean isStarted(){
                                return process.isAlive();
                            }
                        };
                        log_error.start();
                    }catch (Exception ex){
                        System.out.println("Stub returned an error code.");
                    }
                } else {
                    System.out.println(f.getAbsolutePath() + " does not have a Main Class!");
                }
            } catch (Exception ex){
                System.out.println("Failed to start the stub.");
                ex.printStackTrace();
            }
        }
    }
}
