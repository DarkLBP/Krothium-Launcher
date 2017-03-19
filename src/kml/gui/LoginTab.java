package kml.gui;

import kml.Authentication;
import kml.Kernel;
import kml.Language;
import kml.Utils;
import kml.exceptions.AuthenticationException;
import kml.objects.User;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class LoginTab {

    private final ImageIcon button_normal;
    private final ImageIcon button_hover;
    private final ImageIcon login_normal;
    private final ImageIcon login_hover;
    private final ImageIcon logout_normal;
    private final ImageIcon logout_hover;
    private final Kernel kernel;
    private final Font plain = new Font("Minecraftia", Font.PLAIN, 16);
    private JPanel main;
    private JButton login, register;
    private JTextField username;
    private JPasswordField password;
    private JLabel userLabel, passLabel;
    private JComboBox<String> users;
    private DefaultComboBoxModel<String> users_model;
    private JPanel existingUsers;
    private JButton logExisting;
    private JButton logout;
    private JLabel existingLabel;

    public LoginTab(Kernel k) {
        this.kernel = k;
        Border border = BorderFactory.createLineBorder(Color.BLACK, 2, true);
        this.username.setBorder(border);
        this.password.setBorder(border);
        this.users.setBorder(border);
        button_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_normal.png")).getImage().getScaledInstance(220, 40, Image.SCALE_SMOOTH));
        button_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_hover.png")).getImage().getScaledInstance(220, 40, Image.SCALE_SMOOTH));
        login_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_normal.png")).getImage().getScaledInstance(140, 40, Image.SCALE_SMOOTH));
        login_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_hover.png")).getImage().getScaledInstance(140, 40, Image.SCALE_SMOOTH));
        logout_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_normal.png")).getImage().getScaledInstance(220, 40, Image.SCALE_SMOOTH));
        logout_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_hover.png")).getImage().getScaledInstance(220, 40, Image.SCALE_SMOOTH));
        login.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        login.setIcon(button_normal);
        register.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        register.setIcon(button_normal);
        logExisting.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logExisting.setIcon(login_normal);
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.setIcon(logout_normal);
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
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    authenticate();
                }
            }
        });
        logExisting.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logExisting.setIcon(login_hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logExisting.setIcon(login_normal);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                kernel.getAuthentication().setSelectedUser(users.getSelectedItem().toString());
                try {
                    kernel.getAuthentication().refresh();
                } catch (AuthenticationException e1) {
                    JOptionPane.showMessageDialog(null, e1.getMessage(), Language.get(23), JOptionPane.ERROR_MESSAGE);
                    updateExistingUsers();
                }
                if (kernel.getAuthentication().isAuthenticated()) {
                    kernel.getGUI().showLoginPrompt(false);
                }
            }
        });
        logout.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                logout.setIcon(logout_hover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                logout.setIcon(logout_normal);
            }

            @Override
            public void mousePressed(MouseEvent e) {
                int response = JOptionPane.showConfirmDialog(null, Language.get(8), Language.get(9), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION) {
                    kernel.getAuthentication().logOut(users.getSelectedItem().toString());
                    updateExistingUsers();
                }
            }
        });
        users_model = new DefaultComboBoxModel<>();
        users.setModel(users_model);
        users.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (Objects.nonNull(value)) {
                    User u = kernel.getAuthentication().getUser(value.toString());
                    if (Objects.nonNull(u)) {
                        JLabel label = new JLabel(u.getDisplayName());
                        label.setFont(plain);
                        return label;
                    }
                }
                return new JLabel("");
            }
        });
    }

    public void refreshLocalizedStrings() {
        userLabel.setText(Language.get(18));
        passLabel.setText(Language.get(19));
        login.setText(Language.get(20));
        register.setText(Language.get(21));
        logExisting.setText(Language.get(20));
        logout.setText(Language.get(3));
        existingLabel.setText(Language.get(85));
    }

    private void authenticate() {
        Authentication a = kernel.getAuthentication();
        try {
            a.authenticate(username.getText(), new String(password.getPassword()));
            if (!a.isAuthenticated()) {
                JOptionPane.showMessageDialog(null, Language.get(22), Language.get(23), JOptionPane.ERROR_MESSAGE);
                password.setText("");
            } else {
                username.setText("");
                password.setText("");
                username.requestFocus();
                kernel.getGUI().getBrowser().home();
                kernel.getGUI().refreshSkinPreviews();
                kernel.getGUI().populateProfileList();
                kernel.getGUI().updatePlayButton();
            }
        } catch (AuthenticationException e1) {
            JOptionPane.showMessageDialog(null, e1.getMessage(), Language.get(23), JOptionPane.ERROR_MESSAGE);
            password.setText("");
            password.requestFocus();
        }
    }

    public void updateExistingUsers() {
        Map<String, User> users = kernel.getAuthentication().getUsers();
        if (users.size() > 0) {
            Object lastSelected = null;
            if (this.users_model.getSize() > 0) {
                lastSelected = this.users.getSelectedItem();
                this.users_model.removeAllElements();
            }
            for (String s : users.keySet()) {
                this.users_model.addElement(s);
            }
            if (Objects.nonNull(lastSelected)) {
                int index = this.users_model.getIndexOf(lastSelected);
                if (index != -1) {
                    this.users.setSelectedIndex(index);
                }
            }
            if (!kernel.getAuthentication().hasSelectedUser()) {
                this.existingUsers.setVisible(true);
            }
        } else {
            if (this.existingUsers.isVisible()) {
                this.existingUsers.setVisible(false);
            }
        }

    }

    public JPanel getPanel() {
        return this.main;
    }

}
