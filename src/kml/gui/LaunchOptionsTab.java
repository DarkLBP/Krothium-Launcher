package kml.gui;


import kml.*;
import kml.enums.ProfileIcon;
import kml.objects.Profile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class LaunchOptionsTab {
    private JPanel main;
    private JLabel snapshots;
    private JLabel historical;
    private JLabel advanced;
    private JList profiles;
    private final DefaultListModel listModel = new DefaultListModel();
    private final Settings settings;
    private final Kernel kernel;
    private final ImageIcon checkbox_enabled = new ImageIcon(SettingsTab.class.getResource("/kml/gui/textures/checkbox_enabled.png"));
    private final ImageIcon checkbox_disabled = new ImageIcon(SettingsTab.class.getResource("/kml/gui/textures/checkbox_disabled.png"));
    private final ImageIcon addProfile = new ImageIcon(SettingsTab.class.getResource("/kml/gui/textures/add.png"));
    private final Font plain = new Font("Minecraftia", Font.PLAIN,16);
    private final Font bold = new Font("Minecraftia", Font.BOLD,16);
    private final JLabel newProfile = new JLabel("New Profile");

    public LaunchOptionsTab(Kernel k) {
        kernel = k;
        settings = k.getSettings();
        snapshots.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        historical.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        advanced.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        profiles.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        snapshots.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                settings.setEnableSnapshots(!settings.getEnableSnapshots());
                if (settings.getEnableSnapshots()){
                    snapshots.setIcon(checkbox_enabled);
                    JOptionPane.showMessageDialog(null, Language.get(71) + "\n" + Language.get(72), Language.get(78), JOptionPane.WARNING_MESSAGE);
                } else {
                    snapshots.setIcon(checkbox_disabled);
                }
                populateList();
            }
        });
        historical.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                settings.setEnableHistorical(!settings.getEnableHistorical());
                if (settings.getEnableHistorical()){
                    historical.setIcon(checkbox_enabled);
                    JOptionPane.showMessageDialog(null, Language.get(73) + "\n" + Language.get(74) + "\n" + Language.get(75), Language.get(78), JOptionPane.WARNING_MESSAGE);
                } else {
                    historical.setIcon(checkbox_disabled);
                }
            }
        });
        advanced.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                settings.setEnableAdvanced(!settings.getEnableAdvanced());
                if (settings.getEnableAdvanced()){
                    advanced.setIcon(checkbox_enabled);
                    JOptionPane.showMessageDialog(null, Language.get(76) + "\n" + Language.get(77), Language.get(78), JOptionPane.WARNING_MESSAGE);
                } else {
                    advanced.setIcon(checkbox_disabled);
                }
            }
        });
        if (settings.getEnableSnapshots()){
            snapshots.setIcon(checkbox_enabled);
        } else {
            snapshots.setIcon(checkbox_disabled);
        }
        if (settings.getEnableHistorical()){
            historical.setIcon(checkbox_enabled);
        } else {
            historical.setIcon(checkbox_disabled);
        }
        if (settings.getEnableAdvanced()){
            advanced.setIcon(checkbox_enabled);
        } else {
            advanced.setIcon(checkbox_disabled);
        }
        profiles.setModel(this.listModel);
        profiles.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value == null){
                    newProfile.setIcon(addProfile);
                    newProfile.setFont(bold);
                    return newProfile;
                } else {
                    JLabel label = kernel.getProfiles().getProfile(value.toString()).getListItem();
                    label.setIcon(Utils.getProfileIcon(ProfileIcon.GRASS));
                    label.setFont(plain);
                    return label;
                }
            }
        });
        profiles.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (profiles.getSelectedValue() == null){
                    kernel.getGUI().editProfile(null);
                } else {
                    kernel.getGUI().editProfile(profiles.getSelectedValue().toString());
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                profiles.clearSelection();
            }
        });
    }
    public void refreshLocalizedStrings(){
        snapshots.setText(Language.get(48));
        historical.setText(Language.get(49));
        advanced.setText(Language.get(50));
        newProfile.setText(Language.get(51));
    }
    public JPanel getPanel(){
        return this.main;
    }
    public void populateList(){
        this.listModel.removeAllElements();
        Profiles p = kernel.getProfiles();
        Map<String, Profile> profs = p.getProfiles();
        Set set = profs.keySet();
        Iterator it = set.iterator();
        this.listModel.addElement(null);
        while (it.hasNext()){
            this.listModel.addElement(profs.get(it.next().toString()).getID());
        }
    }
}
