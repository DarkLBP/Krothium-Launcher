package kml.gui;

import kml.Kernel;
import kml.Language;
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
import java.sql.Timestamp;
import java.util.Map;
import java.util.Set;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class ProfileEditor{
    private JPanel main;
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
    private final ImageIcon button_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_normal.png")).getImage().getScaledInstance(160, 40, Image.SCALE_SMOOTH));
    private final ImageIcon button_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_hover.png")).getImage().getScaledInstance(160, 40, Image.SCALE_SMOOTH));
    private boolean nameEnabled, versionEnabled, resolutionEnabled, gameDirEnabled, javaExecEnabled, javaArgsEnabled;

    public ProfileEditor(Kernel k){
        this.kernel = k;
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
                    if (!kernel.getSettings().getEnableAdvanced()){
                        javaExecLabel.setVisible(false);
                        javaExec.setVisible(false);
                    }
                } else {
                    javaExec.setEnabled(true);
                    javaExecLabel.setIcon(checkbox_enabled);
                }
                javaExec.setText(Utils.getJavaDir());
                javaExecEnabled = !javaExecEnabled;
            }
        });
        javaArgsLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (javaArgsEnabled){
                    javaArgs.setEnabled(false);
                    javaArgsLabel.setIcon(checkbox_disabled);
                    if (!kernel.getSettings().getEnableAdvanced()){
                        javaArgsLabel.setVisible(false);
                        javaArgs.setVisible(false);
                    }
                } else {
                    javaArgs.setEnabled(true);
                    javaArgsLabel.setIcon(checkbox_enabled);
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
            }
        });
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cancelButton.setIcon(button_hover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                cancelButton.setIcon(button_normal);
                cancelButton.setForeground(Color.WHITE);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                cancelButton.setForeground(Color.YELLOW);
                int response = JOptionPane.showConfirmDialog(null, Language.get(55), Language.get(56), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION){
                    kernel.getGUI().setSelected(kernel.getGUI().options);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                cancelButton.setForeground(Color.WHITE);
            }
        });
        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                saveButton.setIcon(button_hover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                saveButton.setForeground(Color.WHITE);
                saveButton.setIcon(button_normal);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                saveButton.setForeground(Color.YELLOW);
                saveProfile();
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                saveButton.setForeground(Color.WHITE);
            }
        });
        deleteButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                deleteButton.setIcon(button_hover);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                deleteButton.setIcon(button_normal);
                deleteButton.setForeground(Color.RED);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                deleteButton.setForeground(Color.YELLOW);
                int response = JOptionPane.showConfirmDialog(null, Language.get(61), Language.get(62), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.YES_OPTION){
                    kernel.getProfiles().deleteProfile(profile.getID());
                    kernel.getGUI().setSelected(kernel.getGUI().options);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                deleteButton.setForeground(Color.RED);
            }
        });
    }
    public void refreshLocalizedStrings(){
        saveButton.setText(Language.get(52));
        cancelButton.setText(Language.get(53));
        deleteButton.setText(Language.get(54));
        nameLabel.setText(Language.get(63));
        versionsLabel.setText(Language.get(64));
        resolutionLabel.setText(Language.get(65));
        gameDirLabel.setText(Language.get(66));
        javaExecLabel.setText(Language.get(67));
        javaArgsLabel.setText(Language.get(68));
    }
    public boolean setProfile(String p){
        if (p == null){
            this.profile = new Profile(ProfileType.CUSTOM);
            this.profile.setCreated(new Timestamp(0));
            this.profile.setName(Language.get(51));
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
        if (profile == null){
            return;
        }
        if (profile.hasName()){
            name.setText(profile.getName());
            name.setVisible(true);
            nameLabel.setVisible(true);
            nameEnabled = true;
        } else {
            if (profile.getType() != ProfileType.CUSTOM){
                name.setVisible(false);
                nameLabel.setVisible(false);
                nameEnabled = false;
            } else {
                name.setVisible(true);
                name.setText("");
                nameLabel.setVisible(true);
                nameEnabled = true;
            }
        }
        if (profile.getType() == ProfileType.CUSTOM){
            versionsLabel.setVisible(true);
            versions.setVisible(true);
            versionEnabled = true;
            this.versions.removeAllItems();
            int count = 0;
            this.versions.addItem(Language.get(59));
            count++;
            if (kernel.getSettings().getEnableSnapshots()){
                this.versions.addItem(Language.get(60));
                count++;
            }
            if ((profile.hasVersion() && profile.getVersionID().equalsIgnoreCase("latest-snapshot") && kernel.getSettings().getEnableSnapshots()) || (!profile.hasVersion() && profile.getType() == ProfileType.SNAPSHOT)){
                this.versions.setSelectedIndex(1);
            }
            Map<String, VersionMeta> versions = kernel.getVersions().getVersions();
            Set keySet = versions.keySet();
            for (Object aKeySet : keySet) {
                VersionMeta vm = versions.get(aKeySet.toString());
                if (vm.getType() == VersionType.RELEASE || (vm.getType() == VersionType.SNAPSHOT && kernel.getSettings().getEnableSnapshots()) || ((vm.getType() == VersionType.OLD_ALPHA || vm.getType() == VersionType.OLD_BETA) && kernel.getSettings().getEnableHistorical())) {
                    this.versions.addItem(vm.getID());
                    if (profile.hasVersion() && vm.getID().equalsIgnoreCase(profile.getVersionID())) {
                        this.versions.setSelectedIndex(count);
                    }
                    count++;
                }
            }
            deleteButton.setVisible(true);
        } else {
            versionEnabled = false;
            versionsLabel.setVisible(false);
            versions.setVisible(false);
            deleteButton.setVisible(false);
        }
        resolutionEnabled = profile.hasResolution();
        gameDirEnabled = profile.hasGameDir();
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
        if (!kernel.getSettings().getEnableAdvanced()){
            if (profile.hasJavaDir()){
                javaExecLabel.setVisible(true);
                javaExec.setVisible(true);
                javaExecLabel.setIcon(checkbox_enabled);
                javaExec.setText(profile.getJavaDir().getAbsolutePath());
                javaExecEnabled = true;
            } else {
                javaExecLabel.setVisible(false);
                javaExec.setVisible(false);
                javaExecEnabled = false;
            }
            if (profile.hasJavaArgs()){
                javaArgsLabel.setVisible(true);
                javaArgs.setVisible(true);
                javaArgsLabel.setIcon(checkbox_enabled);
                javaArgs.setText(profile.getJavaArgs());
                javaArgsEnabled = true;
            } else {
                javaArgsLabel.setVisible(false);
                javaArgs.setVisible(false);
                javaArgsEnabled = false;
            }
        } else {
            javaExecLabel.setVisible(true);
            javaExec.setVisible(true);
            javaArgsLabel.setVisible(true);
            javaArgs.setVisible(true);
            javaExecEnabled = profile.hasJavaDir();
            javaArgsEnabled = profile.hasJavaArgs();
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
        }
    }
    private void saveProfile(){
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
            } else if (versions.getSelectedIndex() == 1 && kernel.getSettings().getEnableSnapshots()){
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
        JOptionPane.showMessageDialog(null, Language.get(57), Language.get(58), JOptionPane.INFORMATION_MESSAGE);
        kernel.getGUI().setSelected(kernel.getGUI().options);
    }
    public JPanel getPanel(){
        return this.main;
    }
}
