package kml.gui;

import kml.Kernel;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * @author DarkLBP
 *         website https://krothium.com
 */
public class ProfilePopup extends JFrame
{
	private JPanel        main;
	private JList<String> profiles;
	private final DefaultListModel<String> model = new DefaultListModel<>();
	private final Font                     bold  = new Font("Minecraftia", Font.BOLD, 15);
	private final Font                     plain = new Font("Minecraftia", Font.PLAIN, 15);

	public ProfilePopup(final Kernel kernel)
	{
		setUndecorated(true);
		setBackground(new Color(239, 240, 241));
		setContentPane(main);
		main.setBorder(BorderFactory.createMatteBorder(2, 2, 2, 2, Color.DARK_GRAY));

		getContentPane().setPreferredSize(new Dimension(285, 156));
		pack();

		profiles.setModel(model);
		profiles.setVisibleRowCount(3);
		profiles.setFixedCellHeight(50);
		profiles.setOpaque(true);
		profiles.setCellRenderer(new DefaultListCellRenderer()
		{
			@Override
			public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus)
			{
				JLabel label = kernel.getProfiles().getProfile((String) value).getListItem();
				label.setIconTextGap(25);
				if (kernel.getProfiles().getSelectedProfile() != null && kernel.getProfiles().getSelectedProfile().equals(value)) {
					label.setFont(bold);
				}
				else {
					label.setFont(plain);
				}
				label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Color.DARK_GRAY));
				return label;
			}
		});

		profiles.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mousePressed(MouseEvent e)
			{
				kernel.getProfiles().setSelectedProfile(profiles.getSelectedValue());
				kernel.getGUI().updatePlayButton();
				setVisible(false);
			}
		});

		addWindowFocusListener(new WindowAdapter()
		{
			@Override
			public void windowLostFocus(WindowEvent e)
			{
				setVisible(false);
			}
		});
	}

	public void addElement(String element)
	{
		model.addElement(element);
	}

	public void removeAll()
	{
		model.removeAllElements();
	}

	public void showPopup(JComponent component)
	{
		setLocationRelativeTo(component);
		setLocation(getX() + 125, getY() - 122);
		setVisible(true);
	}
}
