package kml.gui;

import kml.*;
import kml.enums.ProfileType;
import kml.exceptions.AuthenticationException;
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
    private JButton playButton, profileButton;
    private BackgroundPanel footerPanel, headPanel, contentPanel;
    public JLabel news, skins, settings, options, logout, language, selected;
    private JProgressBar progress;
    private final Kernel kernel;
    private final LoginTab login;
    private final Browser browser;
    private final SettingsTab setting;
    private final LaunchOptionsTab launchOptions;
    private boolean componentsDisabled = false;
    private final BorderLayout borderLayout = new BorderLayout();
    private final FlowLayout flowLayout = new FlowLayout();
    private final ImageIcon playButton_normal, playButton_hover, profile_normal, profile_hover;
    private final Timer timer = new Timer();
    private final Downloader downloader;
    private final GameLauncher gameLauncher;
    private final ProfileEditor editor;
    private final ProfilePopup popupMenu;
    private final SkinTab skinTab;
    private final ImageIcon tabSelection;
    private final JPopupMenu languages;
    private final Font plain = new Font("Minecraftia", Font.PLAIN,14);
    private final Font bold = new Font("Minecraftia", Font.BOLD,14);
    private final ImageIcon flag_es, flag_pt, flag_us, flag_val, flag_br, flag_hu;
    private final ImageIcon newsIcon, skinsIcon, settingsIcon, optionsIcon;
    private final Console console;
    private boolean wasPlaying;

    public Main(Kernel k){
        setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
        this.kernel = k;
        this.console = k.getConsole();
        this.login = new LoginTab(k);
        this.browser = new Browser(k);
        this.setting = new SettingsTab(k);
        this.launchOptions = new LaunchOptionsTab(k);
        this.gameLauncher = kernel.getGameLauncher();
        this.downloader = kernel.getDownloader();
        this.playButton_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/playbutton.png")).getImage().getScaledInstance(350,70, Image.SCALE_SMOOTH));
        this.playButton_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/playbutton_hover.png")).getImage().getScaledInstance(350,70, Image.SCALE_SMOOTH));
        this.profile_normal = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/profile.png")).getImage().getScaledInstance(40,70, Image.SCALE_SMOOTH));
        this.profile_hover = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/profile_hover.png")).getImage().getScaledInstance(40,70, Image.SCALE_SMOOTH));
        this.flag_es = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/flags/flag_es-es.png")).getImage().getScaledInstance(40,30, Image.SCALE_SMOOTH));
        this.flag_us = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/flags/flag_en-us.png")).getImage().getScaledInstance(40,30, Image.SCALE_SMOOTH));
        this.flag_pt = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/flags/flag_pt-pt.png")).getImage().getScaledInstance(40,30, Image.SCALE_SMOOTH));
        this.flag_val = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/flags/flag_val-es.png")).getImage().getScaledInstance(40,30, Image.SCALE_SMOOTH));
        this.flag_br = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/flags/flag_pt-br.png")).getImage().getScaledInstance(40,30, Image.SCALE_SMOOTH));
        this.flag_hu = new ImageIcon(new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/flags/flag_hu-hu.png")).getImage().getScaledInstance(40,30, Image.SCALE_SMOOTH));
        this.tabSelection = new ImageIcon(LoginTab.class.getResource("/kml/gui/textures/menu_label.png"));
        this.newsIcon = new ImageIcon(tabSelection.getImage().getScaledInstance(115, 35, Image.SCALE_SMOOTH));
        this.skinsIcon = new ImageIcon(tabSelection.getImage().getScaledInstance(90, 35, Image.SCALE_SMOOTH));
        this.settingsIcon = new ImageIcon(tabSelection.getImage().getScaledInstance(140, 35, Image.SCALE_SMOOTH));
        this.optionsIcon = new ImageIcon(tabSelection.getImage().getScaledInstance(295, 35, Image.SCALE_SMOOTH));
        this.editor = new ProfileEditor(kernel);
        this.popupMenu = new ProfilePopup(kernel);
        this.skinTab = new SkinTab(kernel);
        this.languages = new JPopupMenu();
        setSize(950, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
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
        final JMenuItem en = new JMenuItem("English - United States");
        final JMenuItem es = new JMenuItem("Español - España");
        final JMenuItem ca = new JMenuItem("Català (Valencià) - País Valencià");
        final JMenuItem pt = new JMenuItem("Português - Portugal");
        final JMenuItem br = new JMenuItem("Português - Brasil");
        final JMenuItem hu = new JMenuItem("Hungarian - Magyar");
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
        hu.setIcon(flag_hu);
        hu.setFont(plain);
        hu.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kernel.getSettings().setLocale("hu-hu");
                refreshAllLocalizedStrings();
            }
        });
        languages.add(hu);
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
        br.setIcon(flag_br);
        br.setFont(plain);
        br.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                kernel.getSettings().setLocale("pt-br");
                refreshAllLocalizedStrings();
            }
        });
        languages.add(br);
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
                    Thread runThread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            if (!downloader.isDownloading() && !gameLauncher.isStarted()) {
                                Timer t = new Timer();
                                TimerTask task = new TimerTask() {
                                    @Override
                                    public void run() {
                                        if (downloader.isDownloading()) {
                                            updatePlayButton();
                                        } else if (gameLauncher.isStarted()) {
                                            if (!wasPlaying) {
                                                updatePlayButton();
                                                wasPlaying = true;
                                            }
                                        } else if (!(downloader.isDownloading() || gameLauncher.isStarted())) {
                                            updatePlayButton();
                                            Authentication a = kernel.getAuthentication();
                                            try{
                                                a.refresh();
                                            }catch(AuthenticationException ex){
                                                kernel.getConsole().printError(ex.getMessage());
                                                kernel.getGUI().showLoginPrompt(true);
                                            }
                                            wasPlaying = false;
                                            super.cancel();
                                        }
                                    }
                                };
                                t.scheduleAtFixedRate(task, 0, 500);
                                try {
                                    downloader.download();
                                    gameLauncher.launch();
                                } catch (GameLauncherException ex) {
                                    updatePlayButton();
                                    JOptionPane.showMessageDialog(null, Language.get(82), Language.get(81), JOptionPane.ERROR_MESSAGE);
                                    console.printError("Failed to perform game launch task: " + ex);
                                } catch (DownloaderException e1) {
                                    updatePlayButton();
                                    JOptionPane.showMessageDialog(null, Language.get(84), Language.get(83), JOptionPane.ERROR_MESSAGE);
                                    console.printError("Failed to perform game download task: " + e1);
                                }
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
                    showLoginPrompt(true);
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
                languages.show(e.getComponent(), e.getX(), e.getY());
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
        refreshAllLocalizedStrings();
    }
    private void refreshLocalizedStrings(){
        language.setText(Language.get(2));
        logout.setText(Language.get(86));
        news.setText(Language.get(4));
        skins.setText(Language.get(5));
        settings.setText(Language.get(6));
        options.setText(Language.get(7));
        playButton.setText(Language.get(12));
        profileButton.setToolTipText(Language.get(17));
        updatePlayButton();
    }
    private void refreshAllLocalizedStrings(){
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
    public void updatePlayButton() {
        if (downloader.isDownloading()){
            setDisable(true);
            progress.setVisible(true);
            playButton.setText(Language.get(13) + " " + kernel.getDownloader().getProgress() + "%");
            progress.setValue(kernel.getDownloader().getProgress());
            profileButton.setEnabled(false);
        } else if (gameLauncher.isStarted()){
            playButton.setText(Language.get(14));
            progress.setVisible(false);
            profileButton.setEnabled(false);
        } else {
            String buttonMain;
            if (Constants.USE_LOCAL){
                buttonMain = Language.get(79);
            } else {
                buttonMain = Language.get(12);
            }
            if (kernel.getProfiles().getSelectedProfile() != null){
                Profile p = kernel.getProfiles().getProfile(kernel.getProfiles().getSelectedProfile());
                if (p.hasVersion()){
                    switch (p.getVersionID()) {
                        case "latest-release":
                            playButton.setText("<html><center>" + buttonMain + "<br><font size='2'>Minecraft " + kernel.getVersions().getLatestRelease() + " (" + Language.get(59) + ")</font></center></html>");
                            break;
                        case "latest-snapshot":
                            playButton.setText("<html><center>" + buttonMain + "<br><font size='2'>Minecraft " + kernel.getVersions().getLatestSnapshot() + " (" + Language.get(60) + ")</font></center></html>");
                            break;
                        default:
                            playButton.setText("<html><center>" + buttonMain + "<br><font size='2'>Minecraft " + p.getVersionID() + "</font></center></html>");
                            break;
                    }
                } else if (p.getType() == ProfileType.RELEASE && kernel.getVersions().getLatestRelease() != null){
                    playButton.setText("<html><center>" + buttonMain + "<br><font size='2'>Minecraft " + kernel.getVersions().getLatestRelease() + " (" + Language.get(59) + ")</font></center></html>");
                } else if (p.getType() == ProfileType.SNAPSHOT && kernel.getVersions().getLatestSnapshot() != null){
                    playButton.setText("<html><center>" + buttonMain + "<br><font size='2'>Minecraft " + kernel.getVersions().getLatestSnapshot() + " (" + Language.get(60) + ")</font></center></html>");
                } else {
                    playButton.setText(buttonMain);
                }
            } else {
                playButton.setText(buttonMain);
            }
            setDisable(false);
            progress.setVisible(false);
            profileButton.setEnabled(true);
        }
    }
    public void showLoginPrompt(boolean show) {
        if (show) {
            kernel.getAuthentication().setSelectedUser(null);
            setDisable(true);
            setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME);
            contentPanel.removeAll();
            contentPanel.setLayout(flowLayout);
            contentPanel.add(login.getPanel());
            contentPanel.updateUI();
            login.updateExistingUsers();
        } else {
            setDisable(false);
            setTitle("Krothium Minecraft Launcher " + Constants.KERNEL_BUILD_NAME + " | " + kernel.getAuthentication().getSelectedUser().getDisplayName());
            setSelected(news);
            browser.home();
            refreshSkinPreviews();
            populateProfileList();
        }
    }
    @Override
    public void setVisible(boolean b){
        super.setVisible(b);
        updatePlayButton();
        progress.setVisible(false);
        if (b){
            Thread t1 = new Thread("Authentication thread") {
                @Override
                public void run() {
                    Authentication a = kernel.getAuthentication();
                    if (a.hasSelectedUser()){
                        try{
                            a.refresh();
                            showLoginPrompt(false);
                        }catch(AuthenticationException ex){
                            Main.this.kernel.getConsole().printError(ex.getMessage());
                            showLoginPrompt(true);
                        }
                        kernel.saveProfiles();
                    }
                    if (!a.isAuthenticated()) {
                        showLoginPrompt(true);
                    }
                }
            };
            t1.start();
            Thread t2 = new Thread("Visibility thread") {
                @Override
                public void run() {
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
                }
            };
            t2.start();
        } else {
            timer.cancel();
        }
    }
    public void setSelected(JLabel l){
        if (!componentsDisabled){
            this.contentPanel.removeAll();
            if (selected == null){
                selected = l;
            } if (l != selected) {
                selected.setIcon(null);
                selected = l;
            }
            if (l.equals(news)){
                this.contentPanel.setLayout(borderLayout);
                this.contentPanel.add(this.browser.getPanel());
                browser.resizeBrowser(contentPanel.getSize());
                news.setIcon(newsIcon);
            } else {
                this.contentPanel.setLayout(flowLayout);
                if (l.equals(settings)){
                    this.contentPanel.add(this.setting.getPanel());
                    settings.setIcon(settingsIcon);
                } else if (l.equals(options)){
                    this.contentPanel.add(this.launchOptions.getPanel());
                    options.setIcon(optionsIcon);
                } else if (l.equals(skins)){
                    this.contentPanel.add(this.skinTab.getPanel());
                    skins.setIcon(skinsIcon);
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
        popupMenu.showPopup((JComponent)e.getComponent());
    }
    public Browser getBrowser() {
        return this.browser;
    }
    public void refreshSkinPreviews() {
        skinTab.refreshPreviews();
    }
    public void populateProfileList() {
        launchOptions.populateList();
        Thread t = new Thread("Profile popup population"){
            @Override
            public void run() {
                try {
                    popupMenu.removeAll();
                    Profiles p = kernel.getProfiles();
                    Map<String, Profile> profs = p.getProfiles();
                    Set set = profs.keySet();
                    for (Object aSet : set) {
                        popupMenu.addElement(profs.get(aSet.toString()).getID());
                    }
                } catch (Exception ex){
                    kernel.getConsole().printError("Popup profile list interrupted by another thread.");
                }
            }
        };
        t.run();
    }
}
