package kml.gui;

import javax.swing.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by darkl on 18/12/2016.
 */
public class Settings {
    private JPanel main;
    private JLabel keepOpen;
    private JLabel logOpen;
    private final ImageIcon checkbox_enabled = new ImageIcon(Settings.class.getResource("/kml/gui/textures/checbox_enabled.png"));
    private final ImageIcon checkbox_disabled = new ImageIcon(Settings.class.getResource("/kml/gui/textures/checkbox_disabled.png"));

    public Settings() {
        keepOpen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }
        });
        logOpen.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {

            }
        });
    }

    public JPanel getPanel(){return this.main;}
}

