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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import se.trixon.jota.ProcessEvent;
import se.trixon.jota.ServerEvent;
import se.trixon.jota.ServerEventListener;
import se.trixon.jota.job.Job;
import se.trixon.jota.job.JobComboBoxRenderer;
import se.trixon.jota.task.Task;
import se.trixon.jotaclient.ConnectionListener;
import se.trixon.jotaclient.Manager;
import se.trixon.jotaclient.Options;
import se.trixon.util.Xlog;
import se.trixon.util.dictionary.Dict;
import se.trixon.util.icon.Pict;
import se.trixon.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlsson
 */
public class SpeedDialPanel extends JPanel implements ConnectionListener, ServerEventListener, SpeedDialListener, TabListener {

    private final ArrayList<SpeedDialButton> mButtons = new ArrayList<>();
    private JMenuItem mResetMenuItem;
    private final JPopupMenu mPopupMenu = new JPopupMenu(Dict.JOB.getString());
    private SpeedDialButton mButton;
    private final HashSet<SpeedDialListener> mSpeedDialListeners = new HashSet<>();
    private final Options mOptions = Options.INSTANCE;
    private final Manager mManager = Manager.getInstance();
    private boolean mSimulate;

    /**
     * Creates new form SpeedDialPanel
     */
    public SpeedDialPanel() {
        initComponents();
        updateUI();
        init();
        onConnectionDisconnect();
    }

    public boolean addSpeedDialListener(SpeedDialListener speedDialListener) {
        return mSpeedDialListeners.add(speedDialListener);
    }

    @Override
    public JButton getMenuButton() {
        return menuButton;
    }

    @Override
    public void onConnectionConnect() {
        SwingUtilities.invokeLater(() -> {
            SwingHelper.enableComponents(getParent(), true);
            try {
                loadConfiguration();
            } catch (RemoteException ex) {
                Logger.getLogger(SpeedDialPanel.class.getName()).log(Level.SEVERE, null, ex);
            }
        });
    }

    @Override
    public void onConnectionDisconnect() {
        SwingUtilities.invokeLater(() -> {
            SwingHelper.enableComponents(getParent(), false);
            clearConfiguration();
            startButton.setEnabled(false);
            menuButton.setEnabled(true);
        });
    }

    @Override
    public void onProcessEvent(ProcessEvent processEvent, Job job, Task task, Object object) {
        switch (processEvent) {
            case ERR:
            case OUT:
                updateButtons(job, false);
                break;
            case STARTED:
                updateButtons(job, false);
                updateCaptions(job);
                break;
            case CANCELED:
            case FINISHED:
                updateButtons(job, true);
                updateCaptions(job);
                break;
            default:
                break;
        }
    }

    @Override
    public void onServerEvent(ServerEvent serverEvent) {
        switch (serverEvent) {
            case JOTA_CHANGED: {
                try {
                    loadConfiguration();
                } catch (RemoteException ex) {
                    Logger.getLogger(SpeedDialPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
            break;
        }
    }

    @Override
    public void onSpeedDialButtonClicked(SpeedDialButton speedDialButton) {
        requestStartJob(speedDialButton.getJob());
    }

    private void updateButtons(Job job, boolean state) {
        SwingUtilities.invokeLater(() -> {
            if (startButton.isEnabled() != state && job.getId() == ((Job) jobsComboBox.getSelectedItem()).getId()) {
                startButton.setEnabled(state);
            }

            for (SpeedDialButton button : mButtons) {
                if (button.getJobId() == job.getId()) {
                    button.setEnabled(state);
                }
            }
        });
    }

    private void updateCaptions(Job job) {
        SwingUtilities.invokeLater(() -> {
            int index = jobsComboBox.getSelectedIndex();
            try {
                loadConfiguration();
                jobsComboBox.setSelectedIndex(index);
            } catch (RemoteException ex) {
                Logger.getLogger(SpeedDialPanel.class.getName()).log(Level.SEVERE, null, ex);
            } catch (Exception ex) {

            }

            for (SpeedDialButton button : mButtons) {
                if (button.getJobId() == job.getId()) {
                    button.updateText();
                }
            }
        });
    }

    Job getSelectedJob() {
        return (Job) jobsComboBox.getSelectedItem();
    }

    private void clearConfiguration() {
        Job job = new Job(-1, "", "", "");
        jobsComboBox.removeAllItems();
        jobsComboBox.addItem(job);
        jobsComboBox.setEnabled(false);
        mPopupMenu.removeAll();
        mButtons.stream().forEach((button) -> {
            button.setJobId(-1);
        });
    }

    private void loadConfiguration() throws RemoteException {
        clearConfiguration();

        if (mManager.isConnected() && mManager.hasJobs()) {
            DefaultComboBoxModel model = mManager.getServerCommander().populateJobModel((DefaultComboBoxModel) jobsComboBox.getModel());
            jobsComboBox.setModel(model);
            jobsComboBox.setEnabled(true);
            jobsComboBox.setRenderer(new JobComboBoxRenderer());
            mPopupMenu.removeAll();

            mResetMenuItem = new JMenuItem(Dict.RESET.getString());
            mResetMenuItem.addActionListener((ActionEvent e) -> {
                mButton.setJobId(-1);
                try {
                    mManager.getServerCommander().setSpeedDial(mButton.getIndex(), -1);
                } catch (RemoteException ex) {
                    Logger.getLogger(SpeedDialPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            mPopupMenu.add(mResetMenuItem);
            mPopupMenu.add(new JSeparator());

            for (final Job job : mManager.getServerCommander().getJobs()) {
                final long jobId = job.getId();
                JMenuItem menuItem = new JMenuItem(job.toString());
                menuItem.setFont(menuItem.getFont().deriveFont(menuItem.getFont().getStyle() & ~java.awt.Font.BOLD));

                menuItem.addActionListener((ActionEvent e) -> {
                    mButton.setJobId(jobId);
                    try {
                        mManager.getServerCommander().setSpeedDial(mButton.getIndex(), jobId);
                    } catch (RemoteException ex) {
                        Logger.getLogger(SpeedDialPanel.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                mPopupMenu.add(menuItem);
            }

            for (int i = 0; i < mButtons.size(); i++) {
                SpeedDialButton button = mButtons.get(i);
                try {
                    button.setJobId(mManager.getServerCommander().getSpeedDial(i));
                    button.updateColor();
                } catch (RemoteException ex) {
                    Logger.getLogger(SpeedDialPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        } else {
            startButton.setEnabled(false);
        }
    }

    public void setButtonsVisibility(boolean visible) {
        for (SpeedDialButton button : mButtons) {
            button.setVisible(visible);
        }
    }

    @Override
    public void updateUI() {
        super.updateUI();
        if (mPopupMenu != null) {
            try {
                UIManager.setLookAndFeel(SwingHelper.getLookAndFeelClassName(mOptions.getLookAndFeel()));
                SwingUtilities.updateComponentTreeUI(mPopupMenu);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                //Xlog.timedErr(ex.getMessage());
            }
        }
    }

    private void init() {
        startButton.setIcon(Pict.Actions.MEDIA_PLAYBACK_START.get(UI.ICON_SIZE_LARGE));
        menuButton.setIcon(Pict.Custom.MENU.get(UI.ICON_SIZE_LARGE));

        startButton.setToolTipText(Dict.START.getString());
        menuButton.setToolTipText(Dict.MENU.getString());

        mButtons.clear();
        int index = -1;

        for (Component component : centerPanel.getComponents()) {
            if (component instanceof SpeedDialButton) {
                index++;
                SpeedDialButton button = (SpeedDialButton) component;
                button.setIndex(index);

                button.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mousePressed(MouseEvent evt) {
                        SwingUtilities.invokeLater(() -> {
                            if (mManager.hasJobs()) {
                                mButton = (SpeedDialButton) evt.getSource();
                                mResetMenuItem.setEnabled(mButton.getJob() != null);
                                if (!mButton.isEnabled() && evt.getButton() == MouseEvent.BUTTON1 || evt.getButton() == MouseEvent.BUTTON3) {
                                    mPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                                }
                            }
                        });
                    }
                });

                button.addActionListener((ActionEvent evt) -> {
                    mSpeedDialListeners.stream().forEach((se.trixon.jotaclient.ui.SpeedDialListener speedDialListener) -> {
                        try {
                            speedDialListener.onSpeedDialButtonClicked(mButton);
                        } catch (Exception e) {
                            // nvm
                        }
                    });
                });
                mButtons.add(button);
            }
        }

        mManager.addConnectionListeners(this);
        mManager.getClient().addServerEventListener(this);
        addSpeedDialListener(this);
        Options.INSTANCE.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            if (evt.getKey().equalsIgnoreCase(Options.KEY_CUSTOM_COLORS)) {
                if (mManager.isConnected() && mManager.hasJobs()) {
                    for (int i = 0; i < mButtons.size(); i++) {
                        SpeedDialButton button = mButtons.get(i);
                        try {
                            button.setJobId(mManager.getServerCommander().getSpeedDial(i));
                            button.updateColor();
                        } catch (RemoteException ex) {
                            Logger.getLogger(SpeedDialPanel.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            }
        });
    }

    private boolean requestStartJob(Job job) {
        Xlog.timedOut("requestStartJob() " + job.getName());

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

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        topPanel = new javax.swing.JPanel();
        jobsComboBox = new javax.swing.JComboBox();
        toolBar = new javax.swing.JToolBar();
        startButton = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        menuButton = new javax.swing.JButton();
        centerPanel = new javax.swing.JPanel();
        speedDialButton0 = new se.trixon.jotaclient.ui.SpeedDialButton();
        speedDialButton1 = new se.trixon.jotaclient.ui.SpeedDialButton();
        speedDialButton2 = new se.trixon.jotaclient.ui.SpeedDialButton();
        speedDialButton3 = new se.trixon.jotaclient.ui.SpeedDialButton();
        speedDialButton4 = new se.trixon.jotaclient.ui.SpeedDialButton();
        speedDialButton5 = new se.trixon.jotaclient.ui.SpeedDialButton();
        speedDialButton6 = new se.trixon.jotaclient.ui.SpeedDialButton();
        speedDialButton7 = new se.trixon.jotaclient.ui.SpeedDialButton();
        speedDialButton8 = new se.trixon.jotaclient.ui.SpeedDialButton();

        setLayout(new java.awt.GridBagLayout());

        topPanel.setLayout(new java.awt.GridBagLayout());

        jobsComboBox.setFont(jobsComboBox.getFont().deriveFont(jobsComboBox.getFont().getStyle() & ~java.awt.Font.BOLD));
        jobsComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jobsComboBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        topPanel.add(jobsComboBox, gridBagConstraints);

        toolBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        startButton.setFocusable(false);
        startButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        startButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        toolBar.add(startButton);
        toolBar.add(jSeparator1);

        menuButton.setFocusable(false);
        menuButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        menuButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(menuButton);

        topPanel.add(toolBar, new java.awt.GridBagConstraints());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(topPanel, gridBagConstraints);

        centerPanel.setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        centerPanel.add(speedDialButton0, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        centerPanel.add(speedDialButton1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        centerPanel.add(speedDialButton2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        centerPanel.add(speedDialButton3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        centerPanel.add(speedDialButton4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        centerPanel.add(speedDialButton5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        centerPanel.add(speedDialButton6, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        centerPanel.add(speedDialButton7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        centerPanel.add(speedDialButton8, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        add(centerPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        requestStartJob(getSelectedJob());
    }//GEN-LAST:event_startButtonActionPerformed

    private void jobsComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jobsComboBoxActionPerformed
        startButton.setEnabled(true);
    }//GEN-LAST:event_jobsComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel centerPanel;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JComboBox jobsComboBox;
    private javax.swing.JButton menuButton;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton0;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton1;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton2;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton3;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton4;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton5;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton6;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton7;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton8;
    private javax.swing.JButton startButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
