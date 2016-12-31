package kml.gui;

import kml.Kernel;
import kml.Utils;
import kml.enums.OSArch;
import kml.enums.ProfileType;
import kml.enums.VersionType;
import kml.objects.Profile;
import kml.objects.VersionMeta;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.time.Instant;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * Created by darkl on 27/12/2016.
 */
public class ProfileEditor{
    private JPanel main;
    private BackgroundPanel contentPanel;
    private JTextField name;
    private JComboBox versions;
    private JSpinner resX;
    private JSpinner resY;
    private JTextField gameDir;
    private JTextField javaExec;
    private JTextField javaArgs;
    private JLabel resolutionLabel;
    private JLabel gameDirLabel;
    private JLabel javaExecLabel;
    private JLabel javaArgsLabel;
    private JButton saveButton;
    private JButton cancelButton;
    private JLabel nameLabel;
    private JLabel versionsLabel;
    private JButton deleteButton;
    private final Kernel kernel;
    private Profile profile = null;
    private final ImageIcon checkbox_enabled = new ImageIcon(SettingsTab.class.getResource("/kml/gui/textures/checkbox_enabled.png"));
    private final ImageIcon checkbox_disabled = new ImageIcon(SettingsTab.class.getResource("/kml/gui/textures/checkbox_disabled.png"));
    private final ImageIcon button_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_normal.png")).getImage().getScaledInstance(175, 40, Image.SCALE_SMOOTH));
    private final ImageIcon button_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_hover.png")).getImage().getScaledInstance(175, 40, Image.SCALE_SMOOTH));
    private boolean nameEnabled, versionEnabled, resolutionEnabled, gameDirEnabled, javaExecEnabled, javaArgsEnabled;

    public ProfileEditor(Kernel k){
        this.kernel = k;
        contentPanel.setImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/background.png")).getImage());
        resolutionLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gameDirLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        javaExecLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        javaArgsLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.setIcon(button_normal);
        cancelButton.setIcon(button_normal);
        deleteButton.setIcon(button_normal);
        resolutionLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (resolutionEnabled){
                    resX.setEnabled(false);
                    resY.setEnabled(false);
                    resolutionLabel.setIcon(checkbox_disabled);
                } else {
                    resX.setEnabled(true);
                    resY.setEnabled(true);
                    resolutionLabel.setIcon(checkbox_enabled);
                }
                resX.setValue(854);
                resY.setValue(480);
                resolutionEnabled = !resolutionEnabled;
            }
        });
        gameDirLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameDirEnabled){
                    gameDir.setEnabled(false);
                    gameDirLabel.setIcon(checkbox_disabled);
                } else {
                    gameDir.setEnabled(true);
                    gameDirLabel.setIcon(checkbox_enabled);
                }
                gameDir.setText(Utils.getWorkingDirectory().getAbsolutePath());
                gameDirEnabled = !gameDirEnabled;
            }
        });
        javaExecLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (javaExecEnabled){
                    javaExec.setEnabled(false);
                    javaExecLabel.setIcon(checkbox_disabled);
                } else {
                    if (kernel.getSettings().getEnableAdvanced()){
                        javaExec.setEnabled(true);
                        javaExecLabel.setIcon(checkbox_enabled);
                    }
                }
                javaExec.setText(Utils.getJavaDir());
                javaExecEnabled = !javaExecEnabled;
                if (!kernel.getSettings().getEnableAdvanced() && !javaExecEnabled){
                    updateConstraints();
                }
            }
        });
        javaArgsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (javaArgsEnabled){
                    javaArgs.setEnabled(false);
                    javaArgsLabel.setIcon(checkbox_disabled);
                } else {
                    if (kernel.getSettings().getEnableAdvanced()){
                        javaArgs.setEnabled(true);
                        javaArgsLabel.setIcon(checkbox_enabled);
                    }
                }
                StringBuilder builder = new StringBuilder();
                if (Utils.getOSArch().equals(OSArch.OLD)){
                    builder.append("-Xmx512M");
                } else {
                    builder.append("-Xmx1G");
                }
                builder.append(" -XX:+UseConcMarkSweepGC");
                builder.append(" -XX:+CMSIncrementalMode");
                builder.append(" -XX:-UseAdaptiveSizePolicy");
                builder.append(" -Xmn128M");
                javaArgs.setText(builder.toString());
                javaArgsEnabled = !javaArgsEnabled;
                if (!kernel.getSettings().getEnableAdvanced() && !javaArgsEnabled){
                    updateConstraints();
                }
            }
        });
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int response = JOptionPane.showConfirmDialog(null, "Are you sure? Any change won't be saved!", "Confirmation", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION){
                    kernel.getGUI().setSelected(kernel.getGUI().options);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                cancelButton.setIcon(button_hover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                cancelButton.setIcon(button_normal);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                cancelButton.setForeground(Color.YELLOW);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                cancelButton.setForeground(Color.WHITE);
            }
        });
        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                saveProfile();
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                saveButton.setIcon(button_hover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                saveButton.setIcon(button_normal);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                saveButton.setForeground(Color.YELLOW);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                saveButton.setForeground(Color.WHITE);
            }
        });
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete this profile?", "Profile deletion", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.YES_OPTION){
                    kernel.getProfiles().deleteProfile(profile.getID());
                    kernel.getGUI().setSelected(kernel.getGUI().options);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                deleteButton.setIcon(button_hover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                deleteButton.setIcon(button_normal);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                deleteButton.setForeground(Color.YELLOW);
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                deleteButton.setForeground(Color.RED);
            }
        });
    }
    public boolean setProfile(String p){
        if (p == null){
            this.profile = new Profile(ProfileType.CUSTOM, kernel);
            this.profile.setCreated(Instant.EPOCH);
            this.profile.setName("New Profile");
            return true;
        } else {
            if (kernel.getProfiles().existsProfile(p)){
                this.profile = kernel.getProfiles().getProfile(p);
                return true;
            }
        }
        return false;
    }
    public void refreshData(){
        name.setEnabled(true);
        versions.setEnabled(true);
        javaExec.setEnabled(true);
        javaArgs.setEnabled(true);
        javaExecLabel.setEnabled(true);
        javaArgsLabel.setEnabled(true);
        if (profile.hasName()){
            name.setText(profile.getName());
            nameEnabled = true;
            versionEnabled = true;
        } else {
            if (profile.getType() == ProfileType.RELEASE){
                name.setEnabled(false);
                versions.setEnabled(false);
                nameEnabled = false;
                versionEnabled = false;
                name.setText("Latest Release");
            } else if (profile.getType() == ProfileType.SNAPSHOT){
                name.setEnabled(false);
                versions.setEnabled(false);
                nameEnabled = false;
                versionEnabled = false;
                name.setText("Latest Snapshot");
            } else {
                name.setText("");
                nameEnabled = true;
                versionEnabled = true;
            }
        }
        resolutionEnabled = profile.hasResolution();
        gameDirEnabled = profile.hasGameDir();
        javaExecEnabled = profile.hasJavaDir();
        javaArgsEnabled = profile.hasJavaArgs();
        if (resolutionEnabled){
            resX.setEnabled(true);
            resY.setEnabled(true);
            resolutionLabel.setIcon(checkbox_enabled);
            resX.setValue(profile.getResolutionWidth());
            resY.setValue(profile.getResolutionHeight());
        } else {
            resX.setEnabled(false);
            resY.setEnabled(false);
            resolutionLabel.setIcon(checkbox_disabled);
            resX.setValue(854);
            resY.setValue(480);
        }
        if (gameDirEnabled){
            gameDir.setEnabled(true);
            gameDirLabel.setIcon(checkbox_enabled);
            gameDir.setText(profile.getGameDir().getAbsolutePath());
        } else {
            gameDir.setEnabled(false);
            gameDirLabel.setIcon(checkbox_disabled);
            gameDir.setText(Utils.getWorkingDirectory().getAbsolutePath());
        }
        if (javaExecEnabled){
            javaExec.setEnabled(true);
            javaExecLabel.setIcon(checkbox_enabled);
            javaExec.setText(profile.getJavaDir().getAbsolutePath());
        } else {
            javaExec.setEnabled(false);
            javaExecLabel.setIcon(checkbox_disabled);
            javaExec.setText(Utils.getJavaDir());
        }
        if (javaArgsEnabled){
            javaArgs.setEnabled(true);
            javaArgsLabel.setIcon(checkbox_enabled);
            javaArgs.setText(profile.getJavaArgs());
        } else {
            javaArgs.setEnabled(false);
            javaArgsLabel.setIcon(checkbox_disabled);
            StringBuilder builder = new StringBuilder();
            if (Utils.getOSArch().equals(OSArch.OLD)){
                builder.append("-Xmx512M");
            } else {
                builder.append("-Xmx1G");
            }
            builder.append(" -XX:+UseConcMarkSweepGC");
            builder.append(" -XX:+CMSIncrementalMode");
            builder.append(" -XX:-UseAdaptiveSizePolicy");
            builder.append(" -Xmn128M");
            javaArgs.setText(builder.toString());
        }
        updateConstraints();
    }
    public void saveProfile(){
        if (nameEnabled){
            if (!name.getText().isEmpty()){
                profile.setName(name.getText());
            } else {
                profile.setName(null);
            }
        } else {
            profile.setName(null);
        }
        if (versionEnabled){
            if (versions.getSelectedIndex() == 0){
                profile.setVersionID("latest-release");
            } else if (versions.getSelectedIndex() == 1){
                profile.setVersionID("latest-snapshot");
            } else {
                profile.setVersionID(versions.getSelectedItem().toString());
            }
        }
        if (resolutionEnabled){
            profile.setResolution((int)resX.getValue(), (int)resY.getValue());
        } else {
            profile.setResolution(-1, -1);
        }
        if (gameDirEnabled && !gameDir.getText().isEmpty()){
            File gd = new File(gameDir.getText());
            if (gd.exists() && gd.isDirectory()){
                profile.setGameDir(gd);
            } else {
                profile.setGameDir(null);
            }
        } else {
            profile.setGameDir(null);
        }
        if (javaExecEnabled && !javaExec.getText().isEmpty()){
            File jd = new File(javaExec.getText());
            if (jd.exists() && jd.isFile()){
                profile.setJavaDir(jd);
            } else {
                profile.setJavaDir(null);
            }
        } else {
            profile.setJavaDir(null);
        }
        if (javaArgsEnabled && !javaArgs.getText().isEmpty()){
            profile.setJavaArgs(javaArgs.getText());
        } else {
            profile.setJavaArgs(null);
        }
        if (!kernel.getProfiles().existsProfile(this.profile.getID())){
            kernel.getProfiles().addProfile(this.profile);
        }
        JOptionPane.showMessageDialog(null, "Profile saved successfully!", "Saved", JOptionPane.INFORMATION_MESSAGE);
        kernel.getGUI().setSelected(kernel.getGUI().options);
    }
    public void updateConstraints(){
        if (!kernel.getSettings().getEnableAdvanced()){
            if (!javaExecEnabled) {
                javaExecLabel.setEnabled(false);
            }
            if (!javaArgsEnabled){
                javaArgsLabel.setEnabled(false);
            }
        } else {
            javaExecLabel.setEnabled(true);
            javaArgsLabel.setEnabled(true);
        }
        if (nameEnabled){
            nameLabel.setEnabled(true);
        } else {
            nameLabel.setEnabled(false);
        }
        if (versionEnabled){
            versionsLabel.setEnabled(true);
        } else {
            versionsLabel.setEnabled(false);
        }
        this.versions.removeAllItems();
        this.versions.addItem("Latest Version");
        if (kernel.getSettings().getEnableSnapshots()){
            this.versions.addItem("Latest Snapshot");
        }
        if ((profile.hasVersion() && profile.getVersionID().equalsIgnoreCase("latest-snapshot") && kernel.getSettings().getEnableSnapshots()) || (!profile.hasVersion() && profile.getType() == ProfileType.SNAPSHOT)){
            this.versions.setSelectedIndex(1);
        }
        int count = 2;
        Map<String, VersionMeta> versions = kernel.getVersions().getVersions();
        Set keySet = versions.keySet();
        Iterator it = keySet.iterator();
        while (it.hasNext()){
            VersionMeta vm = versions.get(it.next().toString());
            if (vm.getType() == VersionType.RELEASE || (vm.getType() == VersionType.SNAPSHOT && kernel.getSettings().getEnableSnapshots()) || ((vm.getType() == VersionType.OLD_ALPHA || vm.getType() == VersionType.OLD_BETA) && kernel.getSettings().getEnableHistorical())){
                this.versions.addItem(vm.getID());
                if (profile.hasVersion() && vm.getID().equalsIgnoreCase(profile.getVersionID())){
                    this.versions.setSelectedIndex(count);
                }
                count++;
            }
        }
        if (this.profile.getType() == ProfileType.CUSTOM && kernel.getProfiles().existsProfile(this.profile.getID())){
            deleteButton.setVisible(true);
        } else {
            deleteButton.setVisible(false);
        }
    }
    public JPanel getPanel(){
        return this.contentPanel;
    }
}
