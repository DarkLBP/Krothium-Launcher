package kml.gui;

import kml.Constants;
import kml.Kernel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class GameLog extends JFrame{
    private JPanel main;
    private JList logList;
    private JScrollPane scrollPane;
    private final DefaultListModel model = new DefaultListModel();
    public GameLog(Kernel k){
        setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME + " | Game Output");
        setSize(new Dimension(750, 600));
        setIconImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/icon.png")).getImage());
        setLocationRelativeTo(null);
        setContentPane(main);
        logList.setModel(model);
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                if (!k.getSettings().getKeepLauncherOpen()){
                    k.exitSafely();
                }
            }
        });
    }
    @Override
    public void setVisible(boolean s){
        super.setVisible(s);
        model.clear();
    }
    public void pushString(String line){
        model.addElement(line);
        logList.setSelectedIndex(model.getSize() - 1);
        JScrollBar vertical = scrollPane.getVerticalScrollBar();
        vertical.setValue( vertical.getMaximum() );
    }
}
