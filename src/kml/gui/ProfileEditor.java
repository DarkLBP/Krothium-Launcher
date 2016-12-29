package kml.gui;

import kml.Kernel;
import kml.Utils;
import kml.enums.OSArch;
import kml.enums.ProfileType;
import kml.objects.Profile;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

/**
 * Created by darkl on 27/12/2016.
 */
public class ProfileEditor extends JFrame{
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
    private final Kernel kernel;
    private Profile profile = null;
    private final ImageIcon checkbox_enabled = new ImageIcon(SettingsTab.class.getResource("/kml/gui/textures/checkbox_enabled.png"));
    private final ImageIcon checkbox_disabled = new ImageIcon(SettingsTab.class.getResource("/kml/gui/textures/checkbox_disabled.png"));
    private final ImageIcon button_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_normal.png")).getImage().getScaledInstance(175, 40, Image.SCALE_SMOOTH));
    private final ImageIcon button_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_hover.png")).getImage().getScaledInstance(175, 40, Image.SCALE_SMOOTH));
    private boolean nameEnabled, versionEnabled, resolutionEnabled, gameDirEnabled, javaExecEnabled, javaArgsEnabled;

    public ProfileEditor(Kernel k, LaunchOptionsTab tab){
        setContentPane(main);
        setSize(650, 450);
        setResizable(false);
        setIconImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/icon.png")).getImage());
        this.kernel = k;
        contentPanel.setImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/background.png")).getImage());
        resolutionLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gameDirLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        javaExecLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        javaArgsLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        cancelButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        saveButton.setIcon(button_normal);
        cancelButton.setIcon(button_normal);
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
                ProfileEditor.this.setVisible(false);
            }
        });
        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (nameEnabled){
                    if (!name.getText().isEmpty()){
                        profile.setName(name.getText());
                    } else {
                        profile.setName(null);
                    }
                } else {
                    profile.setName(null);
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
                tab.populateList();
                setVisible(false);
            }
        });
        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                saveButton.setIcon(button_hover);
            }
        });
        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                saveButton.setIcon(button_normal);
            }
        });
        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                saveButton.setForeground(Color.YELLOW);
            }
        });
        saveButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                saveButton.setForeground(Color.WHITE);
            }
        });
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                cancelButton.setIcon(button_hover);
            }
        });
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                cancelButton.setIcon(button_normal);
            }
        });
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                cancelButton.setForeground(Color.YELLOW);
            }
        });
        cancelButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                cancelButton.setForeground(Color.WHITE);
            }
        });
    }
    @Override
    public void setVisible(boolean b){
        if (b){
            refreshData();
        }
        super.setVisible(b);
    }
    public boolean setProfile(String p){
        if (kernel.getProfiles().existsProfile(p)){
            this.profile = kernel.getProfiles().getProfile(p);
            return true;
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
            setTitle("Profile Editor: " + profile.getName());
            name.setText(profile.getName());
            nameEnabled = true;
            versionEnabled = true;
        } else {
            if (profile.getType() == ProfileType.RELEASE){
                setTitle("Profile Editor: Latest Release");
                name.setEnabled(false);
                versions.setEnabled(false);
                nameEnabled = false;
                versionEnabled = false;
                name.setText("Latest Release");
            } else if (profile.getType() == ProfileType.SNAPSHOT){
                setTitle("Profile Editor: Latest Snapshot");
                name.setEnabled(false);
                versions.setEnabled(false);
                nameEnabled = false;
                versionEnabled = false;
                name.setText("Latest Snapshot");
            } else {
                setTitle("Profile Editor: Unnamed Profile");
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
    }
}
