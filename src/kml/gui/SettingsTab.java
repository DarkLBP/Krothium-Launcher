package kml.gui;

import kml.Kernel;
import kml.Settings;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class SettingsTab {
    private JPanel main;
    private JLabel keepOpen;
    private JLabel logOpen;
    private final ImageIcon checkbox_enabled = new ImageIcon(SettingsTab.class.getResource("/kml/gui/textures/checkbox_enabled.png"));
    private final ImageIcon checkbox_disabled = new ImageIcon(SettingsTab.class.getResource("/kml/gui/textures/checkbox_disabled.png"));
    private final Settings settings;
    public SettingsTab(Kernel k) {
        settings = k.getSettings();
        keepOpen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logOpen.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        keepOpen.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                settings.setKeepLauncherOpen(!settings.getKeepLauncherOpen());
                if (settings.getKeepLauncherOpen()){
                    keepOpen.setIcon(checkbox_enabled);
                } else {
                    keepOpen.setIcon(checkbox_disabled);
                }
            }
        });
        logOpen.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                settings.setShowGameLog(!settings.getShowGameLog());
                if (settings.getShowGameLog()){
                    logOpen.setIcon(checkbox_enabled);
                } else {
                    logOpen.setIcon(checkbox_disabled);
                }
            }
        });
        if (settings.getKeepLauncherOpen()){
            keepOpen.setIcon(checkbox_enabled);
        } else {
            keepOpen.setIcon(checkbox_disabled);
        }
        if (settings.getShowGameLog()){
            logOpen.setIcon(checkbox_enabled);
        } else {
            logOpen.setIcon(checkbox_disabled);
        }
    }
    public JPanel getPanel(){return this.main;}
}

