package kml.gui;

import javax.swing.*;

/**
 * Created by darkl on 18/11/2016.
 */
public class Main extends JFrame{
    private JPanel panel1;
    private JButton button1;
    private BackgroundPanel backgroundPanel1;
    private BackgroundPanel backgroundPanel2;
    private BackgroundPanel test;

    public Main(){
        test.setImage(new ImageIcon(Login.class.getResource("/kml/gui/textures/background.png")).getImage());
        backgroundPanel1.setImage(new ImageIcon(Login.class.getResource("/kml/gui/textures/login-background.png")).getImage());
        backgroundPanel2.setImage(new ImageIcon(Login.class.getResource("/kml/gui/textures/login-background.png")).getImage());
        setContentPane(panel1);
        setVisible(true);
    }
}
