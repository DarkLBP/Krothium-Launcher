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
    private final Kernel kernel;
    private Profile profile = null;
    private final ImageIcon checkbox_enabled = new ImageIcon(SettingsTab.class.getResource("/kml/gui/textures/checkbox_enabled.png"));
    private final ImageIcon checkbox_disabled = new ImageIcon(SettingsTab.class.getResource("/kml/gui/textures/checkbox_disabled.png"));
    private boolean resolutionEnabled, gameDirEnabled, javaExecEnabled, javaArgsEnabled;

    public ProfileEditor(Kernel k){
        setContentPane(main);
        this.kernel = k;
        contentPanel.setImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/background.png")).getImage());
        resolutionLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        gameDirLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        javaExecLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        javaArgsLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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
            }
        });

    }
    @Override
    public void setVisible(boolean b){
        name.setEnabled(true);
        versions.setEnabled(true);
        javaExec.setEnabled(true);
        javaArgs.setEnabled(true);
        if (profile.hasName()){
            setTitle("Profile Editor: " + profile.getName());
            name.setText(profile.getName());
        } else {
            if (profile.getType() == ProfileType.RELEASE){
                setTitle("Profile Editor: Latest Release");
                name.setEnabled(false);
                versions.setEnabled(false);
                name.setText("Latest Release");
            } else if (profile.getType() == ProfileType.SNAPSHOT){
                setTitle("Profile Editor: Latest Snapshot");
                name.setEnabled(false);
                versions.setEnabled(false);
                name.setName("Latest Snapshot");
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
    }
    public void setProfile(Profile p){
        this.profile = p;
    }
}
