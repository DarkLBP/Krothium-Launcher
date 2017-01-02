package kml.gui;

import kml.*;
import kml.exceptions.DownloaderException;
import kml.exceptions.GameLauncherException;
import kml.objects.Browser;
import kml.objects.Profile;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * @author DarkLBP
 * website https://krothium.com
 */
public class Main extends JFrame{
    private JPanel main;
    private JButton playButton;
    private BackgroundPanel footerPanel;
    private BackgroundPanel headPanel;
    private BackgroundPanel contentPanel;
    public JLabel news;
    public JLabel skins;
    public JLabel settings;
    public JLabel options;
    private JButton profileButton;
    private JLabel logout;
    private JProgressBar progress;
    private JLabel language;
    private JLabel selected;
    private final Kernel kernel;
    private final LoginTab login;
    private final Browser browser;
    private final SettingsTab setting;
    private final LaunchOptionsTab launchOptions;
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
    private final ProfileEditor editor;
    private final ProfilePopup popupMenu;
    private final SkinTab skinTab;
    private final ImageIcon tabSelection;
    private final JPopupMenu languages;
    private final Font plain = new Font("Minecraftia", Font.PLAIN,16);
    private final Font bold = new Font("Minecraftia", Font.BOLD,16);
    private final ImageIcon flag_es;
    private final ImageIcon flag_pt;
    private final ImageIcon flag_us;
    private final ImageIcon flag_val;

    public Main(Kernel k){
        this.kernel = k;
        this.login = new LoginTab(k);
        this.browser = new Browser();
        this.setting = new SettingsTab(k);
        this.launchOptions = new LaunchOptionsTab(k);
        this.gameLauncher = kernel.getGameLauncher();
        this.downloader = kernel.getDownloader();
        this.playButton_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/playbutton.png")).getImage().getScaledInstance(325,70, Image.SCALE_SMOOTH));
        this.playButton_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/playbutton_hover.png")).getImage().getScaledInstance(325,70, Image.SCALE_SMOOTH));
        this.playButton_click = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/playbutton_click.png")).getImage().getScaledInstance(325,70, Image.SCALE_SMOOTH));
        this.profile_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/profile.png")).getImage().getScaledInstance(40,70, Image.SCALE_SMOOTH));
        this.profile_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/profile_hover.png")).getImage().getScaledInstance(40,70, Image.SCALE_SMOOTH));
        this.profile_click = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/profile_click.png")).getImage().getScaledInstance(40,70, Image.SCALE_SMOOTH));
        this.flag_es = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/flags/flag_es-es.png")).getImage().getScaledInstance(40,30, Image.SCALE_SMOOTH));
        this.flag_us = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/flags/flag_en-us.png")).getImage().getScaledInstance(40,30, Image.SCALE_SMOOTH));
        this.flag_pt = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/flags/flag_pt-pt.png")).getImage().getScaledInstance(40,30, Image.SCALE_SMOOTH));
        this.flag_val = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/flags/flag_val-es.png")).getImage().getScaledInstance(40,30, Image.SCALE_SMOOTH));
        this.tabSelection = new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/menu_label.png"));
        this.editor = new ProfileEditor(kernel);
        this.popupMenu = new ProfilePopup(kernel);
        this.skinTab = new SkinTab(kernel);
        this.languages = new JPopupMenu();
        setSize(950, 750);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        setIconImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/icon.png")).getImage());
        contentPanel.setImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/background.png")).getImage());
        contentPanel.setLayout(new FlowLayout());
        footerPanel.setImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/login-background.png")).getImage());
        headPanel.setImage(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/login-background.png")).getImage());
        setContentPane(main);
        news.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        skins.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        settings.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        options.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        playButton.setIcon(playButton_normal);
        profileButton.setIcon(profile_normal);
        profileButton.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        popupMenu.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        logout.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        language.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JMenuItem en = new JMenuItem("English - United States");
        JMenuItem es = new JMenuItem("Español - España");
        JMenuItem ca = new JMenuItem("Català (Valencià) - País Valencià");
        JMenuItem pt = new JMenuItem("Português - Portugal");
        ca.setIcon(flag_val);
        ca.setFont(plain);
        ca.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kernel.getSettings().setLocale("val-es");
                refreshAllLocalizedStrings();
            }
        });
        languages.add(ca);
        en.setFont(plain);
        en.setIcon(flag_us);
        en.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kernel.getSettings().setLocale("en-us");
                refreshAllLocalizedStrings();
            }
        });
        languages.add(en);
        es.setIcon(flag_es);
        es.setFont(plain);
        es.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kernel.getSettings().setLocale("es-es");
                refreshAllLocalizedStrings();
            }
        });
        languages.add(es);
        pt.setIcon(flag_pt);
        pt.setFont(plain);
        pt.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kernel.getSettings().setLocale("pt-pt");
                refreshAllLocalizedStrings();
            }
        });
        languages.add(pt);
        languages.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        languages.addPopupMenuListener(new PopupMenuListener() {
            @Override
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                for (MenuElement element: languages.getSubElements()){
                    if (element.getComponent().equals(ca) && kernel.getSettings().getLocale().equals("ca") || element.getComponent().equals(en) && kernel.getSettings().getLocale().equals("en-us") || element.getComponent().equals(es) && kernel.getSettings().getLocale().equals("es-es") || element.getComponent().equals(pt) && kernel.getSettings().getLocale().equals("pt-pt")){
                        element.getComponent().setFont(bold);
                    } else {
                        element.getComponent().setFont(plain);
                    }
                }
            }

            @Override
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}

            @Override
            public void popupMenuCanceled(PopupMenuEvent e) {}
        });
        language.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                languages.show(e.getComponent(), e.getX(), e.getY());
            }
        });
        MouseAdapter tabAdapter = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (e.getComponent() != selected){
                    e.getComponent().setForeground(Color.GREEN);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                e.getComponent().setForeground(Color.WHITE);
            }
            @Override
            public void mousePressed(MouseEvent e) {
                setSelected((JLabel)e.getComponent());
                e.getComponent().setForeground(Color.WHITE);
            }
        };
        news.addMouseListener(tabAdapter);
        skins.addMouseListener(tabAdapter);
        settings.addMouseListener(tabAdapter);
        options.addMouseListener(tabAdapter);
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
                    Thread runThread = new Thread(() -> {
                        if (!downloader.isDownloading() && !gameLauncher.isStarted()){
                            try {
                                downloader.download();
                                gameLauncher.launch();
                            } catch (GameLauncherException | DownloaderException e1) {
                                e1.printStackTrace();
                            }
                        }
                    });
                    runThread.start();
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                if (playButton.isEnabled()){
                    playButton.setIcon(playButton_hover);
                }
            }
            @Override
            public void mouseExited(MouseEvent e) {
                if (playButton.isEnabled()){
                    playButton.setIcon(playButton_normal);
                }
            }
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
            @Override
            public void mouseExited(MouseEvent e) {
                if (profileButton.isEnabled()){
                    profileButton.setIcon(profile_normal);
                }
            }
            @Override
            public void mousePressed(MouseEvent e) {
                if (profileButton.isEnabled()){
                    profileButton.setIcon(profile_click);
                    showPopupMenu(e);
                }
            }
            @Override
            public void mouseReleased(MouseEvent e) {
                if (profileButton.isEnabled()){
                    profileButton.setIcon(profile_normal);
                }
            }
        });
        logout.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (logout.isEnabled()){
                    int response = JOptionPane.showConfirmDialog(null, Language.get(8), Language.get(9), JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
                    if (response == JOptionPane.YES_OPTION){
                        kernel.getAuthentication().logOut();
                    }
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                logout.setForeground(Color.YELLOW);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                logout.setForeground(Color.WHITE);
            }
        });
        language.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {

            }
            @Override
            public void mouseEntered(MouseEvent e) {
                language.setForeground(Color.YELLOW);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                language.setForeground(Color.WHITE);
            }
        });
        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                kernel.exitSafely();
            }
        });
        guiThread = new TimerTask() {
            @Override
            public void run() {
                if (kernel.getAuthentication().isAuthenticated()){
                    if (componentsDisabled){
                        setDisable(false);
                    }
                    if (selected == null){
                        setSelected(news);
                    }
                    if (downloader.isDownloading()){
                        progress.setVisible(true);
                        playButton.setText(Language.get(13) + " " + kernel.getDownloader().getProgress() + "%");
                        progress.setValue(kernel.getDownloader().getProgress());
                        profileButton.setEnabled(false);
                    } else if (gameLauncher.isStarted()){
                        playButton.setText(Language.get(14));
                        progress.setVisible(false);
                        profileButton.setEnabled(false);
                    } else {
                        playButton.setText(Language.get(12));
                        progress.setVisible(false);
                        profileButton.setEnabled(true);
                    }
                } else {
                    if (!componentsDisabled){
                        setDisable(true);
                        contentPanel.setLayout(flowLayout);
                        contentPanel.add(login.getPanel());
                        contentPanel.updateUI();
                    }
                    progress.setVisible(false);
                }
            }
        };
        refreshAllLocalizedStrings();
    }
    public void refreshLocalizedStrings(){
        language.setText(Language.get(2));
        logout.setText(Language.get(3));
        news.setText(Language.get(4));
        skins.setText(Language.get(5));
        settings.setText(Language.get(6));
        options.setText(Language.get(7));
        playButton.setText(Language.get(12));
        profileButton.setToolTipText(Language.get(17));
    }
    public void refreshAllLocalizedStrings(){
        this.refreshLocalizedStrings();
        skinTab.refreshLocalizedStrings();
        launchOptions.refreshLocalizedStrings();
        setting.refreshLocalizedStrings();
        editor.refreshLocalizedStrings();
        login.refreshLocalizedStrings();
    }
    public void editProfile(String s){
        if (s == null){
            editor.setProfile(null);
            editor.refreshData();
            this.contentPanel.removeAll();
            this.contentPanel.add(editor.getPanel());
            this.contentPanel.updateUI();
        } else {
            if (kernel.getProfiles().existsProfile(s)){
                editor.setProfile(s);
                editor.refreshData();
                this.contentPanel.removeAll();
                this.contentPanel.add(editor.getPanel());
                this.contentPanel.updateUI();
            }
        }
    }
    @Override
    public void setVisible(boolean b){
        super.setVisible(b);
        if (b){
            timer.scheduleAtFixedRate(guiThread, 0, 500);
            kernel.getProfiles().updateSessionProfiles();
            String update = kernel.checkForUpdates();
            if (update != null){
                int response = JOptionPane.showConfirmDialog(null, Language.get(10), Language.get(11), JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (response == JOptionPane.YES_OPTION){
                    try {
                        Utils.openWebsite(Utils.fromBase64(update));
                    } catch (IOException e) {
                        kernel.getConsole().printError("Failed to open update page.\n" + e.getMessage());
                    }
                }
            }
        } else {
            timer.cancel();
        }
    }
    public void setSelected(JLabel l){
        if (!componentsDisabled){
            this.contentPanel.removeAll();
            if (selected == null ){
                selected = l;
                selected.setIcon(new ImageIcon(tabSelection.getImage().getScaledInstance(selected.getWidth() + 15, selected.getHeight() + 15, Image.SCALE_SMOOTH)));
            } else if (l != selected) {
                selected.setIcon(null);
                selected = l;
                selected.setIcon(new ImageIcon(tabSelection.getImage().getScaledInstance(selected.getWidth() + 15, selected.getHeight() + 15, Image.SCALE_SMOOTH)));
            }
            if (l.equals(news)){
                this.contentPanel.setLayout(borderLayout);
                this.contentPanel.add(this.browser.getPanel());
                browser.loadURL("http://mcupdate.tumblr.com/");
                browser.resizeBrowser(contentPanel.getSize());
            } else {
                this.contentPanel.setLayout(flowLayout);
                if (l.equals(settings)){
                    this.contentPanel.add(this.setting.getPanel());
                } else if (l.equals(options)){
                    this.contentPanel.add(this.launchOptions.getPanel());
                    this.launchOptions.populateList();
                } else if (l.equals(skins)){
                    this.contentPanel.add(this.skinTab.getPanel());
                    this.skinTab.refreshPreviews();
                }
            }
            this.headPanel.updateUI();
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
            this.logout.setEnabled(!b);
            componentsDisabled = b;
            if (!b){
                this.setSelected(news);
            }
        }
    }
    private void showPopupMenu(MouseEvent e){
        popupMenu.removeAll();
        Profiles p = kernel.getProfiles();
        Map<String, Profile> profs = p.getProfiles();
        Set set = profs.keySet();
        for (Object aSet : set) {
            popupMenu.addElement(profs.get(aSet.toString()).getID());
        }
        popupMenu.showPopup((JComponent)e.getComponent());
    }
}
