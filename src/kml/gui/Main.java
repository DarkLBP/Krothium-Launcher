package kml.gui;

import kml.*;
import kml.exceptions.DownloaderException;
import kml.exceptions.GameLauncherException;
import kml.objects.Browser;
import kml.objects.Profile;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import javax.swing.*;
import javax.swing.event.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Timer;

/**
 * Created by darkl on 18/11/2016.
 */
public class Main extends JFrame{
    private JPanel panel1;
    private JButton playButton;
    private BackgroundPanel footerPanel;
    private BackgroundPanel headPanel;
    private BackgroundPanel contentPanel;
    private JLabel news;
    private JLabel skins;
    private JLabel settings;
    private JLabel options;
    private JButton profileButton;
    private JLabel selected;
    private final Kernel kernel;
    private final LoginTab login;
    private final Browser browser;
    private final SettingsTab setting;
    private boolean componentsDisabled = false;
    private final BorderLayout borderLayout = new BorderLayout();
    private final FlowLayout flowLayout = new FlowLayout();
    private final ImageIcon playButton_normal;
    private final ImageIcon playButton_hover;
    private final ImageIcon playButton_click;
    private final ImageIcon profile_normal;
    private final ImageIcon profile_hover;
    private final ImageIcon profile_click;
    private final Timer timer = new Timer();
    private final TimerTask guiThread;
    private final Downloader downloader;
    private final GameLauncher gameLauncher;
    private final JPopupMenu popupMenu = new JPopupMenu();

    public Main(Kernel k){
        this.kernel = k;
        this.login = new LoginTab(k);
        this.browser = new Browser();
        this.setting = new SettingsTab(k);
        this.gameLauncher = kernel.getGameLauncher();
        this.downloader = kernel.getDownloader();
        this.playButton_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/playbutton.png")).getImage().getScaledInstance(350,80, Image.SCALE_SMOOTH));
        this.playButton_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/playbutton_hover.png")).getImage().getScaledInstance(350,80, Image.SCALE_SMOOTH));
        this.playButton_click = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/playbutton_click.png")).getImage().getScaledInstance(350,80, Image.SCALE_SMOOTH));
        this.profile_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/profile.png")).getImage().getScaledInstance(40,80, Image.SCALE_SMOOTH));
        this.profile_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/profile_hover.png")).getImage().getScaledInstance(40,80, Image.SCALE_SMOOTH));
        this.profile_click = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/profile_click.png")).getImage().getScaledInstance(40,80, Image.SCALE_SMOOTH));
        setSize(900, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        setIconImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/icon.png")).getImage());
        contentPanel.setImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/background.png")).getImage());
        contentPanel.setLayout(new FlowLayout());
        footerPanel.setImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/login-background.png")).getImage());
        headPanel.setImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/login-background.png")).getImage());
        setContentPane(panel1);
        news.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        skins.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        settings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        options.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playButton.setIcon(playButton_normal);
        profileButton.setIcon(profile_normal);
        profileButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        popupMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        news.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (news != selected){
                    news.setForeground(Color.GREEN);
                }
            }
        });
        news.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                news.setForeground(Color.WHITE);
            }
        });
        news.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setSelected(news);
                news.setForeground(Color.WHITE);
            }
        });
        skins.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (skins != selected){
                    skins.setForeground(Color.GREEN);
                }
            }
        });
        skins.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                skins.setForeground(Color.WHITE);
            }
        });
        skins.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setSelected(skins);
                skins.setForeground(Color.WHITE);
            }
        });
        settings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (settings != selected){
                    settings.setForeground(Color.GREEN);
                }
            }
        });
        settings.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                settings.setForeground(Color.WHITE);
            }
        });
        settings.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setSelected(settings);
                settings.setForeground(Color.WHITE);
            }
        });
        options.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (options != selected){
                    options.setForeground(Color.GREEN);
                }
            }
        });
        options.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                options.setForeground(Color.WHITE);
            }
        });
        options.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                setSelected(options);
                options.setForeground(Color.WHITE);
            }
        });
        contentPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                if (selected != null){
                    if (selected.equals(news)){
                        browser.resizeBrowser(e.getComponent().getSize());
                    }
                }
            }
        });
        playButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (playButton.isEnabled()){
                    playButton.setIcon(playButton_click);
                }
            }
        });
        playButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (!downloader.isDownloading() && !gameLauncher.isStarted()){
                    try {
                        downloader.download();
                        gameLauncher.launch();
                    } catch (GameLauncherException e1) {
                        e1.printStackTrace();
                    } catch (DownloaderException e1) {
                        e1.printStackTrace();
                    }
                }
            }
        });
        playButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (playButton.isEnabled()){
                    playButton.setIcon(playButton_hover);
                }
            }
        });
        playButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (playButton.isEnabled()){
                    playButton.setIcon(playButton_normal);
                }
            }
        });
        playButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (playButton.isEnabled()){
                    playButton.setIcon(playButton_normal);
                }
            }
        });
        profileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (profileButton.isEnabled()){
                    profileButton.setIcon(profile_hover);
                }
            }
        });
        profileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseExited(MouseEvent e) {
                if (profileButton.isEnabled()){
                    profileButton.setIcon(profile_normal);
                }
            }
        });
        profileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (profileButton.isEnabled()){
                    profileButton.setIcon(profile_click);
                }
            }
        });
        profileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseReleased(MouseEvent e) {
                if (profileButton.isEnabled()){
                    profileButton.setIcon(profile_normal);
                }
            }
        });
        profileButton.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (profileButton.isEnabled()){
                    showPopupMenu(e);
                }
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                kernel.exitSafely();
            }
        });
        guiThread = new TimerTask() {
            public void run() {
                if (kernel.isAuthenticated()){
                    if (componentsDisabled){
                        setDisable(false);
                    }
                    if (selected == null){
                        setSelected(news);
                    }
                    if (downloader.isDownloading()){
                        playButton.setText("DOWNLOADING");
                    } else if (gameLauncher.isStarted()){
                        playButton.setText("PLAYING");
                    } else {
                        playButton.setText("PLAY");
                    }
                } else {
                    if (!componentsDisabled){
                        setDisable(true);
                        contentPanel.setLayout(flowLayout);
                        contentPanel.add(login.getPanel());
                    }
                }
            }
        };

    }
    @Override
    public void setVisible(boolean b){
        super.setVisible(b);
        timer.scheduleAtFixedRate(guiThread, 0, 1000);
    }
    private void setSelected(JLabel l){
        if (!componentsDisabled){
            this.contentPanel.removeAll();
            if (selected == null ){
                selected = l;
                selected.setIcon(new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/menu_label.png")).getImage().getScaledInstance(selected.getWidth() + 15, selected.getHeight() + 15, Image.SCALE_SMOOTH)));
            } else if (l != selected) {
                selected.setIcon(null);
                selected = l;
                selected.setIcon(new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/menu_label.png")).getImage().getScaledInstance(selected.getWidth() + 15, selected.getHeight() + 15, Image.SCALE_SMOOTH)));
            }
            if (l.equals(news)){
                this.contentPanel.setLayout(borderLayout);
                this.contentPanel.add(this.browser.getPanel());
                browser.loadURL("http://mcupdate.tumblr.com/");
            } else {
                this.contentPanel.setLayout(flowLayout);
                if (l.equals(settings)){
                    this.contentPanel.add(this.setting.getPanel());
                }
            }
            this.contentPanel.updateUI();
        }
    }
    public void setDisable(boolean b){
        if (b != componentsDisabled){
            this.news.setEnabled(!b);
            this.settings.setEnabled(!b);
            this.options.setEnabled(!b);
            this.skins.setEnabled(!b);
            this.playButton.setEnabled(!b);
            this.profileButton.setEnabled(!b);
            componentsDisabled = b;
            if (!b){
                this.setSelected(news);
            }
        }
    }
    public void showPopupMenu(MouseEvent e){
        popupMenu.removeAll();
        Profiles p = kernel.getProfiles();
        Map<String, Profile> profs = p.getProfiles();
        Set set = profs.keySet();
        Iterator it = set.iterator();
        while (it.hasNext()){
            popupMenu.add(profs.get(it.next().toString()).getName());
        }
        popupMenu.show(e.getComponent(), e.getX(), e.getY());
    }
}
