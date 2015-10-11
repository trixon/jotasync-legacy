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

import java.awt.CardLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.UnknownHostException;
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
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import se.trixon.jota.ServerCommander;
import se.trixon.jota.ServerEventListener;
import se.trixon.jota.job.Job;
import se.trixon.jota.job.JobManager;
import se.trixon.jotaclient.Client;
import se.trixon.jotaclient.Manager;
import se.trixon.jotaclient.Options;
import se.trixon.jotaclient.Options.ClientOptionsEvent;
import se.trixon.jotaclient.ui.editor.EditorPanel;
import se.trixon.jotaclient.ui.speeddial.SpeedDialPanel;
import se.trixon.util.BundleHelper;
import se.trixon.util.CircularInt;
import se.trixon.util.SystemHelper;
import se.trixon.util.dictionary.Dict;
import se.trixon.util.icon.Pict;
import se.trixon.util.swing.SwingHelper;
import se.trixon.util.swing.dialogs.Message;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class MainFrame extends javax.swing.JFrame  {

    private static final String PROGRESS_PANEL = "progressPanel";
    private static final String DASHBOARD_PANEL = "dashboardPanel";
    private static final int STATES = 3;
    private static final int STARTABLE = 0;
    private static final int STOPPABLE = 1;
    private static final int CLOSEABLE = 2;
    private static final int ICON_SIZE_LARGE = 32;
    private static final int ICON_SIZE_SMALL = 16;
    private ActionManager mActionManager;
//    private JotaRunner mJotaRunner;
    private ProgressPanel mProgressPanel;
    private SpeedDialPanel mSpeedDialPanel;
    private CardLayout mCardLayout;
    private Job mSelectedJob;
    private boolean mSimulate;
    private final CircularInt mState = new CircularInt(0, 2);
    private ImageIcon[] mStateIcons;
    private String[] mStateTexts;
    private final Options mOptions = Options.INSTANCE;
//    private final ConnectionManager mConnectionManager = ConnectionManager.INSTANCE;
    private Client mClient;// = ConnectionManager.INSTANCE.getClient();
    private final ResourceBundle mBundle = BundleHelper.getBundle(MainFrame.class, "Bundle");
    private final LinkedList<Action> mActions = new LinkedList<>();
//    private ServerOptions mServerOptions;
    private ServerCommander mServerCommander;
    private final Manager mManager=Manager.getInstance();

    /**
     * Creates new form MainFrame
     */
    public MainFrame()  {
        initComponents();
        //mConnectionManager.connectClient();
//        mClient = mConnectionManager.getClient();
//        mClient.getJotaManager().load();

        init();
//        //SwingUtilities.invokeLater(this::showEditor);
//        //loadServerOptions();
//        enableGui(false);
//        mSpeedDialPanel.onConnectionClientDisconnect();
    }

    private void closeWindow() {
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }


    private void enableGui(boolean state) {
        boolean cronActive = false;
//        try {
//            cronActive = state && mConnectionManager.getServer().isCronActive();
//        } catch (RemoteException ex) {
//        }
//
//        mActionManager.getAction(ActionManager.CRON).putValue(Action.SELECTED_KEY, cronActive);
//
//        mActions.stream().forEach((action) -> {
//            action.setEnabled(state);
//        });
//
//        closeWindowButton.setEnabled(true);
//
//        if (state) {
//            updateWindowTitle();
//        } else {
//            setTitle("Jotasync");
//        }
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

        //JobManager.INSTANCE.addJobListener(this);
        mCardLayout = (CardLayout) (mainPanel.getLayout());
        mSpeedDialPanel = new SpeedDialPanel();
        mProgressPanel = new ProgressPanel();

        mainPanel.add(mSpeedDialPanel, DASHBOARD_PANEL);
        mainPanel.add(mProgressPanel, PROGRESS_PANEL);

        //mSpeedDialPanel.addSpeedDialListener(this);
        JobManager.INSTANCE.notifyDataListeners();

        mStateTexts = new String[STATES];
        mStateTexts[STARTABLE] = Dict.START.getString();
        mStateTexts[STOPPABLE] = Dict.STOP.getString();
        mStateTexts[CLOSEABLE] = Dict.CLOSE.getString();

        mStateIcons = new ImageIcon[STATES];
        mStateIcons[STARTABLE] = Pict.Actions.ARROW_RIGHT.get(ICON_SIZE_LARGE);
        mStateIcons[STOPPABLE] = Pict.Actions.PROCESS_STOP.get(ICON_SIZE_LARGE);
        mStateIcons[CLOSEABLE] = Pict.Actions.WINDOW_CLOSE.get(ICON_SIZE_LARGE);

        setRunState(mState.get());

//        jobEditorButton.setIcon(Pict.Actions.CONFIGURE.get(ICON_SIZE_LARGE));
        closeWindowButton.setIcon(Pict.Actions.WINDOW_CLOSE.get(ICON_SIZE_LARGE));
//        closeButton.setVisible(false);
        shutdownServerAndWindowButton.setIcon(Pict.Actions.APPLICATION_EXIT.get(ICON_SIZE_LARGE));

//        mConnectionManager.addConnectionListeners(this);
//
//        loadClientOption(ClientOptionsEvent.LOOK_AND_FEEL);
//        loadClientOption(ClientOptionsEvent.MENU_ICONS);

        updateWindowTitle();
        try {
            SwingHelper.frameStateRestore(this);
        } catch (BackingStoreException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

//        mClient.addServerEventListener(this);
//        mConnectionManager.addConnectionListeners(mSpeedDialPanel);
        JobManager.INSTANCE.addJobListener(mSpeedDialPanel);

    }

    private void loadClientOption(ClientOptionsEvent clientOptionEvent) {
        switch (clientOptionEvent) {
            case LOOK_AND_FEEL:
                if (mOptions.isForceLookAndFeel()) {
                    SwingUtilities.invokeLater(() -> {
                        try {
                            UIManager.setLookAndFeel(SwingHelper.getLookAndFeelClassName(mOptions.getLookAndFeel()));
                            SwingUtilities.updateComponentTreeUI(MainFrame.this);
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

//    private void loadConfiguration() {
//        if (!mConnectionManager.isConnected()) {
//            return;
//        }
//
//        boolean hasJob = mConnectionManager.isConnected() && JobManager.INSTANCE.hasJobs();
//        stateButton.setEnabled(hasJob);
//
//        Action cronAction = mActionManager.getAction(ActionManager.CRON);
//
//        try {
//            boolean cronActive = mConnectionManager.getServer().isCronActive();
//            cronAction.putValue(Action.SELECTED_KEY, cronActive);
//        } catch (RemoteException ex) {
//            System.err.println("mConnectionManager: " + mConnectionManager);
//        }
//
//    }

//    private ServerOptions loadServerOptions() {
//        try {
//            mServerOptions = mServerCommander.loadServerOptions();
//            return mServerOptions;
//        } catch (RemoteException ex) {
//            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
//        }
//
//        return null;
//    }

    private boolean requestJobStart(Job job) {
        mSelectedJob = job;
        mProgressPanel.setProgressString(job.getName());
        String start = Dict.START.getString();
        String simulate = "Simulate";
        String cancel = Dict.CANCEL.getString();
        String[] options = new String[]{simulate, start};
        String title = "Confirm";
        String message = String.format("<html>Start job <b>%s</b>?</html>", job.getName());

        //NotifyDescriptor d = new NotifyDescriptor(message, title, NotifyDescriptor.DEFAULT_OPTION, NotifyDescriptor.QUESTION_MESSAGE, options, start);
        //d.setAdditionalOptions(new String[]{cancel});
//        Object retval = DialogDisplayer.getDefault().notify(d);
//        mSimulate = retval == simulate;
//        if (retval == start || retval == simulate) {
//        mJotaRunner = new JotaRunner(mProgressPanel);
//        mJotaRunner.addJotaListener(this);
//        mJotaRunner.start(job, mSimulate);
//        }

        return false;
    }

    private void requestJobStop() {
//        mJotaRunner.stop();
        setRunState(CLOSEABLE);
    }

    private void setRunState(int state) {
        mState.set(state);
        stateButton.setToolTipText(mStateTexts[state]);
        stateButton.setIcon(mStateIcons[state]);
        saveButton.setVisible(state == CLOSEABLE);
    }

    private void showConnect() {
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

//            try {
//                int port = Integer.valueOf(portString);
//                disconnect();
//                connect(host, port);
//
//                if (comboBoxModel.getIndexOf(host) == -1) {
//                    comboBoxModel.addElement(host);
//                }
//                mClientOptions.setHosts(SwingHelper.comboBoxModelToString(comboBoxModel));
//            } catch (NumberFormatException e) {
//                Message.error(this, Dict.ERROR.getString(), String.format(Dict.INVALID_PORT.getString(), portString));
//            } catch (NotBoundException | MalformedURLException | RemoteException | UnknownHostException ex) {
//                Message.error(this, Dict.ERROR.getString(), ex.getLocalizedMessage());
//                mClient.setHost(currentHost);
//                mClient.setPortHost(currentPort);
//            }
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

//        if (retval == JOptionPane.OK_OPTION) {
//            editorPanel.save();
//            try {
//                mConnectionManager.getServer().createJotaManager().save();
//            } catch (IOException ex) {
//                Message.error(this, Dict.IO_ERROR_TITLE.getString(), ex.getLocalizedMessage());
//            }
//        }
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
//            mSpeedDialPanel.setServerOptions(optionsPanel.getServerOptions());
        }
    }

    private void updateWindowTitle() {
//        setTitle(String.format(mBundle.getString("windowTitle"), mConnectionManager.getHost(), mConnectionManager.getPort()));
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        toolBar = new javax.swing.JToolBar();
        stateButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        statusButton = new javax.swing.JButton();
        dirButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        connectButton = new javax.swing.JButton();
        disconnectButton = new javax.swing.JButton();
        cronToggleButton = new javax.swing.JToggleButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jobEditorButton = new javax.swing.JButton();
        optionsButton = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        closeWindowButton = new javax.swing.JButton();
        shutdownServerAndWindowButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        statusPanel = new javax.swing.JPanel();
        statusLabel = new javax.swing.JLabel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        connectMenuItem = new javax.swing.JMenuItem();
        disconnectMenuItem = new javax.swing.JMenuItem();
        startLocalServerMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        cronCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jobEditorMenuItem = new javax.swing.JMenuItem();
        optionsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        shutdownServerMenuItem = new javax.swing.JMenuItem();
        shutdownServerAndWindowMenuItem = new javax.swing.JMenuItem();
        closeMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        aboutMenuItem = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Jotasync");
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        stateButton.setFocusable(false);
        stateButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        stateButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(stateButton);

        saveButton.setText("jButton1");
        saveButton.setFocusable(false);
        saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(saveButton);

        statusButton.setText("Status");
        statusButton.setFocusable(false);
        statusButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        statusButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        statusButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                statusButtonActionPerformed(evt);
            }
        });
        toolBar.add(statusButton);

        dirButton.setText("ls /home");
        dirButton.setFocusable(false);
        dirButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        dirButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        dirButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                dirButtonActionPerformed(evt);
            }
        });
        toolBar.add(dirButton);
        toolBar.add(filler1);

        connectButton.setFocusable(false);
        connectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        connectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(connectButton);

        disconnectButton.setFocusable(false);
        disconnectButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        disconnectButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(disconnectButton);

        cronToggleButton.setFocusable(false);
        cronToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cronToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(cronToggleButton);
        toolBar.add(jSeparator3);

        jobEditorButton.setFocusable(false);
        jobEditorButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        jobEditorButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(jobEditorButton);

        optionsButton.setFocusable(false);
        optionsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        optionsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(optionsButton);
        toolBar.add(jSeparator4);

        closeWindowButton.setFocusable(false);
        closeWindowButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        closeWindowButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(closeWindowButton);

        shutdownServerAndWindowButton.setFocusable(false);
        shutdownServerAndWindowButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        shutdownServerAndWindowButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(shutdownServerAndWindowButton);

        getContentPane().add(toolBar, java.awt.BorderLayout.PAGE_START);

        mainPanel.setLayout(new java.awt.CardLayout());
        getContentPane().add(mainPanel, java.awt.BorderLayout.CENTER);

        statusPanel.setBackground(new java.awt.Color(204, 204, 204));

        statusLabel.setText("status");

        javax.swing.GroupLayout statusPanelLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusPanelLayout);
        statusPanelLayout.setHorizontalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusLabel)
                .addGap(427, 427, 427))
        );
        statusPanelLayout.setVerticalGroup(
            statusPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(statusPanelLayout.createSequentialGroup()
                .addGap(7, 7, 7)
                .addComponent(statusLabel)
                .addGap(7, 7, 7))
        );

        getContentPane().add(statusPanel, java.awt.BorderLayout.PAGE_END);

        fileMenu.setText(Dict.FILE_MENU.getString());
        fileMenu.add(connectMenuItem);
        fileMenu.add(disconnectMenuItem);
        fileMenu.add(startLocalServerMenuItem);
        fileMenu.add(jSeparator1);
        fileMenu.add(cronCheckBoxMenuItem);
        fileMenu.add(jobEditorMenuItem);
        fileMenu.add(optionsMenuItem);
        fileMenu.add(jSeparator2);
        fileMenu.add(shutdownServerMenuItem);
        fileMenu.add(shutdownServerAndWindowMenuItem);
        fileMenu.add(closeMenuItem);

        menuBar.add(fileMenu);

        helpMenu.setText(Dict.HELP.getString());

        aboutMenuItem.setText(Dict.ABOUT.getString());
        aboutMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                aboutMenuItemActionPerformed(evt);
            }
        });
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setJMenuBar(menuBar);

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void shutdownServer() {
        mClient.execute(Client.Command.SHUTDOWN);
        enableGui(false);
    }

    private void shutdownServerAndWindow() {
        try {
//            if (mConnectionManager.isConnected()) {
//                mClient.execute(Client.Command.SHUTDOWN);
//            }
        } catch (Exception e) {
            //nvm
        }
        dispatchEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
        System.exit(0);
    }

    private void aboutMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_aboutMenuItemActionPerformed
        JOptionPane.showMessageDialog(this, "Jotasync", Dict.ABOUT.getString(), JOptionPane.INFORMATION_MESSAGE);
    }//GEN-LAST:event_aboutMenuItemActionPerformed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        System.err.println("closing");
        SwingHelper.frameStateSave(this);
    }//GEN-LAST:event_formWindowClosing

    private void statusButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_statusButtonActionPerformed
        mClient.execute(Client.Command.DISPLAY_STATUS);
    }//GEN-LAST:event_statusButtonActionPerformed

    private void dirButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_dirButtonActionPerformed
        mClient.execute(Client.Command.DIR_HOME);
    }//GEN-LAST:event_dirButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem aboutMenuItem;
    private javax.swing.JMenuItem closeMenuItem;
    private javax.swing.JButton closeWindowButton;
    private javax.swing.JButton connectButton;
    private javax.swing.JMenuItem connectMenuItem;
    private javax.swing.JCheckBoxMenuItem cronCheckBoxMenuItem;
    private javax.swing.JToggleButton cronToggleButton;
    private javax.swing.JButton dirButton;
    private javax.swing.JButton disconnectButton;
    private javax.swing.JMenuItem disconnectMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JButton jobEditorButton;
    private javax.swing.JMenuItem jobEditorMenuItem;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton optionsButton;
    private javax.swing.JMenuItem optionsMenuItem;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton shutdownServerAndWindowButton;
    private javax.swing.JMenuItem shutdownServerAndWindowMenuItem;
    private javax.swing.JMenuItem shutdownServerMenuItem;
    private javax.swing.JMenuItem startLocalServerMenuItem;
    private javax.swing.JButton stateButton;
    private javax.swing.JButton statusButton;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JPanel statusPanel;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables

    private class ActionManager {

        static final String START_LOCAL_SERVER = "startLocalServer";
        static final String SHUTDOWN_SERVER = "shutdownServer";
        static final String SHUTDOWN_SERVER_AND_WINDOW = "shutdownServerAndWindow";
        static final String CLOSE_WINDOW = "closeWindow";
        static final String CONNECT = "connect";
        static final String CRON = "cron";
        static final String DISCONNECT = "disconnect";
        static final String JOB_EDITOR = "jobeditor";
        static final String JOTA_SMALL_ICON_KEY = "jota_small_icon";
        static final String OPTIONS = "options";

        private ActionManager() {
            initActions();
        }

        private Action getAction(String key) {
            return getRootPane().getActionMap().get(key);
        }

        private void initAction(Action action, String key, KeyStroke keyStroke, Pict.Actions icon, boolean addToList) {
            InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
            ActionMap actionMap = getRootPane().getActionMap();

            action.putValue(Action.ACCELERATOR_KEY, keyStroke);
            action.putValue(Action.SHORT_DESCRIPTION, action.getValue(Action.NAME));
            action.putValue("hideActionText", true);
            if (icon != null) {
                action.putValue(Action.LARGE_ICON_KEY, icon.get(ICON_SIZE_LARGE));
                action.putValue(JOTA_SMALL_ICON_KEY, icon.get(ICON_SIZE_SMALL));
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
                    showConnect();
                }
            };

            initAction(action, CONNECT, keyStroke, Pict.Actions.NETWORK_CONNECT, false);
            connectMenuItem.setAction(action);
            connectButton.setAction(action);

            //disconnect
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.DISCONNECT.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    //disconnect();
                }
            };

            initAction(action, DISCONNECT, keyStroke, Pict.Actions.NETWORK_DISCONNECT, true);
            disconnectButton.setAction(action);
            disconnectMenuItem.setAction(action);

            //cron
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK);
            action = new AbstractAction(mBundle.getString("schedule")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    //loadServerOptions();
                    boolean nextState = false;
                    try {
                        nextState = !mServerCommander.isCronActive();
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
            cronToggleButton.setAction(action);

            //jobEditor
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_J, InputEvent.CTRL_MASK);
            action = new AbstractAction(mBundle.getString("jobEditor")) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    showEditor();
                }
            };

            initAction(action, JOB_EDITOR, keyStroke, Pict.Actions.DOCUMENT_EDIT, true);
            jobEditorButton.setAction(action);
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
            optionsButton.setAction(action);
            optionsMenuItem.setAction(action);

            //closeWindow
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.CLOSE_WINDOW.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    closeWindow();
                }
            };

            initAction(action, CLOSE_WINDOW, keyStroke, Pict.Actions.WINDOW_CLOSE, false);
            closeWindowButton.setAction(action);
            closeMenuItem.setAction(action);

            //startLocalServer
            keyStroke = null;//KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK);
            String title = String.format(mBundle.getString("startLocalServer"), SystemHelper.getHostname());
            action = new AbstractAction(title) {

                @Override
                public void actionPerformed(ActionEvent e) {
                }
            };

            initAction(action, START_LOCAL_SERVER, keyStroke, Pict.Actions.SVN_COMMIT, false);
            startLocalServerMenuItem.setAction(action);

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

            //shutdownServerAndWindow
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_MASK + InputEvent.SHIFT_MASK);
            action = new AbstractAction(Dict.SHUTDOWN_SERVER_AND_WINDOW.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    shutdownServerAndWindow();
                }
            };

            initAction(action, SHUTDOWN_SERVER_AND_WINDOW, keyStroke, Pict.Actions.APPLICATION_EXIT, true);
            shutdownServerAndWindowMenuItem.setAction(action);
            shutdownServerAndWindowButton.setAction(action);

            for (Component component : toolBar.getComponents()) {
                if (component instanceof AbstractButton) {
                    ((AbstractButton) component).setHideActionText(true);
                }
            }
        }
    }
}
