package kml.gui;

import kml.Kernel;
import kml.enums.ProfileType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Objects;

public class ProfilePopupMenu
{
	private final Font bold  = new Font("Minecraftia", Font.BOLD, 14);
	private final Font plain = new Font("Minecraftia", Font.PLAIN, 14);
	private final Kernel         kernel;
	private final JPopupMenu     jPopupMenu;
	private final ActionListener popupListener;
	private ArrayList<JMenuItem> jMenuItems = new ArrayList<>();

	public ProfilePopupMenu(final Kernel kernel)
	{
		this.kernel = kernel;

		jPopupMenu = new JPopupMenu();
		jPopupMenu.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.DARK_GRAY));
		MenuScroller.setScrollerFor(jPopupMenu, 4, 250, 1, 0);

		this.popupListener = event -> {
			String action = event.getActionCommand();

			kernel.getProfiles().setSelectedProfile(event.getActionCommand());
			kernel.getGUI().updatePlayButton();

			//Update font
			for (JMenuItem menuItem : jMenuItems) {
				if (menuItem.getActionCommand().equals(action)) {
					menuItem.setFont(bold);
				}
				else {
					menuItem.setFont(plain);
				}
			}
			// When updating fonts it does not update it in the popupmenu so we have to readd all items.
			jPopupMenu.removeAll();
			for (JMenuItem menuItem : jMenuItems)
				jPopupMenu.add(menuItem);
		};
	}

	public void addElement(String id, Object aSet)
	{
		JMenuItem item = kernel.getProfiles().getProfile(id).getMenuItem();
		if (Objects.nonNull(kernel.getProfiles().getSelectedProfile())) {
			item.setFont(kernel.getProfiles().getSelectedProfile().equals(id) ? bold : plain);
		}
		jPopupMenu.add(item).setActionCommand(String.valueOf(aSet));
		item.addActionListener(popupListener);
		if (kernel.getProfiles().getProfile(id).getType().equals(ProfileType.RELEASE)) {
			jMenuItems.add(0, item); //Shifts all elements
			jPopupMenu.add(item, 0);
		}
		else {
			jMenuItems.add(item);
			jPopupMenu.add(item);
		}
		item.setActionCommand(String.valueOf(aSet));
	}

	public void clear()
	{
		jPopupMenu.removeAll();
		jMenuItems.clear();
	}

	public void showPopup(Component component)
	{
		Dimension d = jPopupMenu.getPreferredSize();
		d.width = 307;
		d.height = 39 * jMenuItems.size();
		if (jMenuItems.size() >= 6) {
			d.height += 20;
		}
		jPopupMenu.setPreferredSize(d);
		jPopupMenu.show(component, 0, -jPopupMenu.getPreferredSize().height + (jMenuItems.size() > 7 ? (jMenuItems.size() - 7) * 39 : 0) - 8);
	}
}
