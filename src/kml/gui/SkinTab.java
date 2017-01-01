package kml.gui;

import kml.*;
import kml.CapePreview;

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
 * Created by darkl on 01/01/2017.
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
    private HashMap<String, String> params = new HashMap<>();
    private final Console console;
    private final Kernel kernel;
    private final ImageIcon button_normal;
    private final ImageIcon button_hover;
    private final JFileChooser fc = new JFileChooser();

    public SkinTab(Kernel kernel) {
        this.kernel = kernel;
        console = kernel.getConsole();
        deleteCapeButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                params.clear();
                int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete your cape?", "Cape deletion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
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
                            JOptionPane.showMessageDialog(null, "Failed to delete the cape!\n" + r, "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Cape deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            console.printInfo("Cape deleted successfully!");
                            refreshPreviews();
                        }
                    } catch (Exception ex){
                        console.printError("Failed to delete the cape.");
                        console.printError(ex.getMessage());
                        JOptionPane.showMessageDialog(null, "Failed to delete the cape!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                int response = JOptionPane.showConfirmDialog(null, "Are you sure you want to delete your skin?", "Skin deletion", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
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
                            JOptionPane.showMessageDialog(null, "Failed to delete the skin!\n" + r, "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Skin deleted successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            console.printInfo("Skin deleted successfully!");
                            refreshPreviews();
                        }
                    } catch (Exception ex){
                        console.printError("Failed to delete the skin.");
                        console.printError(ex.getMessage());
                        JOptionPane.showMessageDialog(null, "Failed to delete the skin!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
        ButtonGroup group = new ButtonGroup();
        group.add(steve);
        group.add(alex);
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
                            JOptionPane.showMessageDialog(null, "Failed to change the skin!\n" + r, "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Skin changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            console.printInfo("Skin changed successfully!");
                            refreshPreviews();
                        }
                    } catch (Exception ex){
                        console.printError("Failed to change the skin.");
                        console.printError(ex.getMessage());
                        JOptionPane.showMessageDialog(null, "Failed to change the skin!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                            JOptionPane.showMessageDialog(null, "Failed to change the cape!\n" + r, "Error", JOptionPane.ERROR_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(null, "Cape changed successfully!", "Success", JOptionPane.INFORMATION_MESSAGE);
                            console.printInfo("Cape changed successfully!");
                            refreshPreviews();
                        }
                    } catch (Exception ex){
                        console.printError("Failed to change the cape.");
                        console.printError(ex.getMessage());
                        JOptionPane.showMessageDialog(null, "Failed to change the cape!\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
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
                return "Skin / Cape PNG File (128KB MAX)";
            }
        });
    }
    public JPanel getPanel(){
        return this.main;
    }
    public void refreshPreviews(){
        Thread refresh = new Thread(() -> {
            try {
                URL skinURL = new URL("http://mc.krothium.com/skins/" + kernel.getAuthentication().getSelectedUser().getDisplayName() + ".png");
                skinPreview.setIcon(new ImageIcon(SkinPreview.generateCombo(skinURL, 4, 1)));
            } catch (Exception ex){
                console.printError("Failed to load skin preview!");
                skinPreview.setIcon(null);
            }
            try {
                URL capeURL = new URL("http://mc.krothium.com/capes/" + kernel.getAuthentication().getSelectedUser().getDisplayName() + ".png");
                capePreview.setIcon(new ImageIcon(CapePreview.generateCombo(capeURL, 6, 1)));
            } catch (Exception ex){
                console.printError("Failed to load cape preview!");
                capePreview.setIcon(null);
            }
        });
        refresh.start();
    }
}
