package kml.gui;

import kml.Kernel;
import kml.Utils;
import kml.enums.ProfileIcon;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class ProfilePopup extends JFrame{
    private JPanel main;
    private JList profiles;
    private final DefaultListModel model = new DefaultListModel();
    private final Font bold = new Font("Minecraftia", Font.BOLD,16);
    private final Font plain = new Font("Minecraftia", Font.PLAIN,16);

    public ProfilePopup(final Kernel kernel){
        setUndecorated(true);
        setSize(new Dimension(300, 150));
        setContentPane(main);
        profiles.setModel(model);
        profiles.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                JLabel label = kernel.getProfiles().getProfile((String)value).getListItem();
                if (kernel.getProfiles().getSelectedProfile() != null && kernel.getProfiles().getSelectedProfile().equals(value)){
                    label.setFont(bold);
                } else {
                    label.setFont(plain);
                }
                label.setIcon(Utils.getProfileIcon(ProfileIcon.GRASS));
                return label;
            }
        });
        profiles.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                kernel.getProfiles().setSelectedProfile((String)profiles.getSelectedValue());
                setVisible(false);
            }
        });
        this.addWindowFocusListener(new WindowAdapter() {
            @Override
            public void windowLostFocus(WindowEvent e) {
                setVisible(false);
            }
        });
    }
    public void addElement(String element){
        model.addElement(element);
    }
    public void removeAll(){
        model.removeAllElements();
    }
    public void showPopup(JComponent component){
        setLocationRelativeTo(component);
        setVisible(true);
    }
}
