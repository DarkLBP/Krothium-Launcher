package kml.gui;

import kml.*;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Objects;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class SkinTab
{
	private final HashMap<String, String> params = new HashMap<>();
	private final Console   console;
	private final Kernel    kernel;
	private final ImageIcon button_normal;
	private final ImageIcon button_hover;
	private final JFileChooser fc = new JFileChooser();
	private JPanel  main;
	private JButton changeSkinButton, deleteSkinButton, changeCapeButton, deleteCapeButton;
	private JLabel capePreview, skinPreview;
	private JRadioButton steve, alex;
	private JLabel skinType, skinPreviewLabel, capePreviewLabel;

	public SkinTab(final Kernel kernel)
	{
		this.kernel = kernel;
		console = kernel.getConsole();
		deleteCapeButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				deleteCapeButton.setForeground(Color.YELLOW);
				int response = JOptionPane.showConfirmDialog(null, Language.get(36), Language.get(37), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.YES_OPTION) {
					params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
					params.put("Client-Token", kernel.getAuthentication().getClientToken());
					params.put("Content-Length", "0");
					try {
						String r = Utils.sendPost(Constants.CHANGECAPE_URL, new byte[0], params);
						if (!r.equals("OK")) {
							console.printError("Failed to delete the cape.");
							console.printError(r);
							JOptionPane.showMessageDialog(null, Language.get(38) + "\n" + r, Language.get(23), JOptionPane.ERROR_MESSAGE);
						}
						else {
							JOptionPane.showMessageDialog(null, Language.get(39), Language.get(35), JOptionPane.INFORMATION_MESSAGE);
							console.printInfo("Cape deleted successfully!");
							refreshPreviews();
						}
					}
					catch (Exception ex) {
						console.printError("Failed to delete the cape.");
						console.printError(ex.getMessage());
						JOptionPane.showMessageDialog(null, Language.get(38) + "\n" + ex.getMessage(), Language.get(23), JOptionPane.ERROR_MESSAGE);
					}
					params.clear();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				deleteCapeButton.setIcon(button_hover);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				deleteCapeButton.setIcon(button_normal);
				deleteCapeButton.setForeground(Color.RED);
			}
		});
		deleteSkinButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				deleteSkinButton.setForeground(Color.YELLOW);
				int response = JOptionPane.showConfirmDialog(null, Language.get(31), Language.get(32), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
				if (response == JOptionPane.YES_OPTION) {
					params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
					params.put("Client-Token", kernel.getAuthentication().getClientToken());
					params.put("Content-Length", "0");
					try {
						String r = Utils.sendPost(Constants.CHANGESKIN_URL, new byte[0], params);
						if (!r.equals("OK")) {
							console.printError("Failed to delete the skin.");
							console.printError(r);
							JOptionPane.showMessageDialog(null, Language.get(33) + "\n" + r, Language.get(23), JOptionPane.ERROR_MESSAGE);
						}
						else {
							JOptionPane.showMessageDialog(null, Language.get(34), Language.get(35), JOptionPane.INFORMATION_MESSAGE);
							console.printInfo("Skin deleted successfully!");
							refreshPreviews();
						}
					}
					catch (Exception ex) {
						console.printError("Failed to delete the skin.");
						console.printError(ex.getMessage());
						JOptionPane.showMessageDialog(null, Language.get(33) + "\n" + ex.getMessage(), Language.get(23), JOptionPane.ERROR_MESSAGE);
					}
					params.clear();
				}
			}

			@Override
			public void mouseEntered(MouseEvent e)
			{
				deleteSkinButton.setIcon(button_hover);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				deleteSkinButton.setIcon(button_normal);
				deleteSkinButton.setForeground(Color.RED);
			}
		});
		button_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_normal.png")).getImage().getScaledInstance(220, 40, Image.SCALE_SMOOTH));
		button_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/button_hover.png")).getImage().getScaledInstance(220, 40, Image.SCALE_SMOOTH));
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
		changeSkinButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				changeSkinButton.setIcon(button_hover);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				changeSkinButton.setIcon(button_normal);
				changeSkinButton.setForeground(Color.WHITE);
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				changeSkinButton.setForeground(Color.YELLOW);
				int response = fc.showOpenDialog(kernel.getGUI());
				if (response == JFileChooser.APPROVE_OPTION) {
					try {
						byte[] data = Files.readAllBytes(fc.getSelectedFile().toPath());
						params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
						params.put("Client-Token", kernel.getAuthentication().getClientToken());
						if (alex.isSelected()) {
							params.put("Skin-Type", "alex");
						}
						else {
							params.put("Skin-Type", "steve");
						}
						params.put("Content-Type", "image/png");
						params.put("Content-Length", String.valueOf(data.length));
						String r = Utils.sendPost(Constants.CHANGESKIN_URL, data, params);
						if (!r.equals("OK")) {
							console.printError("Failed to change the skin.");
							console.printError(r);
							JOptionPane.showMessageDialog(null, Language.get(42) + "\n" + r, Language.get(23), JOptionPane.ERROR_MESSAGE);
						}
						else {
							JOptionPane.showMessageDialog(null, Language.get(40), Language.get(35), JOptionPane.INFORMATION_MESSAGE);
							console.printInfo("Skin changed successfully!");
							refreshPreviews();
						}
					}
					catch (Exception ex) {
						console.printError("Failed to change the skin.");
						console.printError(ex.getMessage());
						JOptionPane.showMessageDialog(null, Language.get(42) + "\n" + ex.getMessage(), Language.get(23), JOptionPane.ERROR_MESSAGE);
					}
					params.clear();
				}
			}
		});
		changeCapeButton.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseEntered(MouseEvent e)
			{
				changeCapeButton.setIcon(button_hover);
			}

			@Override
			public void mouseExited(MouseEvent e)
			{
				changeCapeButton.setIcon(button_normal);
				changeCapeButton.setForeground(Color.WHITE);
			}

			@Override
			public void mousePressed(MouseEvent e)
			{
				changeCapeButton.setForeground(Color.YELLOW);
				int response = fc.showOpenDialog(kernel.getGUI());
				if (response == JFileChooser.APPROVE_OPTION) {
					try {
						byte[] data = Files.readAllBytes(fc.getSelectedFile().toPath());
						params.put("Access-Token", kernel.getAuthentication().getSelectedUser().getAccessToken());
						params.put("Client-Token", kernel.getAuthentication().getClientToken());
						params.put("Content-Type", "image/png");
						params.put("Content-Length", String.valueOf(data.length));
						String r = Utils.sendPost(Constants.CHANGECAPE_URL, data, params);
						if (!r.equals("OK")) {
							console.printError("Failed to change the cape.");
							console.printError(r);
							JOptionPane.showMessageDialog(null, Language.get(43) + "\n" + r, Language.get(23), JOptionPane.ERROR_MESSAGE);
						}
						else {
							JOptionPane.showMessageDialog(null, Language.get(41), Language.get(35), JOptionPane.INFORMATION_MESSAGE);
							console.printInfo("Cape changed successfully!");
							refreshPreviews();
						}
					}
					catch (Exception ex) {
						console.printError("Failed to change the cape.");
						console.printError(ex.getMessage());
						JOptionPane.showMessageDialog(null, Language.get(43) + "\n" + ex.getMessage(), Language.get(23), JOptionPane.ERROR_MESSAGE);
					}
					params.clear();
				}
			}
		});
		fc.setAcceptAllFileFilterUsed(false);
		fc.setFileFilter(new FileFilter()
		{
			@Override
			public boolean accept(File f)
			{
				return !f.isFile() || f.getName().endsWith(".png") && f.length() <= 131072;
			}

			@Override
			public String getDescription()
			{
				return Language.get(44);
			}
		});
	}

	public void refreshLocalizedStrings()
	{
		changeSkinButton.setText(Language.get(24));
		deleteSkinButton.setText(Language.get(25));
		changeCapeButton.setText(Language.get(26));
		deleteCapeButton.setText(Language.get(27));
		skinType.setText(Language.get(28));
		skinPreviewLabel.setText(Language.get(29));
		capePreviewLabel.setText(Language.get(30));
	}

	public JPanel getPanel()
	{
		return this.main;
	}

	public void refreshPreviews()
	{
		Thread refresh = new Thread(() -> {
			try {
				skinPreview.setIcon(null);
				capePreview.setIcon(null);
				URL    profileURL = new URL("https://mc.krothium.com/profiles/" + kernel.getAuthentication().getSelectedUser().getProfileID());
				String profile    = Utils.readURL(profileURL);
				if (Objects.nonNull(profile)) {
					JSONArray  properties = new JSONObject(Utils.readURL(profileURL)).getJSONArray("properties");
					JSONObject textures   = new JSONObject(Utils.fromBase64(properties.getJSONObject(0).getString("value"))).getJSONObject("textures");
					if (textures.has("SKIN")) {
						try {
							JSONObject skin = textures.getJSONObject("SKIN");
							if (skin.has("metadata")) {
								alex.setSelected(true);
							}
							URL skinURL = new URL(skin.getString("url"));
							skinPreview.setIcon(new ImageIcon(TexturePreview.generateComboSkin(skinURL, 3, 1)));
						}
						catch (Exception ex) {
							console.printInfo("No skin found.");
						}
					}
					if (textures.has("CAPE")) {
						try {
							JSONObject cape    = textures.getJSONObject("CAPE");
							URL        capeURL = new URL(cape.getString("url"));
							capePreview.setIcon(new ImageIcon(TexturePreview.generateComboCape(capeURL, 5, 1)));
						}
						catch (Exception ex) {
							console.printInfo("No cape found.");
						}
					}
				}
				else {
					console.printError("Failed to load profile textures data. No server response.");
				}
			}
			catch (Exception ex) {
				console.printError("Failed to load profile textures data: " + ex);
				skinPreview.setIcon(null);
			}
		});
		refresh.start();
	}
}
