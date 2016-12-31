package kml.gui;


import kml.Kernel;
import kml.Profiles;
import kml.Settings;
import kml.Utils;
import kml.enums.ProfileIcon;
import kml.enums.ProfileType;
import kml.objects.Profile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by darkl on 26/12/2016.
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
                } else {
                    snapshots.setIcon(checkbox_disabled);
                }
            }
        });
        historical.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                settings.setEnableHistorical(!settings.getEnableHistorical());
                if (settings.getEnableHistorical()){
                    historical.setIcon(checkbox_enabled);
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
    public JPanel getPanel(){
        return this.main;
    }
    public void populateList(){
        this.listModel.clear();
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
