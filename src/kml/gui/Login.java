package kml.gui;

import kml.Constants;
import kml.Utils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class Login extends JFrame{

    private JPanel mainPanel;
    private JButton login;
    private JButton register;
    private BackgroundPanel background;
    private JPanel usernamePanel;
    private JTextField username;
    private JPanel passwordPanel;
    private JPasswordField password;
    private JPanel content;
    private ImageIcon button_normal = new ImageIcon(new ImageIcon(Login.class.getResource("/kml/gui/textures/button_normal.png")).getImage().getScaledInstance(240, 40, Image.SCALE_SMOOTH));
    private ImageIcon button_hover = new ImageIcon(new ImageIcon(Login.class.getResource("/kml/gui/textures/button_hover.png")).getImage().getScaledInstance(240, 40, Image.SCALE_SMOOTH));
    private Image background_image = new ImageIcon(Login.class.getResource("/kml/gui/textures/login-background.png")).getImage();

    public Login() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.background.setImage(background_image);
        setContentPane(this.background);
        setMinimumSize(new Dimension(550, 420));
        setLocationRelativeTo(null);
        setResizable(false);
        setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        setIconImage(new ImageIcon(Login.class.getResource("/kml/gui/textures/icon.png")).getImage());
        login.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        login.setIcon(button_normal);
        register.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        register.setIcon(button_normal);
        login.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                login.setIcon(button_hover);
            }
        });
        login.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                login.setIcon(button_normal);
                login.setForeground(Color.WHITE);
            }
        });
        login.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                login.setIcon(button_hover);
            }
        });
        login.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                login.setIcon(button_normal);
            }
        });
        login.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                login.setForeground(Color.WHITE);
            }
        });
        login.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                login.setForeground(Color.YELLOW);
            }
        });
        register.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                register.setIcon(button_hover);
            }
        });
        register.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                register.setForeground(Color.WHITE);
                register.setIcon(button_normal);
            }
        });
        register.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                register.setIcon(button_hover);
            }
        });
        register.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                register.setIcon(button_normal);
            }
        });
        register.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
            register.setForeground(Color.WHITE);
            try {
                Utils.openWebsite("https://krothium.com/register");
            } catch (IOException e1) {
                //
            }
            }
        });
        register.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                register.setForeground(Color.YELLOW);
            }
        });
    }
}
