package kml.gui;

import kml.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Created by darkl on 18/11/2016.
 */
public class Main extends JFrame{
    private JPanel panel1;
    private JButton button1;
    private BackgroundPanel backgroundPanel1;
    private BackgroundPanel backgroundPanel2;
    private BackgroundPanel test;
    private JLabel news;
    private JLabel skins;
    private JLabel settings;
    private JLabel options;
    private JLabel selected;
    private ImageIcon button_normal = new ImageIcon(new ImageIcon(Login.class.getResource("/kml/gui/textures/button_normal.png")).getImage().getScaledInstance(240, 40, Image.SCALE_SMOOTH));
    private ImageIcon button_hover = new ImageIcon(new ImageIcon(Login.class.getResource("/kml/gui/textures/button_hover.png")).getImage().getScaledInstance(240, 40, Image.SCALE_SMOOTH));
    public Main(){
        setSize(600, 600);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        setIconImage(new ImageIcon(Login.class.getResource("/kml/gui/textures/icon.png")).getImage());
        test.setImage(new ImageIcon(Login.class.getResource("/kml/gui/textures/background.png")).getImage());
        backgroundPanel1.setImage(new ImageIcon(Login.class.getResource("/kml/gui/textures/login-background.png")).getImage());
        backgroundPanel2.setImage(new ImageIcon(Login.class.getResource("/kml/gui/textures/login-background.png")).getImage());
        setContentPane(panel1);
        setVisible(true);
        news.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (news != selected){
                    news.setForeground(Color.GREEN);
                }
                news.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        });
        news.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                news.setForeground(Color.WHITE);
            }
        });
        news.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelected(news);
                news.setForeground(Color.WHITE);
            }
        });
        skins.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (skins != selected){
                    skins.setForeground(Color.GREEN);
                }
                skins.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        });
        skins.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                skins.setForeground(Color.WHITE);
            }
        });
        skins.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelected(skins);
                skins.setForeground(Color.WHITE);
            }
        });
        settings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (settings != selected){
                    settings.setForeground(Color.GREEN);
                }
                settings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        });
        settings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                settings.setForeground(Color.WHITE);
            }
        });
        settings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelected(settings);
                settings.setForeground(Color.WHITE);
            }
        });
        options.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (options != selected){
                    options.setForeground(Color.GREEN);
                }
                options.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
        });
        options.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                options.setForeground(Color.WHITE);
            }
        });
        options.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                setSelected(options);
                options.setForeground(Color.WHITE);
            }
        });
    }
    @Override
    public void setVisible(boolean b){
        super.setVisible(b);
        setSelected(news);
    }
    private void setSelected(JLabel l){
        if (selected == null ){
            selected = l;
            selected.setIcon(new ImageIcon(new ImageIcon(Login.class.getResource("/kml/gui/textures/menu_label.png")).getImage().getScaledInstance(selected.getWidth() + 15, selected.getHeight() + 15, Image.SCALE_SMOOTH)));
        } else if (l != selected) {
            selected.setIcon(null);
            selected = l;
            selected.setIcon(new ImageIcon(new ImageIcon(Login.class.getResource("/kml/gui/textures/menu_label.png")).getImage().getScaledInstance(selected.getWidth() + 15, selected.getHeight() + 15, Image.SCALE_SMOOTH)));
        }
    }
}
