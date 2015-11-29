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
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import se.trixon.jota.Jota;
import se.trixon.jota.ProcessEvent;
import se.trixon.jota.ProcessState;
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
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class MainFrame extends JFrame implements ConnectionListener, ServerEventListener, SpeedDialListener {

    private static final String PROGRESS_PANEL = "progressPanel";
    private static final String DASHBOARD_PANEL = "dashboardPanel";
    private static final int ICON_SIZE_LARGE = 32;
    private static final int ICON_SIZE_SMALL = 16;
    private ActionManager mActionManager;
    private ProgressPanel mProgressPanel;
    private boolean mShutdownInProgress;
    private boolean mServerShutdownRequested;
    private SpeedDialPanel mSpeedDialPanel;
    private CardLayout mCardLayout;
    private Job mSelectedJob;
    private boolean mSimulate;
    private JButton[] mRunStateButtons;
    private ProcessState mProcessState;
    private final Options mOptions = Options.INSTANCE;
    private Client mClient;
    private final ResourceBundle mBundle = BundleHelper.getBundle(MainFrame.class, "Bundle");
    private final LinkedList<Action> mActions = new LinkedList<>();
    private final Manager mManager = Manager.getInstance();

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
            mSpeedDialPanel.onConnectionConnect();
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
        if (processEvent == ProcessEvent.STARTED) {
            setProcessState(ProcessState.CANCELABLE);
        } else if (processEvent == ProcessEvent.CANCELED || processEvent == ProcessEvent.FINISHED) {
            setProcessState(ProcessState.CLOSEABLE);
        }
        Xlog.timedOut("onProcessEvent");
        Xlog.timedOut(processEvent.name());
        Xlog.timedOut(job.getName());
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

    @Override
    public void onSpeedDialButtonClicked(SpeedDialButton speedDialButton) {
        requestJobStart(speedDialButton.getJob());
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

        mCardLayout = (CardLayout) (mainPanel.getLayout());
        mSpeedDialPanel = new SpeedDialPanel();
        mProgressPanel = new ProgressPanel();

        mainPanel.add(mSpeedDialPanel, DASHBOARD_PANEL);
        mainPanel.add(mProgressPane, PROGRESS_PANEL);
        mProgressPane.add(mProgressPanel, "images");
        ProgressPanel mProgressPanel2 = new ProgressPanel();

        mProgressPane.add(mProgressPanel2, "sounds");

        mSpeedDialPanel.addSpeedDialListener(this);

        mRunStateButtons = new JButton[]{startButton, cancelButton, closeButton};

        setProcessState(ProcessState.STARTABLE);

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

    private void loadConfiguration() {
        if (!mManager.isConnected()) {
            return;
        }

        boolean hasJob = mManager.isConnected() && mManager.hasJobs();
        mRunStateButtons[ProcessState.STARTABLE.ordinal()].getAction().setEnabled(hasJob);

        Action cronAction = mActionManager.getAction(ActionManager.CRON);

        try {
            boolean cronActive = mManager.getServerCommander().isCronActive();
            cronAction.putValue(Action.SELECTED_KEY, cronActive);
        } catch (RemoteException ex) {
            System.err.println("mManager: " + mManager);
        }

    }

    private boolean requestJobStart(Job job) {
        Xlog.timedOut("requestJobStart() " + job.toString());
        mSelectedJob = job;
        mProgressPanel.setProgressString(job.getName());
        String start = Dict.START.getString();
        String simulate = "Simulate";
        String cancel = Dict.CANCEL.getString();
        String[] options = new String[]{simulate, start};
        String title = "Confirm";
        String message = String.format("<html>Start job <b>%s</b>?</html>", job.getName());

        try {
            mManager.getServerCommander().startJob(job);
        } catch (RemoteException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

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

    private void requestJobCancel() {
        mSelectedJob = mSpeedDialPanel.getSelectedJob(); //TODO Move this to tabbed pane.
        try {
            mManager.getServerCommander().cancelJob(mSelectedJob);
        } catch (RemoteException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void setProcessState(ProcessState processState) {
        for (JButton button : mRunStateButtons) {
            button.getAction().setEnabled(false);
            button.setVisible(false);
        }

        mRunStateButtons[processState.ordinal()].setVisible(true);
        mRunStateButtons[processState.ordinal()].getAction().setEnabled(true);
        mProcessState = processState;
        saveButton.setVisible(processState == ProcessState.CLOSEABLE);
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

        mProgressPane = new se.trixon.jotaclient.ui.ProgressPane();
        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        toolBar = new javax.swing.JToolBar();
        launcherToggleButton = new javax.swing.JToggleButton();
        logToggleButton = new javax.swing.JToggleButton();
        jSeparator5 = new javax.swing.JToolBar.Separator();
        startButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        saveButton = new javax.swing.JButton();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(32767, 0));
        connectButton = new javax.swing.JButton();
        disconnectButton = new javax.swing.JButton();
        cronToggleButton = new javax.swing.JToggleButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        jobEditorButton = new javax.swing.JButton();
        optionsButton = new javax.swing.JButton();
        jSeparator4 = new javax.swing.JToolBar.Separator();
        shutdownServerButton = new javax.swing.JButton();
        quitButton = new javax.swing.JButton();
        mainPanel = new javax.swing.JPanel();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        connectMenuItem = new javax.swing.JMenuItem();
        disconnectMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        cronCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jobEditorMenuItem = new javax.swing.JMenuItem();
        optionsMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        shutdownServerMenuItem = new javax.swing.JMenuItem();
        quitMenuItem = new javax.swing.JMenuItem();
        viewMenu = new javax.swing.JMenu();
        launcherRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
        logRadioButtonMenuItem = new javax.swing.JRadioButtonMenuItem();
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

        buttonGroup1.add(launcherToggleButton);
        launcherToggleButton.setFocusable(false);
        launcherToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        launcherToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(launcherToggleButton);

        buttonGroup1.add(logToggleButton);
        logToggleButton.setFocusable(false);
        logToggleButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        logToggleButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(logToggleButton);
        toolBar.add(jSeparator5);

        startButton.setFocusable(false);
        startButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        startButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(startButton);

        cancelButton.setFocusable(false);
        cancelButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cancelButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(cancelButton);

        closeButton.setFocusable(false);
        closeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        closeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(closeButton);

        saveButton.setFocusable(false);
        saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(saveButton);
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

        shutdownServerButton.setFocusable(false);
        shutdownServerButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        shutdownServerButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(shutdownServerButton);

        quitButton.setFocusable(false);
        quitButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        quitButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(quitButton);

        mainPanel.setLayout(new java.awt.CardLayout());

        fileMenu.setText(Dict.FILE_MENU.getString());
        fileMenu.add(connectMenuItem);
        fileMenu.add(disconnectMenuItem);
        fileMenu.add(jSeparator1);
        fileMenu.add(cronCheckBoxMenuItem);
        fileMenu.add(jobEditorMenuItem);
        fileMenu.add(optionsMenuItem);
        fileMenu.add(jSeparator2);
        fileMenu.add(shutdownServerMenuItem);
        fileMenu.add(quitMenuItem);

        menuBar.add(fileMenu);

        viewMenu.setText(Dict.VIEW.getString());

        buttonGroup2.add(launcherRadioButtonMenuItem);
        launcherRadioButtonMenuItem.setSelected(true);
        viewMenu.add(launcherRadioButtonMenuItem);

        buttonGroup2.add(logRadioButtonMenuItem);
        viewMenu.add(logRadioButtonMenuItem);

        menuBar.add(viewMenu);

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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(toolBar, javax.swing.GroupLayout.DEFAULT_SIZE, 450, Short.MAX_VALUE)
            .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(toolBar, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0)
                .addComponent(mainPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
                .addGap(0, 0, 0))
        );

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
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton connectButton;
    private javax.swing.JMenuItem connectMenuItem;
    private javax.swing.JCheckBoxMenuItem cronCheckBoxMenuItem;
    private javax.swing.JToggleButton cronToggleButton;
    private javax.swing.JButton disconnectButton;
    private javax.swing.JMenuItem disconnectMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.Box.Filler filler1;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JToolBar.Separator jSeparator4;
    private javax.swing.JToolBar.Separator jSeparator5;
    private javax.swing.JButton jobEditorButton;
    private javax.swing.JMenuItem jobEditorMenuItem;
    private javax.swing.JRadioButtonMenuItem launcherRadioButtonMenuItem;
    private javax.swing.JToggleButton launcherToggleButton;
    private javax.swing.JRadioButtonMenuItem logRadioButtonMenuItem;
    private javax.swing.JToggleButton logToggleButton;
    private se.trixon.jotaclient.ui.ProgressPane mProgressPane;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JButton optionsButton;
    private javax.swing.JMenuItem optionsMenuItem;
    private javax.swing.JButton quitButton;
    private javax.swing.JMenuItem quitMenuItem;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton shutdownServerButton;
    private javax.swing.JMenuItem shutdownServerMenuItem;
    private javax.swing.JButton startButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JMenu viewMenu;
    // End of variables declaration//GEN-END:variables

    private class ActionManager {

        static final String CLOSE = "close";
        static final String CONNECT = "connect";
        static final String CRON = "cron";
        static final String DISCONNECT = "disconnect";
        static final String JOB_EDITOR = "jobeditor";
        static final String JOTA_SMALL_ICON_KEY = "jota_small_icon";
        static final String OPTIONS = "options";
        static final String QUIT = "shutdownServerAndWindow";
        static final String SHUTDOWN_SERVER = "shutdownServer";
        static final String SAVE = "start";
        static final String START = "save";
        static final String CANCEL = "cancel";
        static final String VIEW_LAUNCHER = "viewHome";
        static final String VIEW_LOG = "viewLog";

        private ActionManager() {
            initActions();
        }

        private Action getAction(String key) {
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
                    action.putValue(Action.LARGE_ICON_KEY, icon.get(ICON_SIZE_LARGE));
                    action.putValue(JOTA_SMALL_ICON_KEY, icon.get(ICON_SIZE_SMALL));
                } else if (iconEnum instanceof Pict.Apps) {
                    Pict.Apps icon = (Pict.Apps) iconEnum;
                    action.putValue(Action.LARGE_ICON_KEY, icon.get(ICON_SIZE_LARGE));
                    action.putValue(JOTA_SMALL_ICON_KEY, icon.get(ICON_SIZE_SMALL));
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

            //view launcher
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_1, InputEvent.CTRL_MASK);
            action = new AbstractAction("Launcher") {

                @Override
                public void actionPerformed(ActionEvent e) {
                    launcherToggleButton.setSelected(true);
                    launcherRadioButtonMenuItem.setSelected(true);
                    mCardLayout.show(mainPanel, DASHBOARD_PANEL);
                }
            };

            initAction(action, VIEW_LAUNCHER, keyStroke, Pict.Actions.GO_HOME, true);
            launcherRadioButtonMenuItem.setAction(action);
            launcherToggleButton.setAction(action);

            //view log
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_2, InputEvent.CTRL_MASK);
            action = new AbstractAction("Log") {

                @Override
                public void actionPerformed(ActionEvent e) {
                    logToggleButton.setSelected(true);
                    logRadioButtonMenuItem.setSelected(true);
                    mCardLayout.show(mainPanel, PROGRESS_PANEL);
                }
            };

            initAction(action, VIEW_LOG, keyStroke, Pict.Apps.UTILITIES_LOG_VIEWER, true);
            logRadioButtonMenuItem.setAction(action);
            logToggleButton.setAction(action);

            //start
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F6, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.START.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    requestJobStart(mSpeedDialPanel.getSelectedJob());
                }
            };

            initAction(action, START, keyStroke, Pict.Actions.MEDIA_PLAYBACK_START, true);
            startButton.setAction(action);

            //cancel
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F7, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.CANCEL.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    requestJobCancel();
                }
            };

            initAction(action, CANCEL, keyStroke, Pict.Actions.MEDIA_PLAYBACK_STOP, true);
            cancelButton.setAction(action);

            //save
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.SAVE.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                }
            };

            initAction(action, SAVE, keyStroke, Pict.Actions.DOCUMENT_SAVE, true);
            saveButton.setAction(action);

            //close
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_W, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.CLOSE.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    setProcessState(ProcessState.STARTABLE);
                }
            };

            initAction(action, CLOSE, keyStroke, Pict.Actions.WINDOW_CLOSE, true);
            closeButton.setAction(action);

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
            connectButton.setAction(action);

            //disconnect
            keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_D, InputEvent.CTRL_MASK);
            action = new AbstractAction(Dict.DISCONNECT.getString()) {

                @Override
                public void actionPerformed(ActionEvent e) {
                    mManager.disconnect();
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
            shutdownServerButton.setAction(action);

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
            quitButton.setAction(action);

            for (Component component : toolBar.getComponents()) {
                if (component instanceof AbstractButton) {
                    ((AbstractButton) component).setHideActionText(true);
                }
            }
        }
    }
}
