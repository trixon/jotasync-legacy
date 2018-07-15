/*
 * Copyright 2018 Patrik Karlström.
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

import com.apple.eawt.AppEvent;
import com.apple.eawt.Application;
import java.awt.event.ActionEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.SocketException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.DefaultComboBoxModel;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.AboutModel;
import se.trixon.almond.util.AlmondAction;
import se.trixon.almond.util.AlmondOptions;
import se.trixon.almond.util.AlmondOptionsPanel;
import se.trixon.almond.util.AlmondUI;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.almond.util.swing.dialogs.HtmlPanel;
import se.trixon.almond.util.swing.dialogs.MenuModePanel;
import se.trixon.almond.util.swing.dialogs.Message;
import se.trixon.almond.util.swing.dialogs.about.AboutPanel;
import se.trixon.jota.client.Client;
import se.trixon.jota.client.ClientOptions;
import se.trixon.jota.client.ConnectionListener;
import se.trixon.jota.client.Manager;
import se.trixon.jota.client.ui.editor.EditorPanel;
import se.trixon.jota.server.JobValidator;
import se.trixon.jota.shared.ProcessEvent;
import se.trixon.jota.shared.ServerEvent;
import se.trixon.jota.shared.ServerEventListener;
import se.trixon.jota.shared.job.Job;
import se.trixon.jota.shared.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class MainFrame extends JFrame implements ConnectionListener, ServerEventListener {

    private static final boolean IS_MAC = SystemUtils.IS_OS_MAC;

    private ActionManager mActionManager;
    private boolean mShutdownInProgress;
    private boolean mServerShutdownRequested;
    private final ClientOptions mOptions = ClientOptions.INSTANCE;
    private final Client mClient;
    private final ResourceBundle mBundle = SystemHelper.getBundle(MainFrame.class, "Bundle");
    private final Manager mManager = Manager.getInstance();
    private TabHolder mTabHolder;
    private final AlmondOptions mAlmondOptions = AlmondOptions.getInstance();
    private final AlmondUI mAlmondUI = AlmondUI.getInstance();

    /**
     * Creates new form MainFrame
     */
    public MainFrame() {
        initComponents();

        mAlmondUI.addWindowWatcher(this);
        mAlmondUI.initoptions();

        initActions();
        init();

        if (IS_MAC) {
            initMac();
        }

        initMenus();

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
                Message.html(this, Dict.Dialog.ERROR_VALIDATION.toString(), validator.getSummaryAsHtml());
            }
        } catch (RemoteException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }

        return false;
    }

    private void enableGui(boolean state) {
        boolean cronActive = false;

        try {
            cronActive = state && mManager.getServerCommander().isCronActive();
        } catch (RemoteException ex) {
        }

        mActionManager.getAction(ActionManager.CRON).putValue(Action.SELECTED_KEY, cronActive);

        mActionManager.getConditionallyEnabledActions().stream().forEach((action) -> {
            action.setEnabled(state);
        });

        mActionManager.getAction(ActionManager.CLOSE_TAB).setEnabled(false);
        mActionManager.getAction(ActionManager.SAVE_TAB).setEnabled(false);

        if (state) {
            updateWindowTitle();
        } else {
            setTitle("JotaSync");
        }
    }

    private void init() {
        String fileName = String.format("/%s/sync-256px.png", getClass().getPackage().getName().replace(".", "/"));
        ImageIcon imageIcon = new ImageIcon(getClass().getResource(fileName));
        setIconImage(imageIcon.getImage());

        mTabHolder = new TabHolder();
        add(mTabHolder);
        mTabHolder.initActions();
        mManager.addConnectionListeners(this);

        initListeners();
        updateWindowTitle();
    }

    private void initActions() {
        mActionManager = ActionManager.getInstance().init(getRootPane().getActionMap(), getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW));

        //about
        PomInfo pomInfo = new PomInfo(MainFrame.class, "se.trixon", "jotasync");
        AboutModel aboutModel = new AboutModel(SystemHelper.getBundle(MainFrame.class, "about"), SystemHelper.getResourceAsImageIcon(MainFrame.class, "sync-256px.png"));

        aboutModel.setAppVersion(pomInfo.getVersion());
        AboutPanel aboutPanel = new AboutPanel(aboutModel);
        AlmondAction action = AboutPanel.getAction(MainFrame.this, aboutPanel);

        getRootPane().getActionMap().put(ActionManager.ABOUT, action);

        //File
        connectMenuItem.setAction(mActionManager.getAction(ActionManager.CONNECT));
        disconnectMenuItem.setAction(mActionManager.getAction(ActionManager.DISCONNECT));
        startServerMenuItem.setAction(mActionManager.getAction(ActionManager.START_SERVER));
        shutdownServerMenuItem.setAction(mActionManager.getAction(ActionManager.SHUTDOWN_SERVER));
        shutdownServerQuitMenuItem.setAction(mActionManager.getAction(ActionManager.SHUTDOWN_SERVER_QUIT));
        saveMenuItem.setAction(mActionManager.getAction(ActionManager.SAVE_TAB));
        quitMenuItem.setAction(mActionManager.getAction(ActionManager.QUIT));

        //Tools
        cronCheckBoxMenuItem.setAction(mActionManager.getAction(ActionManager.CRON));
        jobEditorMenuItem.setAction(mActionManager.getAction(ActionManager.JOB_EDITOR));
        optionsMenuItem.setAction(mActionManager.getAction(ActionManager.OPTIONS));

        //Help
        helpMenuItem.setAction(mActionManager.getAction(ActionManager.HELP));
        aboutMenuItem.setAction(mActionManager.getAction(ActionManager.ABOUT));
        aboutRsyncMenuItem.setAction(mActionManager.getAction(ActionManager.ABOUT_RSYNC));
    }

    private void initListeners() {
        mActionManager.addAppListener(new ActionManager.AppListener() {
            @Override
            public void onAboutRsync(ActionEvent actionEvent) {
                try {
                    String aboutRsync = mManager.getServerCommander().getAboutRsync();
                    Message.information(MainFrame.this, String.format(Dict.ABOUT_S.toString(), "rsync"), aboutRsync);
                } catch (RemoteException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void onCancel(ActionEvent actionEvent) {
            }

            @Override
            public void onClientConnect(ActionEvent actionEvent) {
                try {
                    requestConnect();
                } catch (NotBoundException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void onClientDisconnect(ActionEvent actionEvent) {
                mManager.disconnect();
            }

            @Override
            public void onClose(ActionEvent actionEvent) {
                mTabHolder.closeTab();
            }

            @Override
            public void onCron(ActionEvent actionEvent) {
                boolean nextState = false;
                try {
                    nextState = !mManager.getServerCommander().isCronActive();
                } catch (RemoteException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                mActionManager.getAction(ActionManager.CRON).putValue(Action.SELECTED_KEY, !nextState);

                Client.Command command = Client.Command.STOP_CRON;
                if (nextState) {
                    command = Client.Command.START_CRON;
                }
                mClient.execute(command);
            }

            @Override
            public void onEdit(ActionEvent actionEvent) {
                showEditor(-1, false);
            }

            @Override
            public void onMenu(ActionEvent actionEvent) {
            }

            @Override
            public void onOptions(ActionEvent actionEvent) {
                showOptions();
            }

            @Override
            public void onQuit(ActionEvent actionEvent) {
                quit();
            }

            @Override
            public void onSave(ActionEvent actionEvent) {
                mTabHolder.saveTab();
            }

            @Override
            public void onServerShutdown(ActionEvent actionEvent) {
                serverShutdown();
            }

            @Override
            public void onServerShutdownAndQuit(ActionEvent actionEvent) {
                serverShutdown();
                quit();
            }

            @Override
            public void onServerStart(ActionEvent actionEvent) {
                try {
                    if (mClient.serverStart()) {
                        mManager.connect(SystemHelper.getHostname(), mOptions.getAutostartServerPort());
                    }
                } catch (URISyntaxException | IOException | NotBoundException ex) {
                    Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void onStart(ActionEvent actionEvent) {
            }
        });
    }

    private void initMac() {
        Application macApplication = Application.getApplication();
        macApplication.setAboutHandler((AppEvent.AboutEvent ae) -> {
            mActionManager.getAction(ActionManager.ABOUT).actionPerformed(null);
        });

        macApplication.setPreferencesHandler((AppEvent.PreferencesEvent pe) -> {
            mActionManager.getAction(ActionManager.OPTIONS).actionPerformed(null);
        });
    }

    private void initMenus() {
        if (mAlmondOptions.getMenuMode() == MenuModePanel.MenuMode.BUTTON) {
            sPopupMenu.add(connectMenuItem);
            sPopupMenu.add(disconnectMenuItem);
            sPopupMenu.add(serverMenu);
            sPopupMenu.add(new JSeparator());
            sPopupMenu.add(cronCheckBoxMenuItem);
            sPopupMenu.add(jobEditorMenuItem);
            if (!IS_MAC) {
                sPopupMenu.add(optionsMenuItem);
            }
            sPopupMenu.add(new JSeparator());
            sPopupMenu.add(helpMenuItem);
            sPopupMenu.add(aboutRsyncMenuItem);
            if (!IS_MAC) {
                sPopupMenu.add(aboutMenuItem);
            }
            sPopupMenu.add(new JSeparator());
            sPopupMenu.add(saveMenuItem);

            if (!IS_MAC) {
                sPopupMenu.add(quitMenuItem);
            }

        } else {
            setJMenuBar(menuBar);
            if (IS_MAC) {
                fileMenu.remove(quitMenuItem);
                toolsMenu.remove(optionsMenuItem);
                helpMenu.remove(aboutMenuItem);
            }

            fileMenu.setVisible(fileMenu.getComponents().length > 0 || !IS_MAC);
            toolsMenu.setVisible(toolsMenu.getComponents().length > 0 || !IS_MAC);
        }

        SwingHelper.clearToolTipText(menuBar);
        SwingHelper.clearToolTipText(sPopupMenu);
    }

    public static JPopupMenu getPopupMenu() {
        return sPopupMenu;
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
                Message.error(this, Dict.Dialog.ERROR.toString(), String.format(Dict.INVALID_PORT.toString(), portString));
            } catch (NotBoundException | MalformedURLException | RemoteException | SocketException ex) {
                Message.error(this, Dict.Dialog.ERROR.toString(), ex.getLocalizedMessage());
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

        Object[] options = new Object[]{AlmondOptionsPanel.getGlobalOptionsButton(optionsPanel), new JSeparator(), Dict.CANCEL, Dict.OK};
        int retval = JOptionPane.showOptionDialog(this,
                optionsPanel,
                Dict.OPTIONS.toString(),
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                options,
                Dict.OK);

        if (retval == Arrays.asList(options).indexOf(Dict.OK)) {
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
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        sPopupMenu = new javax.swing.JPopupMenu();
        menuBar = new javax.swing.JMenuBar();
        fileMenu = new javax.swing.JMenu();
        connectMenuItem = new javax.swing.JMenuItem();
        disconnectMenuItem = new javax.swing.JMenuItem();
        serverMenu = new javax.swing.JMenu();
        startServerMenuItem = new javax.swing.JMenuItem();
        shutdownServerMenuItem = new javax.swing.JMenuItem();
        shutdownServerQuitMenuItem = new javax.swing.JMenuItem();
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        saveMenuItem = new javax.swing.JMenuItem();
        quitMenuItem = new javax.swing.JMenuItem();
        toolsMenu = new javax.swing.JMenu();
        cronCheckBoxMenuItem = new javax.swing.JCheckBoxMenuItem();
        jobEditorMenuItem = new javax.swing.JMenuItem();
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        optionsMenuItem = new javax.swing.JMenuItem();
        helpMenu = new javax.swing.JMenu();
        helpMenuItem = new javax.swing.JMenuItem();
        aboutRsyncMenuItem = new javax.swing.JMenuItem();
        aboutMenuItem = new javax.swing.JMenuItem();

        fileMenu.setText(Dict.FILE_MENU.toString());
        fileMenu.add(connectMenuItem);
        fileMenu.add(disconnectMenuItem);

        serverMenu.setText(Dict.SERVER.toString());
        serverMenu.add(startServerMenuItem);
        serverMenu.add(shutdownServerMenuItem);
        serverMenu.add(shutdownServerQuitMenuItem);

        fileMenu.add(serverMenu);
        fileMenu.add(jSeparator1);
        fileMenu.add(saveMenuItem);
        fileMenu.add(quitMenuItem);

        menuBar.add(fileMenu);

        toolsMenu.setText(Dict.TOOLS.toString());
        toolsMenu.add(cronCheckBoxMenuItem);
        toolsMenu.add(jobEditorMenuItem);
        toolsMenu.add(jSeparator2);
        toolsMenu.add(optionsMenuItem);

        menuBar.add(toolsMenu);

        helpMenu.setText(Dict.HELP.toString());
        helpMenu.add(helpMenuItem);
        helpMenu.add(aboutRsyncMenuItem);
        helpMenu.add(aboutMenuItem);

        menuBar.add(helpMenu);

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("JotaSync"); // NOI18N
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
    private javax.swing.JMenuItem connectMenuItem;
    private javax.swing.JCheckBoxMenuItem cronCheckBoxMenuItem;
    private javax.swing.JMenuItem disconnectMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JMenu helpMenu;
    private javax.swing.JMenuItem helpMenuItem;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JMenuItem jobEditorMenuItem;
    private javax.swing.JMenuBar menuBar;
    private javax.swing.JMenuItem optionsMenuItem;
    private javax.swing.JMenuItem quitMenuItem;
    private static javax.swing.JPopupMenu sPopupMenu;
    private javax.swing.JMenuItem saveMenuItem;
    private javax.swing.JMenu serverMenu;
    private javax.swing.JMenuItem shutdownServerMenuItem;
    private javax.swing.JMenuItem shutdownServerQuitMenuItem;
    private javax.swing.JMenuItem startServerMenuItem;
    private javax.swing.JMenu toolsMenu;
    // End of variables declaration//GEN-END:variables
}
