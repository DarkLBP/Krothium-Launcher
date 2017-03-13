package kml.gui;

import kml.Kernel;

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

	private final Kernel kernel;
	private JPopupMenu jPopupMenu = new JPopupMenu();
	private ActionListener popupListener;

	public ProfilePopupMenu(final Kernel kernel)
	{
		this.kernel = kernel;
		MenuScroller.setScrollerFor(this.jPopupMenu, 3, 125, 1, 0);
		this.jPopupMenu.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.DARK_GRAY));
		popupListener = new ActionListener()
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
		if (kernel.getProfiles().getSelectedProfile() != null && kernel.getProfiles().getSelectedProfile().equals(id)) {
			item.setFont(bold);
		}
		else {
			item.setFont(plain);
		}
		jPopupMenu.add(item).setActionCommand(String.valueOf(aSet));
		item.addActionListener(popupListener);
		jMenuItems.add(item);
	}

	public JPopupMenu getjPopupMenu()
	{
		return jPopupMenu;
	}

	public void clear()
	{
		jPopupMenu.removeAll();
		jMenuItems.clear();
	}

	public void showPopup(Component component)
	{
		jPopupMenu.show(component, 0, (jMenuItems.size() > 4 ? ((-jPopupMenu.getPreferredSize().height) - 12) : ((-jPopupMenu.getPreferredSize().height) - 8)));
	}
}
