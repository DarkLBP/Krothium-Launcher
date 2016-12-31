package kml.gui;

import kml.Constants;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;

/**
 * Created by darkl on 31/12/2016.
 */
public class GameLog extends JFrame{
    private JPanel main;
    private BackgroundPanel contentPanel;
    private JTextArea logArea;
    private InputStream info;
    private InputStream error;
    private final Thread infoThread;
    private final Thread errorThread;
    public GameLog(){
        setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME + " | Game Output");
        setSize(new Dimension(600, 800));
        setIconImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/icon.png")).getImage());
        setLocationRelativeTo(null);
        infoThread = new Thread() {
            @Override
            public void run(){
                if (info != null){
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(info, Charset.forName("UTF-8")));
                        String line;
                        while ((line = in.readLine()) != null && !super.isInterrupted()){
                            logArea.append(line);
                            logArea.append("\n");
                        }
                    } catch (Exception ex){};
                }
            }
        };
        errorThread = new Thread() {
            @Override
            public void run(){
                if (info != null){
                    try {
                        BufferedReader in = new BufferedReader(new InputStreamReader(error, Charset.forName("UTF-8")));
                        String line;
                        while ((line = in.readLine()) != null && !super.isInterrupted()){
                            logArea.append(line);
                            logArea.append("\n");
                        }
                    } catch (Exception ex){};
                }
            }
        };
    }
    @Override
    public void setVisible(boolean s){
        if (s){
            if (!infoThread.isAlive()){
                infoThread.run();
            }
            if (!errorThread.isAlive()){
                errorThread.run();
            }
        } else {
            if (infoThread.isAlive()){
                infoThread.interrupt();
                errorThread.interrupt();
            }
        }
        super.setVisible(s);
    }
    public void setInfoStream(InputStream s){this.info = s;}
    public void setErrorStream(InputStream s){this.error = s;}
}
