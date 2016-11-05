package kml.gui;

import kml.Constants;

import javax.swing.*;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * @website https://krothium.com
 * @author DarkLBP
 */

public class Login extends JFrame{

    private JPanel background = new JPanel(){
        private Image image = new ImageIcon(Login.class.getResource("/kml/gui/textures/background.png")).getImage();
        @Override
        public void paintComponent(Graphics g){
            int height = image.getHeight(null);
            int width = image.getWidth(null);
            for (int x = 0; x < getWidth(); x += width) {
                for (int y = 0; y < getHeight(); y += height) {
                    g.drawImage( image, x, y, null, null );
                }
            }
        }
    };
    private JPanel mainPanel;
    private JTextField username;
    private JPasswordField password;
    private JButton login;
    private JButton register;
    private ImageIcon button_normal = new ImageIcon(new ImageIcon(Login.class.getResource("/kml/gui/textures/button_normal.png")).getImage().getScaledInstance(240, 40, Image.SCALE_SMOOTH));
    private ImageIcon button_hover = new ImageIcon(new ImageIcon(Login.class.getResource("/kml/gui/textures/button_hover.png")).getImage().getScaledInstance(240, 40, Image.SCALE_SMOOTH));

    public Login() {
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.background.setLayout(new GridLayout());
        this.background.add(this.mainPanel);
        setContentPane(this.background);
        setLocationRelativeTo(null);
        setMinimumSize(new Dimension(550, 400));
        setResizable(false);
        setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        setIconImage(new ImageIcon(Login.class.getResource("/kml/gui/textures/icon.png")).getImage());
        login.setIcon(button_normal);
        register.setIcon(button_normal);
        login.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                login.setIcon(button_hover);
                login.setForeground(Color.YELLOW);
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
                login.setForeground(Color.YELLOW);
            }
        });
        login.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                login.setIcon(button_normal);
                login.setForeground(Color.WHITE);
            }
        });
        register.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                register.setIcon(button_hover);
                register.setForeground(Color.YELLOW);
            }
        });
        register.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                register.setIcon(button_normal);
                register.setForeground(Color.WHITE);
            }
        });
        register.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                register.setIcon(button_hover);
                register.setForeground(Color.YELLOW);
            }
        });
        register.addFocusListener(new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
                register.setIcon(button_normal);
                register.setForeground(Color.WHITE);
            }
        });
    }
    public static void main(String[] args) throws ClassNotFoundException, UnsupportedLookAndFeelException, InstantiationException, IllegalAccessException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        Login l = new Login();
        l.setVisible(true);
    }

}
