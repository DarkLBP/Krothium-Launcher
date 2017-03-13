package kml.gui;

import kml.Kernel;
import kml.enums.ProfileType;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class ProfilePopupMenu
{
	private ArrayList<JMenuItem> jMenuItems = new ArrayList<>();

	private final Font bold  = new Font("Minecraftia", Font.BOLD, 14);
	private final Font plain = new Font("Minecraftia", Font.PLAIN, 14);

	private final Kernel         kernel;
	private final JPopupMenu     jPopupMenu;
	private final ActionListener popupListener;

	public ProfilePopupMenu(final Kernel kernel)
	{
		this.kernel = kernel;

		jPopupMenu = new JPopupMenu();
		jPopupMenu.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.DARK_GRAY));
		MenuScroller.setScrollerFor(jPopupMenu, 3, 125, 1, 0);

		this.popupListener = new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
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
			}
		};
	}

	public void addElement(String id, Object aSet)
	{
		JMenuItem item = kernel.getProfiles().getProfile(id).getMenuItem();
		if (kernel.getProfiles().getSelectedProfile() != null) {
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
		Point pos = new Point();

		// Adjust the x position so that the left side of the popup
		// appears at the center of  the component
		pos.x = (component.getWidth() / 2);

		// Adjust the y position so that the y position (top corner)
		// is positioned so that the bottom of the popup
		// appears in the center
		pos.y = (component.getHeight() / 2) - jPopupMenu.getHeight();

		// For some reason everytime I modify the JPopupMenu's contents ( adding or deleting an item ) it does not update the height.
		// After showing the menu it updates it self and it's working as intended.
		// Basically you have to click it twice..
		jPopupMenu.show(component, (int)pos.getX(), (int)pos.getY());
	}
}
