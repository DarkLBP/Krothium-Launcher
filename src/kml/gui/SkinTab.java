package kml.gui;

import kml.*;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class SkinTab {
    private JPanel main;
    private JButton changeSkinButton;
    private JButton deleteSkinButton;
    private JButton changeCapeButton;
    private JLabel capePreview;
    private JLabel skinPreview;
    private JButton deleteCapeButton;
    private JRadioButton steve;
    private JRadioButton alex;
    private JLabel skinType;
    private JLabel skinPreviewLabel;
    private JLabel capePreviewLabel;
    private final HashMap<String, String> params = new HashMap<>();
    private final Console console;
    private final Kernel kernel;
    private final ImageIcon button_normal;
    private final ImageIcon button_hover;
    private final JFileChooser fc = new JFileChooser();

    public SkinTab(final Kernel kernel) {
        this.kernel = kernel;
        console = kernel.getConsole();
        deleteCapeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                params.clear();
                int response = JOptionPane.showConfirmDialog(null, Language.get(36), Language.get(37), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION){
                    params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
                    params.put("Client-Token", kernel.getAuthentication().getClientToken());
                    params.put("Content-Length", "0");
                    try{
                        URL url = Constants.CHANGECAPE_URL;
                        if (!Constants.USE_HTTPS){
                            url = Utils.stringToURL(url.toString().replace("https", "http"));
                        }
                        String r = Utils.sendPost(url, new byte[0], params);
                        if (!r.equals("OK")){
                            console.printError("Failed to delete the cape.");
                            console.printError(r);
                            JOptionPane.showMessageDialog(null, Language.get(38) + "\n" + r, Language.get(23), JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, Language.get(39), Language.get(35), JOptionPane.INFORMATION_MESSAGE);
                            console.printInfo("Cape deleted successfully!");
                            refreshPreviews();
                        }
                    } catch (Exception ex){
                        console.printError("Failed to delete the cape.");
                        console.printError(ex.getMessage());
                        JOptionPane.showMessageDialog(null, Language.get(38) + "\n" + ex.getMessage(), Language.get(23), JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                deleteCapeButton.setIcon(button_hover);
                deleteCapeButton.setForeground(Color.YELLOW);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                deleteCapeButton.setIcon(button_normal);
                deleteCapeButton.setForeground(Color.RED);
            }
        });
        deleteSkinButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int response = JOptionPane.showConfirmDialog(null, Language.get(31), Language.get(32), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                if (response == JOptionPane.YES_OPTION){
                    params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
                    params.put("Client-Token", kernel.getAuthentication().getClientToken());
                    params.put("Content-Length", "0");
                    try{
                        URL url = Constants.CHANGESKIN_URL;
                        if (!Constants.USE_HTTPS){
                            url = Utils.stringToURL(url.toString().replace("https", "http"));
                        }
                        String r = Utils.sendPost(url, new byte[0], params);
                        if (!r.equals("OK")){
                            console.printError("Failed to delete the skin.");
                            console.printError(r);
                            JOptionPane.showMessageDialog(null, Language.get(33) + "\n" + r, Language.get(23), JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, Language.get(34), Language.get(35), JOptionPane.INFORMATION_MESSAGE);
                            console.printInfo("Skin deleted successfully!");
                            refreshPreviews();
                        }
                    } catch (Exception ex){
                        console.printError("Failed to delete the skin.");
                        console.printError(ex.getMessage());
                        JOptionPane.showMessageDialog(null, Language.get(33) + "\n" + ex.getMessage(), Language.get(23), JOptionPane.ERROR_MESSAGE);
                    }
                    params.clear();
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                deleteSkinButton.setIcon(button_hover);
                deleteSkinButton.setForeground(Color.YELLOW);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                deleteSkinButton.setIcon(button_normal);
                deleteSkinButton.setForeground(Color.RED);
            }
        });
        button_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_normal.png")).getImage().getScaledInstance(240, 50, Image.SCALE_SMOOTH));
        button_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_hover.png")).getImage().getScaledInstance(240, 50, Image.SCALE_SMOOTH));
        changeSkinButton.setIcon(button_normal);
        changeCapeButton.setIcon(button_normal);
        deleteSkinButton.setIcon(button_normal);
        deleteCapeButton.setIcon(button_normal);
        changeSkinButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        changeCapeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteSkinButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        deleteCapeButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        steve.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        alex.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        changeSkinButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                changeSkinButton.setIcon(button_hover);
                changeSkinButton.setForeground(Color.YELLOW);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                changeSkinButton.setIcon(button_normal);
                changeSkinButton.setForeground(Color.WHITE);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                int response = fc.showOpenDialog(kernel.getGUI());
                if (response == JFileChooser.APPROVE_OPTION){
                    try {
                        byte[] data = Files.readAllBytes(fc.getSelectedFile().toPath());
                        params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
                        params.put("Client-Token", kernel.getAuthentication().getClientToken());
                        if (alex.isSelected()){
                            params.put("Skin-Type", "alex");
                        } else {
                            params.put("Skin-Type", "steve");
                        }
                        params.put("Content-Type", "image/png");
                        params.put("Content-Length", String.valueOf(data.length));
                        URL url = Constants.CHANGESKIN_URL;
                        if (!Constants.USE_HTTPS){
                            url = Utils.stringToURL(url.toString().replace("https", "http"));
                        }
                        String r = Utils.sendPost(url, data, params);
                        if (!r.equals("OK")){
                            console.printError("Failed to change the skin.");
                            console.printError(r);
                            JOptionPane.showMessageDialog(null, Language.get(42) + "\n" + r, Language.get(23), JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, Language.get(40), Language.get(35), JOptionPane.INFORMATION_MESSAGE);
                            console.printInfo("Skin changed successfully!");
                            refreshPreviews();
                        }
                    } catch (Exception ex){
                        console.printError("Failed to change the skin.");
                        console.printError(ex.getMessage());
                        JOptionPane.showMessageDialog(null, Language.get(42) + "\n" + ex.getMessage(), Language.get(23), JOptionPane.ERROR_MESSAGE);
                    }
                    params.clear();
                }
            }
        });
        changeCapeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                changeCapeButton.setIcon(button_hover);
                changeCapeButton.setForeground(Color.YELLOW);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                changeCapeButton.setIcon(button_normal);
                changeCapeButton.setForeground(Color.WHITE);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                int response = fc.showOpenDialog(kernel.getGUI());
                if (response == JFileChooser.APPROVE_OPTION){
                    try {
                        byte[] data = Files.readAllBytes(fc.getSelectedFile().toPath());
                        params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
                        params.put("Client-Token", kernel.getAuthentication().getClientToken());
                        params.put("Content-Type", "image/png");
                        params.put("Content-Length", String.valueOf(data.length));
                        URL url = Constants.CHANGECAPE_URL;
                        if (!Constants.USE_HTTPS){
                            url = Utils.stringToURL(url.toString().replace("https", "http"));
                        }
                        String r = Utils.sendPost(url, data, params);
                        if (!r.equals("OK")){
                            console.printError("Failed to change the cape.");
                            console.printError(r);
                            JOptionPane.showMessageDialog(null, Language.get(43) + "\n" + r, Language.get(23), JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, Language.get(41), Language.get(35), JOptionPane.INFORMATION_MESSAGE);
                            console.printInfo("Cape changed successfully!");
                            refreshPreviews();
                        }
                    } catch (Exception ex){
                        console.printError("Failed to change the cape.");
                        console.printError(ex.getMessage());
                        JOptionPane.showMessageDialog(null, Language.get(43) + "\n" + ex.getMessage(), Language.get(23), JOptionPane.ERROR_MESSAGE);
                    }
                    params.clear();
                }
            }
        });
        fc.setAcceptAllFileFilterUsed(false);
        fc.setFileFilter(new FileFilter() {
            @Override
            public boolean accept(File f) {
                if (f.isFile()){
                    return f.getName().endsWith(".png") && f.length() <= 131072;
                } else {
                    return true;
                }
            }
            @Override
            public String getDescription() {
                return Language.get(44);
            }
        });
    }
    public void refreshLocalizedStrings(){
        changeSkinButton.setText(Language.get(24));
        deleteSkinButton.setText(Language.get(25));
        changeCapeButton.setText(Language.get(26));
        deleteCapeButton.setText(Language.get(27));
        skinType.setText(Language.get(28));
        skinPreviewLabel.setText(Language.get(29));
        capePreviewLabel.setText(Language.get(30));
    }
    public JPanel getPanel(){
        return this.main;
    }
    public void refreshPreviews(){
        Thread refresh = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    URL skinURL = new URL("http://mc.krothium.com/skins/" + kernel.getAuthentication().getSelectedUser().getDisplayName() + ".png");
                    skinPreview.setIcon(new ImageIcon(TexturePreview.generateComboSkin(skinURL, 4, 1)));
                } catch (Exception ex) {
                    console.printError("Failed to load skin preview!");
                    skinPreview.setIcon(null);
                }
                try {
                    URL capeURL = new URL("http://mc.krothium.com/capes/" + kernel.getAuthentication().getSelectedUser().getDisplayName() + ".png");
                    capePreview.setIcon(new ImageIcon(TexturePreview.generateComboCape(capeURL, 6, 1)));
                } catch (Exception ex) {
                    console.printError("Failed to load cape preview!");
                    capePreview.setIcon(null);
                }
            }
        });
        refresh.start();
    }
}
