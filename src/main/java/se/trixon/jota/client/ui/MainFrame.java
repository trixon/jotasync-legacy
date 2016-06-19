/* 
 * Copyright 2016 Patrik Karlsson.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package se.trixon.jota.client.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIDefaults;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.jota.client.Client;
import se.trixon.jota.client.ClientOptions;
import se.trixon.jota.client.ClientOptions.ClientOptionsEvent;
import se.trixon.jota.client.ConnectionListener;
import se.trixon.jota.client.Main;
import se.trixon.jota.client.Manager;
import se.trixon.jota.client.ui.editor.EditorPanel;
import se.trixon.jota.server.JobValidator;
import se.trixon.jota.shared.Jota;
import se.trixon.jota.shared.ProcessEvent;
import se.trixon.jota.shared.ServerEvent;
import se.trixon.jota.shared.ServerEventListener;
import se.trixon.jota.shared.job.Job;
import se.trixon.jota.shared.task.Task;
import se.trixon.almond.util.BundleHelper;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icon.Pict;
import se.trixon.almond.util.icons.IconColor;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.almond.util.swing.dialogs.HtmlPanel;
import se.trixon.almond.util.swing.dialogs.Message;

/**
 *
 * @author Patrik Karlsson
 */
public class MainFrame extends JFrame implements ConnectionListener, ServerEventListener {

    private ActionManager mActionManager;
    private boolean mShutdownInProgress;
    private boolean mServerShutdownRequested;
    private final ClientOptions mOptions = ClientOptions.INSTANCE;
    private final Client mClient;
    private final ResourceBundle mBundle = BundleHelper.getBundle(MainFrame.class, "Bundle");
    private final LinkedList<JotaAction> mServerActions = new LinkedList<>();
    private final LinkedList<JotaAction> mAllActions = new LinkedList<>();
    private final Manager mManager = Manager.getInstance();
    private TabHolder mTabHolder;

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();
        init();
        mClient = mManager.getClient();
        loadConfiguration();

        if (mManager.isConnected()) {
            enableGui(true);
            mTabHolder.getSpeedDialPanel().onConnectionConnect();
            updateStartServerState();
        } else {
            enableGui(false);
        }

        if (mManager.isConnected()) {
            SwingUtilities.invokeLater(() -> {
                //showEditor(-1);
            });
        }
    }

    @Override
    public void onConnectionConnect() {
        mServerShutdownRequested = false;
        mShutdownInProgress = false;
        SwingUtilities.invokeLater(() -> {
            loadConfiguration();
            enableGui(true);
            updateStartServerState();
        });
    }

    @Override
    public void onConnectionDisconnect() {
        SwingUtilities.invokeLater(() -> {
            enableGui(false);
            if (mShutdownInProgress && !mServerShutdownRequested) {
                Message.warning(this, Dict.CONNECTION_LOST.toString(), Dict.CONNECTION_LOST_SERVER_SHUTDOWN.toString());
            }
            mActionManager.getAction(ActionManager.START_SERVER).setEnabled(true);
        });
    }

    @Override
    public void onProcessEvent(ProcessEvent processEvent, Job job, Task task, Object object) {
        // nvm
    }

    @Override
    public void onServerEvent(ServerEvent serverEvent) {
        switch (serverEvent) {
            case CRON_CHANGED:
                try {
                    boolean cronActive = mManager.getServerCommander().isCronActive();
                    mActionManager.getAction(ActionManager.CRON).putValue(Action.SELECTED_KEY, cronActive);
                } catch (RemoteException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                break;

            case JOTA_CHANGED:
                loadConfiguration();
                break;

            case SHUTDOWN:
                mShutdownInProgress = true;
                mManager.disconnect();
                break;

            default:
                throw new AssertionError();
        }
    }

    public boolean requestStartJob(Job job) {
        try {
            JobValidator validator = mManager.getServerCommander().validate(job);
            if (validator.isValid()) {
                Object[] options = {Dict.RUN.toString(), Dict.DRY_RUN.toString(), Dict.CANCEL.toString()};
                HtmlPanel htmlPanel = new HtmlPanel(job.getSummaryAsHtml());
                SwingHelper.makeWindowResizable(htmlPanel);

                int result = JOptionPane.showOptionDialog(this,
                        htmlPanel,
                        Dict.RUN.toString(),
                        JOptionPane.YES_NO_CANCEL_OPTION,
                        JOptionPane.PLAIN_MESSAGE,
                        null,
                        options,
                        options[1]);

                if (result > -1 && result < 2) {
                    boolean dryRun = result == 1;
                    mManager.getServerCommander().startJob(job, dryRun);
                }
            } else {
                Message.html(this, Dict.ERROR_VALIDATION.toString(), validator.getSummaryAsHtml());
            }
        } catch (RemoteException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    ActionManager getActionManager() {
        return mActionManager;
    }

    private void enableGui(boolean state) {
        boolean cronActive = false;

        try {
            cronActive = state && mManager.getServerCommander().isCronActive();
        } catch (RemoteException ex) {
        }

        mActionManager.getAction(ActionManager.CRON).putValue(Action.SELECTED_KEY, cronActive);

        mServerActions.stream().forEach((action) -> {
            action.setEnabled(state);
        });

        mActionManager.getAction(ActionManager.CLOSE_TAB).setEnabled(false);
        mActionManager.getAction(ActionManager.SAVE_TAB).setEnabled(false);

        if (state) {
            updateWindowTitle();
        } else {
            setTitle("Jotasync");
        }
    }

    private void init() {
        String fileName = String.format("/%s/sync-256px.png", getClass().getPackage().getName().replace(".", "/"));
        ImageIcon imageIcon = new ImageIcon(getClass().getResource(fileName));
        setIconImage(imageIcon.getImage());

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            String key = evt.getKey();
            if (key.equalsIgnoreCase(ClientOptions.KEY_MENU_ICONS)) {
                loadClientOption(ClientOptionsEvent.MENU_ICONS);
            } else if (key.equalsIgnoreCase(ClientOptions.KEY_FORCE_LOOK_AND_FEEL)
                    || key.equalsIgnoreCase(ClientOptions.KEY_LOOK_AND_FEEL)) {
                loadClientOption(ClientOptionsEvent.LOOK_AND_FEEL);
            } else if (key.equalsIgnoreCase(ClientOptions.KEY_ICON_THEME)) {
                mAllActions.stream().forEach((jotaAction) -> {
                    jotaAction.updateIcon();
                });
            }
        });

        mActionManager = new ActionManager();
        mActionManager.initActions();

        mTabHolder = new TabHolder();
        add(mTabHolder);
        mTabHolder.initActions();
        mManager.addConnectionListeners(this);

        loadClientOption(ClientOptionsEvent.LOOK_AND_FEEL);
        loadClientOption(ClientOptionsEvent.MENU_ICONS);

        updateWindowTitle();

        try {
            SwingHelper.frameStateRestore(this, 800, 600);
        } catch (BackingStoreException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static JPopupMenu getPopupMenu() {
        return sPopupMenu;
    }

    private void loadClientOption(ClientOptionsEvent clientOptionEvent) {
        switch (clientOptionEvent) {
            case LOOK_AND_FEEL:
                if (mOptions.isForceLookAndFeel()) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            UIManager.setLookAndFeel(SwingHelper.getLookAndFeelClassName(mOptions.getLookAndFeel()));
                            SwingUtilities.updateComponentTreeUI(MainFrame.this);
                            SwingUtilities.updateComponentTreeUI(sPopupMenu);

                            if (mOptions.getLookAndFeel().equalsIgnoreCase("Darcula")) {
                                int iconSize = 32;
                                UIDefaults uiDefaults = UIManager.getLookAndFeelDefaults();
                                uiDefaults.put("OptionPane.informationIcon", MaterialIcon.Action.INFO_OUTLINE.get(iconSize, IconColor.WHITE));
                                uiDefaults.put("OptionPane.errorIcon", MaterialIcon.Alert.ERROR_OUTLINE.get(iconSize, IconColor.WHITE));
                                uiDefaults.put("OptionPane.questionIcon", MaterialIcon.Action.HELP_OUTLINE.get(iconSize, IconColor.WHITE));
                                uiDefaults.put("OptionPane.warningIcon", MaterialIcon.Alert.WARNING.get(iconSize, IconColor.WHITE));
                            }
                        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                            //Xlog.timedErr(ex.getMessage());
                        }
                    });
                }
                break;

            case MENU_ICONS:
                ActionMap actionMap = getRootPane().getActionMap();
                for (Object allKey : actionMap.allKeys()) {
                    Action action = actionMap.get(allKey);
                    Icon icon = null;
                    if (mOptions.isDisplayMenuIcons()) {
                        icon = (Icon) action.getValue(ActionManager.JOTA_SMALL_ICON_KEY);
                    }
                    action.putValue(Action.SMALL_ICON, icon);
                }
                break;

            default:
                throw new AssertionError();
        }

    }

    private void loadConfiguration() {
        if (!mManager.isConnected()) {
            return;
        }

        boolean hasJob = mManager.isConnected() && mManager.hasJobs();

        Action cronAction = mActionManager.getAction(ActionManager.CRON);

        try {
            boolean cronActive = mManager.getServerCommander().isCronActive();
            cronAction.putValue(Action.SELECTED_KEY, cronActive);
        } catch (RemoteException ex) {
            System.err.println("mManager: " + mManager);
        }

    }

    private void requestConnect() throws NotBoundException {
        String[] hosts = mOptions.getHosts().split(";");
        Arrays.sort(hosts);
        DefaultComboBoxModel comboBoxModel = new DefaultComboBoxModel(hosts);
        JComboBox hostComboBox = new JComboBox(comboBoxModel);
        hostComboBox.setEditable(true);

        hostComboBox.setSelectedItem(mClient.getHost());
        JTextField portTextField = new JTextField(String.valueOf(mClient.getPortHost()));
        final JComponent[] inputs = new JComponent[]{
            new JLabel(Dict.HOST.toString()),
            hostComboBox,
            new JLabel(Dict.PORT.toString()),
            portTextField,};

        Object[] options = {Dict.CONNECT.toString(), Dict.CANCEL.toString()};
        int retval = JOptionPane.showOptionDialog(this,
                inputs,
                Dict.CONNECT_TO_HOST.toString(),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                options[0]);

        if (retval == 0) {
            String currentHost = mClient.getHost();
            int currentPort = mClient.getPortHost();
            String host = (String) hostComboBox.getSelectedItem();
            String portString = portTextField.getText();

            try {
                int port = Integer.valueOf(portString);
                mManager.disconnect();
                mManager.connect(host, port);

                if (comboBoxModel.getIndexOf(host) == -1) {
                    comboBoxModel.addElement(host);
                }
                mOptions.setHosts(SwingHelper.comboBoxModelToString(comboBoxModel));
            } catch (NumberFormatException e) {
                Message.error(this, Dict.ERROR.toString(), String.format(Dict.INVALID_PORT.toString(), portString));
            } catch (NotBoundException | MalformedURLException | RemoteException | SocketException ex) {
                Message.error(this, Dict.ERROR.toString(), ex.getLocalizedMessage());
                mClient.setHost(currentHost);
                mClient.setPortHost(currentPort);
            }
        }
    }

    void showEditor(long jobId, boolean openJob) {
        EditorPanel editorPanel = new EditorPanel(jobId, openJob);
        SwingHelper.makeWindowResizable(editorPanel);

        int retval = JOptionPane.showOptionDialog(this,
                editorPanel,
                mBundle.getString("jobEditor"),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if (retval == JOptionPane.OK_OPTION) {
            editorPanel.save();
            try {
                mManager.getServerCommander().saveJota();
            } catch (RemoteException ex) {
                Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void showOptions() {
        OptionsPanel optionsPanel = new OptionsPanel();
        SwingHelper.makeWindowResizable(optionsPanel);

        int retval = JOptionPane.showOptionDialog(this,
                optionsPanel,
                Dict.OPTIONS.toString(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if (retval == JOptionPane.OK_OPTION) {
            optionsPanel.save();
        }
    }

    private void updateStartServerState() {
        boolean connectedToAutstartServer = mManager.isConnected()
                && mClient.getHost().equalsIgnoreCase(SystemHelper.getHostname())
                && mClient.getPortHost() == mOptions.getAutostartServerPort();
        mActionManager.getAction(ActionManager.START_SERVER).setEnabled(!connectedToAutstartServer);
    }

    private void updateWindowTitle() {
        setTitle(String.format(mBundle.getString("windowTitle"), mManager.getClient().getHost(), mManager.getClient().getPortHost()));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sPopupMenu = new javax.swing.JPopupMenu();
        connectMenuItem = new javax.swing.JMenuItem();
        disconnectMenuItem = new javax.swing.JMenuItem();
        serverMenu = new javax.swing.JMenu();
        startServerMenuItem = new javax.swing.JMenuItem();
        shutdownServerMenuItem = new javax.swing.JMenuItem();
        shutdownServerQuitMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        cronCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jobEditorMenuItem = new javax.swing.JMenuItem();
        optionsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        aboutRsyncMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        saveMenuItem = new javax.swing.JMenuItem();
        closeMenuItem = new javax.swing.JMenuItem();
        quitMenuItem = new javax.swing.JMenuItem();

        sPopupMenu.add(connectMenuItem);
        sPopupMenu.add(disconnectMenuItem);

        serverMenu.setText(Dict.SERVER.toString());

        startServerMenuItem.setText("jMenuItem1");
        serverMenu.add(startServerMenuItem);
        serverMenu.add(shutdownServerMenuItem);
        serverMenu.add(shutdownServerQuitMenuItem);

        sPopupMenu.add(serverMenu);
        sPopupMenu.add(jSeparator1);
        sPopupMenu.add(cronCheckBoxMenuItem);
        sPopupMenu.add(jobEditorMenuItem);
        sPopupMenu.add(optionsMenuItem);
        sPopupMenu.add(jSeparator2);

        aboutRsyncMenuItem.setText("jMenuItem1");
        sPopupMenu.add(aboutRsyncMenuItem);
        sPopupMenu.add(aboutMenuItem);
        sPopupMenu.add(jSeparator6);
        sPopupMenu.add(saveMenuItem);

        closeMenuItem.setText("jMenuItem1");
        sPopupMenu.add(closeMenuItem);
        sPopupMenu.add(quitMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Jotasync");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });
        getContentPane().setLayout(new java.awt.CardLayout());

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void serverShutdown() {
        mServerShutdownRequested = true;
        mClient.execute(Client.Command.SHUTDOWN);
    }

    private void quit() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        SwingHelper.frameStateSave(this);
    }//GEN-LAST:event_formWindowClosing

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        System.exit(0);
    }//GEN-LAST:event_formWindowClosed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem aboutRsyncMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JMenuItem connectMenuItem;
    private javax.swing.JCheckBoxMenuItem cronCheckBoxMenuItem;
    private javax.swing.JMenuItem disconnectMenuItem;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JPopupMenu.Separator jSeparator6;
    private javax.swing.JMenuItem jobEditorMenuItem;
    private javax.swing.JMenuItem optionsMenuItem;
    private javax.swing.JMenuItem quitMenuItem;
    private static javax.swing.JPopupMenu sPopupMenu;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenu serverMenu;
    private javax.swing.JMenuItem shutdownServerMenuItem;
    private javax.swing.JMenuItem shutdownServerQuitMenuItem;
    private javax.swing.JMenuItem startServerMenuItem;
    // End of variables declaration//GEN-END:variables

    class ActionManager {

        static final String ABOUT = "about";
        static final String ABOUT_S = "about_s";
        static final String CLOSE_TAB = "closeTab";
        static final String CONNECT = "connect";
        static final String CRON = "cron";
        static final String DISCONNECT = "disconnect";
        static final String JOB_EDITOR = "jobeditor";
        static final String JOTA_SMALL_ICON_KEY = "jota_small_icon";
        static final String OPTIONS = "options";
        static final String QUIT = "shutdownServerAndWindow";
        static final String SAVE_TAB = "saveTab";
        static final String SHUTDOWN_SERVER = "shutdownServer";
        static final String SHUTDOWN_SERVER_QUIT = "shutdownServerAndQuit";
        static final String START_SERVER = "startServer";

        private ActionManager() {
            initActions();
        }

        Action getAction(String key) {
            return getRootPane().getActionMap().get(key);
        }

        private void initAction(JotaAction action, String key, KeyStroke keyStroke, Enum iconEnum, boolean serverAction) {
            InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = getRootPane().getActionMap();

            action.putValue(Action.ACCELERATOR_KEY, keyStroke);
            action.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.NAME));
            action.putValue("hideActionText", true);
            action.setIconEnum(iconEnum);
            action.updateIcon();

            inputMap.put(keyStroke, key);
            actionMap.put(key, action);

            if (serverAction) {
                mServerActions.add(action);
            }

            mAllActions.add(action);
        }

        private void initActions() {
            JotaAction action;
            KeyStroke keyStroke;
            int commandMask = SystemHelper.getCommandMask();

            //connect
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_O, commandMask);
            action = new JotaAction(Dict.CONNECT_TO_SERVER.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        requestConnect();
                    } catch (NotBoundException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            initAction(action, CONNECT, keyStroke, MaterialIcon.Communication.CALL_MADE, false);
            connectMenuItem.setAction(action);

            //disconnect
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, commandMask);
            action = new JotaAction(Dict.DISCONNECT.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mManager.disconnect();
                }
            };

            initAction(action, DISCONNECT, keyStroke, MaterialIcon.Communication.CALL_RECEIVED, true);
            disconnectMenuItem.setAction(action);

            //cron
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_T, commandMask);
            action = new JotaAction(mBundle.getString("schedule")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    boolean nextState = false;
                    try {
                        nextState = !mManager.getServerCommander().isCronActive();
                    } catch (RemoteException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    putValue(Action.SELECTED_KEY, !nextState);

                    Client.Command command = Client.Command.STOP_CRON;
                    if (nextState) {
                        command = Client.Command.START_CRON;
                    }
                    mClient.execute(command);
                }
            };

            initAction(action, CRON, keyStroke, MaterialIcon.Action.SCHEDULE, true);
            action.putValue(Action.SELECTED_KEY, false);
            cronCheckBoxMenuItem.setAction(action);

            //jobEditor
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_J, commandMask);
            action = new JotaAction(mBundle.getString("jobEditor")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    showEditor(-1, false);
                }
            };

            initAction(action, JOB_EDITOR, keyStroke, MaterialIcon.Editor.MODE_EDIT, true);
            jobEditorMenuItem.setAction(action);

            //options
            int optionsKey = SystemUtils.IS_OS_MAC ? KeyEvent.VK_COMMA : KeyEvent.VK_P;
            keyStroke = KeyStroke.getKeyStroke(optionsKey, commandMask);
            action = new JotaAction(Dict.OPTIONS.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    showOptions();
                }
            };

            initAction(action, OPTIONS, keyStroke, MaterialIcon.Action.SETTINGS, false);
            optionsMenuItem.setAction(action);

            //about
            keyStroke = null;
            action = new JotaAction(Dict.ABOUT.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    String versionInfo = String.format(Jota.getBundle().getString("version_info"), SystemHelper.getJarVersion(Main.class));
                    Message.information(MainFrame.this, Dict.ABOUT.toString(), versionInfo);
                }
            };

            initAction(action, ABOUT, keyStroke, null, false);
            aboutMenuItem.setAction(action);

            //about rsync
            keyStroke = null;
            action = new JotaAction(String.format(Dict.ABOUT_S.toString(), "rsync")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        String aboutRsync = mManager.getServerCommander().getAboutRsync();
                        Message.information(MainFrame.this, String.format(Dict.ABOUT_S.toString(), "rsync"), aboutRsync);
                    } catch (RemoteException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            initAction(action, ABOUT_S, keyStroke, null, true);
            aboutRsyncMenuItem.setAction(action);

            //start Server
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_O, commandMask | InputEvent.SHIFT_MASK);
            action = new JotaAction(Dict.START.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        if (mClient.serverStart()) {
                            mManager.connect(SystemHelper.getHostname(), mOptions.getAutostartServerPort());
                        }
                    } catch (URISyntaxException | IOException | NotBoundException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            initAction(action, START_SERVER, keyStroke, null, false);
            startServerMenuItem.setAction(action);

            //shutdown Server
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, commandMask | InputEvent.SHIFT_MASK);
            action = new JotaAction(Dict.SHUTDOWN.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    serverShutdown();
                }
            };

            initAction(action, SHUTDOWN_SERVER, keyStroke, null, true);
            shutdownServerMenuItem.setAction(action);

            //shutdown server and quit
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, commandMask + InputEvent.SHIFT_MASK);
            action = new JotaAction(Dict.SHUTDOWN_AND_QUIT.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    serverShutdown();
                    quit();
                }
            };

            initAction(action, SHUTDOWN_SERVER_QUIT, keyStroke, null, true);
            shutdownServerQuitMenuItem.setAction(action);

            //quit
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, commandMask);
            action = new JotaAction(Dict.QUIT.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    quit();
                }
            };

            initAction(action, QUIT, keyStroke, Pict.Actions.APPLICATION_EXIT, false);
            quitMenuItem.setAction(action);

            //save tab
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, commandMask);
            action = new JotaAction(Dict.SAVE.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mTabHolder.saveTab();
                }
            };

            initAction(action, SAVE_TAB, keyStroke, MaterialIcon.Content.SAVE, true);
            saveMenuItem.setAction(action);

            //close tab
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_W, commandMask);
            action = new JotaAction(Dict.TAB_CLOSE.toString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mTabHolder.closeTab();
                }
            };

            initAction(action, CLOSE_TAB, keyStroke, MaterialIcon.Navigation.CLOSE, true);
            closeMenuItem.setAction(action);

            for (Component component : sPopupMenu.getComponents()) {
                if (component instanceof AbstractButton) {
                    ((AbstractButton) component).setToolTipText(null);
                }
            }

            for (Component component : serverMenu.getMenuComponents()) {
                if (component instanceof AbstractButton) {
                    ((AbstractButton) component).setToolTipText(null);
                }
            }
        }
    }
}
