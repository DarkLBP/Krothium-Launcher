package kml.gui;

import kml.Authentication;
import kml.Kernel;
import kml.Utils;
import kml.exceptions.AuthenticationException;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class LoginTab {

    private JPanel mainPanel;
    private JButton login;
    private JButton register;
    private JTextField username;
    private JPasswordField password;
    private JPanel content;
    private final ImageIcon button_normal;
    private final ImageIcon button_hover;
    private final Kernel kernel;
    private final Border border;

    public LoginTab(Kernel k) {
        this.kernel = k;
        this.border = BorderFactory.createLineBorder(Color.BLACK, 2, true);
        this.username.setBorder(border);
        this.password.setBorder(border);
        button_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_normal.png")).getImage().getScaledInstance(240, 50, Image.SCALE_SMOOTH));
        button_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_hover.png")).getImage().getScaledInstance(240, 50, Image.SCALE_SMOOTH));
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
                authenticate();
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
        password.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER){
                    authenticate();
                }
            }
        });

    }
    public void authenticate(){
        Authentication a = kernel.getAuthentication();
        try {
            a.authenticate(username.getText(), new String(password.getPassword()));
            if (!a.isAuthenticated()){
                JOptionPane.showMessageDialog(null,"Unnable to login!","Error", JOptionPane.ERROR_MESSAGE);
                password.setText("");
            } else {
                kernel.getGUI().setDisable(false);
            }
        } catch (AuthenticationException e1) {
            JOptionPane.showMessageDialog(null,e1.getMessage(),"Error", JOptionPane.ERROR_MESSAGE);
            password.setText("");
            password.requestFocus();
        }
    }
    public JPanel getPanel(){
        return this.mainPanel;
    }

}
