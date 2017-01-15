package kml.gui;

import kml.Authentication;
import kml.Kernel;
import kml.Language;
import kml.Utils;
import kml.exceptions.AuthenticationException;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;

/**
 * @author DarkLBP
 * website https://krothium.com
 */

public class LoginTab {

    private JPanel main;
    private JButton login;
    private JButton register;
    private JTextField username;
    private JPasswordField password;
    private JLabel userLabel;
    private JLabel passLabel;
    private final ImageIcon button_normal;
    private final ImageIcon button_hover;
    private final Kernel kernel;

    public LoginTab(Kernel k) {
        this.kernel = k;
        Border border = BorderFactory.createLineBorder(Color.BLACK, 2, true);
        this.username.setBorder(border);
        this.password.setBorder(border);
        button_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_normal.png")).getImage().getScaledInstance(220, 40, Image.SCALE_SMOOTH));
        button_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_hover.png")).getImage().getScaledInstance(220, 40, Image.SCALE_SMOOTH));
        login.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        login.setIcon(button_normal);
        register.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        register.setIcon(button_normal);
        login.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                login.setIcon(button_hover);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                login.setForeground(Color.YELLOW);
                authenticate();
                login.setForeground(Color.WHITE);
                login.setIcon(button_normal);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                login.setIcon(button_normal);
                login.setForeground(Color.WHITE);
            }
        });
        register.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                register.setForeground(Color.YELLOW);
                try {
                    Utils.openWebsite("https://krothium.com/register");
                } catch (IOException e1) {
                    //
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                register.setForeground(Color.WHITE);
                register.setIcon(button_normal);
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                register.setIcon(button_hover);
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
    public void refreshLocalizedStrings(){
        userLabel.setText(Language.get(18));
        passLabel.setText(Language.get(19));
        login.setText(Language.get(20));
        register.setText(Language.get(21));
    }
    private void authenticate(){
        Authentication a = kernel.getAuthentication();
        try {
            a.authenticate(username.getText(), new String(password.getPassword()));
            if (!a.isAuthenticated()){
                JOptionPane.showMessageDialog(null,Language.get(22), Language.get(23), JOptionPane.ERROR_MESSAGE);
                password.setText("");
            } else {
                username.setText("");
                password.setText("");
                username.requestFocus();
                kernel.getGUI().setDisable(false);
            }
        } catch (AuthenticationException e1) {
            JOptionPane.showMessageDialog(null,e1.getMessage(), Language.get(23), JOptionPane.ERROR_MESSAGE);
            password.setText("");
            password.requestFocus();
        }
    }
    public JPanel getPanel(){
        return this.main;
    }

}
