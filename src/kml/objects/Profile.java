package kml.objects;

import kml.Language;
import kml.Utils;
import kml.enums.ProfileIcon;
import kml.enums.ProfileType;

import javax.swing.*;
import java.io.File;
import java.sql.Timestamp;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */

public class Profile
{
	private final String id;
	private       String name, javaArgs, lastVersionId;
	private ProfileType type;
	private File        gameDir, javaDir;
	private Timestamp created, lastUsed;
	private Map<String, Integer> resolution = new HashMap<>();
	private JLabel    listItem;
	private JMenuItem menuItem;

	public Profile(ProfileType type)
	{
		this.id = UUID.randomUUID().toString().replaceAll("-", "");
		this.type = type;
		this.lastUsed = new Timestamp(0);
	}

	public Profile(String id, String name, String type, String created, String lastUsed, String lastVersionId, String gameDir, String javaDir, String javaArgs, Map<String, Integer> resolution)
	{
		if (Objects.isNull(id)) {
			this.id = UUID.randomUUID().toString().replaceAll("-", "");
		}
		else {
			this.id = id;
		}
		this.name = name;

		this.lastVersionId = lastVersionId;
		if (Objects.nonNull(gameDir)) {
			this.gameDir = new File(gameDir);
			if (!this.gameDir.exists() || !this.gameDir.isDirectory()) {
				this.gameDir = null;
			}
		}
		if (Objects.nonNull(javaDir)) {
			this.javaDir = new File(javaDir);
			if (!this.javaDir.exists() || !this.javaDir.isFile()) {
				this.javaDir = null;
			}
		}
		this.javaArgs = javaArgs;
		this.resolution = resolution;
		if (Objects.isNull(lastUsed)) {
			this.lastUsed = new Timestamp(0);
		}
		else {
			try {
				this.lastUsed = Timestamp.valueOf(lastUsed.replace("T", " ").replace("Z", ""));
			}
			catch (Exception ex) {
				this.lastUsed = new Timestamp(0);
			}
		}
		if (Objects.isNull(type)) {
			this.type = ProfileType.CUSTOM;
		}
		else {
			type = type.toLowerCase();
			switch (type) {
				case "latest-release":
					this.type = ProfileType.RELEASE;
					break;
				case "latest-snapshot":
					this.type = ProfileType.SNAPSHOT;
					break;
				default:
					this.type = ProfileType.CUSTOM;
			}
		}

		if (this.type == ProfileType.CUSTOM) {
			if (Objects.isNull(created)) {
				this.created = new Timestamp(0);
			}
			else {
				try {
					this.created = Timestamp.valueOf(created.replace("T", " ").replace("Z", ""));
				}
				catch (Exception ex) {
					this.created = new Timestamp(0);
				}
			}
		}
	}

	public String getID() {return this.id;}

	public String getName() {return this.name;}

	public void setName(String newName) {this.name = newName;}

	public boolean hasName() {return Objects.nonNull(this.name);}

	public ProfileType getType() {return this.type;}

	public void setType(ProfileType type) {this.type = type;}

	public String getVersionID() {return this.lastVersionId;}

	public void setVersionID(String ver) {this.lastVersionId = ver;}

	public boolean hasVersion() {return Objects.nonNull(this.lastVersionId);}

	public File getGameDir() {return this.gameDir;}

	public void setGameDir(File dir) {this.gameDir = dir;}

	public boolean hasGameDir() {return Objects.nonNull(this.gameDir);}

	public File getJavaDir() {return this.javaDir;}

	public void setJavaDir(File dir) {this.javaDir = dir;}

	public boolean hasJavaDir() {return Objects.nonNull(this.javaDir);}

	public String getJavaArgs() {return this.javaArgs;}

	public void setJavaArgs(String args) {this.javaArgs = args;}

	public boolean hasJavaArgs() {return Objects.nonNull(this.javaArgs);}

	public Timestamp getLastUsed() {return lastUsed;}

	public void setLastUsed(Timestamp used) {this.lastUsed = used;}

	public boolean hasCreated() {return Objects.nonNull(this.created);}

	public Timestamp getCreated() {return this.created;}

	public void setCreated(Timestamp created) {this.created = created;}

	public boolean hasResolution()
	{
		return Objects.nonNull(this.resolution) && (this.resolution.size() == 2);
	}

	public int getResolutionHeight()
	{
		if (resolution.containsKey("height")) {
			return resolution.get("height");
		}
		return 0;
	}

	public int getResolutionWidth()
	{
		if (resolution.containsKey("width")) {
			return resolution.get("width");
		}
		return 0;
	}

	public void setResolution(int w, int h)
	{
		if (w < 0 || h < 0) {
			resolution = null;
		}
		else {
			if (Objects.isNull(resolution)) {
				resolution = new HashMap<>();
			}
			resolution.put("width", w);
			resolution.put("height", h);
		}
	}

	public JMenuItem getMenuItem()
	{
		if (Objects.isNull(this.menuItem)) {
			if (this.hasName()) {
				this.menuItem = new JMenuItem(this.getName());
			}
			else if (this.getType() == ProfileType.RELEASE) {
				this.menuItem = new JMenuItem(Language.get(59));
			}
			else if (this.getType() == ProfileType.SNAPSHOT) {
				this.menuItem = new JMenuItem(Language.get(60));
			}
			else {
				this.menuItem = new JMenuItem(Language.get(70));
			}
			this.menuItem.setIcon(Utils.getProfileIcon(ProfileIcon.GRASS));
		}
		else {
			if (this.hasName() && !this.getName().equals(this.listItem.getText())) {
				this.menuItem.setText(this.getName());
			}
			else if (this.getType() == ProfileType.RELEASE && !listItem.getText().equals(Language.get(59))) {
				this.menuItem.setText(Language.get(59));
			}
			else if (this.getType() == ProfileType.SNAPSHOT && !listItem.getText().equals(Language.get(60))) {
				this.menuItem.setText(Language.get(60));
			}
			else if (!this.hasName() && this.getType() == ProfileType.CUSTOM) {
				this.menuItem.setText(Language.get(70));
			}
		}
		this.menuItem.setIconTextGap(25);
		return this.menuItem;
	}

	public JLabel getListItem()
	{
		if (Objects.isNull(this.listItem)) {
			if (this.hasName()) {
				this.listItem = new JLabel(this.getName());
			}
			else if (this.getType() == ProfileType.RELEASE) {
				this.listItem = new JLabel(Language.get(59));
			}
			else if (this.getType() == ProfileType.SNAPSHOT) {
				this.listItem = new JLabel(Language.get(60));
			}
			else {
				this.listItem = new JLabel(Language.get(70));
			}
			this.listItem.setIcon(Utils.getProfileIcon(ProfileIcon.GRASS));
		}
		else {
			if (this.hasName() && !this.getName().equals(this.listItem.getText())) {
				this.listItem.setText(this.getName());
			}
			else if (this.getType() == ProfileType.RELEASE && !listItem.getText().equals(Language.get(59))) {
				this.listItem.setText(Language.get(59));
			}
			else if (this.getType() == ProfileType.SNAPSHOT && !listItem.getText().equals(Language.get(60))) {
				this.listItem.setText(Language.get(60));
			}
			else if (!this.hasName() && this.getType() == ProfileType.CUSTOM) {
				this.listItem.setText(Language.get(70));
			}
		}
		return this.listItem;
	}

	public String toString()
	{
		return "[Name: " + getName() + " | UUID: " + getID() + "]";
	}
}
