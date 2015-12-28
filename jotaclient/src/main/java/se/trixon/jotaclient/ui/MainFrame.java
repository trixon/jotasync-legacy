/*
 * Copyright 2015 Patrik Karlsson.
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
package se.trixon.jotaclient.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
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
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import se.trixon.jota.Jota;
import se.trixon.jota.ProcessEvent;
import se.trixon.jota.ServerEvent;
import se.trixon.jota.ServerEventListener;
import se.trixon.jota.job.Job;
import se.trixon.jota.task.Task;
import se.trixon.jotaclient.Client;
import se.trixon.jotaclient.ConnectionListener;
import se.trixon.jotaclient.Manager;
import se.trixon.jotaclient.Options;
import se.trixon.jotaclient.Options.ClientOptionsEvent;
import se.trixon.jotaclient.ui.editor.EditorPanel;
import se.trixon.util.BundleHelper;
import se.trixon.util.Xlog;
import se.trixon.util.dictionary.Dict;
import se.trixon.util.icon.Pict;
import se.trixon.util.swing.SwingHelper;
import se.trixon.util.swing.dialogs.Message;

/**
 *
 * @author Patrik Karlsson
 */
public class MainFrame extends JFrame implements ConnectionListener, ServerEventListener {

    private ActionManager mActionManager;
    private boolean mShutdownInProgress;
    private boolean mServerShutdownRequested;
    private final Options mOptions = Options.INSTANCE;
    private Client mClient;
    private final ResourceBundle mBundle = BundleHelper.getBundle(MainFrame.class, "Bundle");
    private final LinkedList<Action> mActions = new LinkedList<>();
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
        } else {
            enableGui(false);
        }
    }

    @Override
    public void onConnectionConnect() {
        mServerShutdownRequested = false;
        mShutdownInProgress = false;
        SwingUtilities.invokeLater(() -> {
            loadConfiguration();
            enableGui(true);
        });
    }

    @Override
    public void onConnectionDisconnect() {
        SwingUtilities.invokeLater(() -> {
            enableGui(false);
            if (mShutdownInProgress && !mServerShutdownRequested) {
                Message.warning(this, "Connection lost", "Connection lost due to server shutdown");
            }
        });
    }

    @Override
    public void onProcessEvent(ProcessEvent processEvent, Job job, Task task, Object object) {
        // nvm
    }

    @Override
    public void onServerEvent(ServerEvent serverEvent) {
        Xlog.timedOut("UI got " + serverEvent);
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

        mActions.stream().forEach((action) -> {
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
        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            String key = evt.getKey();
            if (key.equalsIgnoreCase(Options.KEY_MENU_ICONS)) {
                loadClientOption(ClientOptionsEvent.MENU_ICONS);
            } else if (key.equalsIgnoreCase(Options.KEY_FORCE_LOOK_AND_FEEL)
                    || key.equalsIgnoreCase(Options.KEY_LOOK_AND_FEEL)) {
                loadClientOption(ClientOptionsEvent.LOOK_AND_FEEL);
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
            SwingHelper.frameStateRestore(this);
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
            new JLabel(Dict.HOST.getString()),
            hostComboBox,
            new JLabel(Dict.PORT.getString()),
            portTextField,};

        Object[] options = {Dict.CONNECT.getString(), Dict.CANCEL.getString()};
        int retval = JOptionPane.showOptionDialog(this,
                inputs,
                Dict.CONNECT_TO_HOST.getString(),
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
                Message.error(this, Dict.ERROR.getString(), String.format(Dict.INVALID_PORT.getString(), portString));
            } catch (NotBoundException | MalformedURLException | RemoteException | SocketException ex) {
                Message.error(this, Dict.ERROR.getString(), ex.getLocalizedMessage());
                mClient.setHost(currentHost);
                mClient.setPortHost(currentPort);
            }
        }
    }

    private void showEditor() {
        EditorPanel editorPanel = new EditorPanel();
        SwingHelper.makeWindowResizable(editorPanel);

        int retval = JOptionPane.showOptionDialog(this,
                editorPanel,
                Dict.JOB_PROPERTIES.getString(),
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
                Dict.OPTIONS.getString(),
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if (retval == JOptionPane.OK_OPTION) {
            optionsPanel.save();
        }
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
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        cronCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jobEditorMenuItem = new javax.swing.JMenuItem();
        optionsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        aboutMenuItem = new javax.swing.JMenuItem();
        jSeparator6 = new javax.swing.JPopupMenu.Separator();
        saveMenuItem = new javax.swing.JMenuItem();
        closeMenuItem = new javax.swing.JMenuItem();
        shutdownServerMenuItem = new javax.swing.JMenuItem();
        quitMenuItem = new javax.swing.JMenuItem();

        sPopupMenu.add(connectMenuItem);
        sPopupMenu.add(disconnectMenuItem);
        sPopupMenu.add(jSeparator1);
        sPopupMenu.add(cronCheckBoxMenuItem);
        sPopupMenu.add(jobEditorMenuItem);
        sPopupMenu.add(optionsMenuItem);
        sPopupMenu.add(jSeparator2);

        aboutMenuItem.setText(Dict.ABOUT.getString());
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        sPopupMenu.add(aboutMenuItem);
        sPopupMenu.add(jSeparator6);
        sPopupMenu.add(saveMenuItem);

        closeMenuItem.setText("jMenuItem1");
        sPopupMenu.add(closeMenuItem);
        sPopupMenu.add(shutdownServerMenuItem);
        sPopupMenu.add(quitMenuItem);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Jotasync");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });
        getContentPane().setLayout(new java.awt.CardLayout());

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void shutdownServer() {
        mServerShutdownRequested = true;
        mClient.execute(Client.Command.SHUTDOWN);
    }

    private void quit() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        System.exit(0);
    }

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        JOptionPane.showMessageDialog(this, Jota.getVersionInfo("jotaclient"), Dict.ABOUT.getString(), JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        SwingHelper.frameStateSave(this);
        System.exit(0);
    }//GEN-LAST:event_formWindowClosing

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
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
    private javax.swing.JMenuItem shutdownServerMenuItem;
    // End of variables declaration//GEN-END:variables

    class ActionManager {

        static final String ABOUT = "about";
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

        private ActionManager() {
            initActions();
        }

        Action getAction(String key) {
            return getRootPane().getActionMap().get(key);
        }

        private void initAction(Action action, String key, KeyStroke keyStroke, Enum iconEnum, boolean addToList) {
            InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = getRootPane().getActionMap();

            action.putValue(Action.ACCELERATOR_KEY, keyStroke);
            action.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.NAME));
            action.putValue("hideActionText", true);
            if (iconEnum != null) {
                if (iconEnum instanceof Pict.Actions) {
                    Pict.Actions icon = (Pict.Actions) iconEnum;
                    action.putValue(Action.LARGE_ICON_KEY, icon.get(UI.ICON_SIZE_LARGE));
                    action.putValue(JOTA_SMALL_ICON_KEY, icon.get(UI.ICON_SIZE_SMALL));
                } else if (iconEnum instanceof Pict.Apps) {
                    Pict.Apps icon = (Pict.Apps) iconEnum;
                    action.putValue(Action.LARGE_ICON_KEY, icon.get(UI.ICON_SIZE_LARGE));
                    action.putValue(JOTA_SMALL_ICON_KEY, icon.get(UI.ICON_SIZE_SMALL));
                }
            }

            inputMap.put(keyStroke, key);
            actionMap.put(key, action);

            if (addToList) {
                mActions.add(action);
            }
        }

        private void initActions() {
            AbstractAction action;
            KeyStroke keyStroke;

            //connect
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_O, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.CONNECT_TO_SERVER.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    try {
                        requestConnect();
                    } catch (NotBoundException ex) {
                        Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            };

            initAction(action, CONNECT, keyStroke, Pict.Actions.NETWORK_CONNECT, false);
            connectMenuItem.setAction(action);

            //disconnect
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.DISCONNECT.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mManager.disconnect();
                }
            };

            initAction(action, DISCONNECT, keyStroke, Pict.Actions.NETWORK_DISCONNECT, true);
            disconnectMenuItem.setAction(action);

            //cron
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_T, InputEvent.CTRL_MASK);
            action = new AbstractAction(mBundle.getString("schedule")) {

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

            initAction(action, CRON, keyStroke, Pict.Actions.CHRONOMETER, true);
            action.putValue(Action.SELECTED_KEY, false);
            cronCheckBoxMenuItem.setAction(action);

            //jobEditor
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK);
            action = new AbstractAction(mBundle.getString("jobEditor")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    showEditor();
                }
            };

            initAction(action, JOB_EDITOR, keyStroke, Pict.Actions.DOCUMENT_EDIT, true);
            jobEditorMenuItem.setAction(action);

            //options
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.OPTIONS.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    showOptions();
                }
            };

            initAction(action, OPTIONS, keyStroke, Pict.Actions.CONFIGURE, false);
            optionsMenuItem.setAction(action);

            //about
            keyStroke = null;
            action = new AbstractAction(Dict.ABOUT.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    JOptionPane.showMessageDialog(MainFrame.this, Jota.getVersionInfo("jotaclient"), Dict.ABOUT.getString(), JOptionPane.INFORMATION_MESSAGE);
                }
            };

            initAction(action, ABOUT, keyStroke, null, false);

            //shutdownServer
            keyStroke = null;//KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.SHUTDOWN_SERVER.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    shutdownServer();
                }
            };

            initAction(action, SHUTDOWN_SERVER, keyStroke, Pict.Actions.SVN_UPDATE, true);
            shutdownServerMenuItem.setAction(action);

            //quit
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.QUIT.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    quit();
                }
            };

            initAction(action, QUIT, keyStroke, Pict.Actions.APPLICATION_EXIT, false);
            quitMenuItem.setAction(action);

            //save tab
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.SAVE.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mTabHolder.saveTab();
                }
            };

            initAction(action, SAVE_TAB, keyStroke, Pict.Actions.DOCUMENT_SAVE, true);
            saveMenuItem.setAction(action);

            //close tab
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.TAB_CLOSE.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mTabHolder.closeTab();
                }
            };

            initAction(action, CLOSE_TAB, keyStroke, Pict.Actions.WINDOW_CLOSE, true);
            closeMenuItem.setAction(action);

            for (Component component : sPopupMenu.getComponents()) {
                if (component instanceof AbstractButton) {
                    ((AbstractButton) component).setToolTipText(null);
                }
            }
        }
    }
}
