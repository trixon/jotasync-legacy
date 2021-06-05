/*
 * Copyright 2020 Patrik Karlström.
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
package se.trixon.jota.client.ui_swing;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import se.trixon.almond.util.AlmondOptions;
import se.trixon.almond.util.AlmondUI;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.material.swing.MaterialIcon;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.jota.client.ClientOptions;
import se.trixon.jota.client.ConnectionListener;
import se.trixon.jota.client.Manager;
import se.trixon.jota.shared.ProcessEvent;
import se.trixon.jota.shared.ServerEvent;
import se.trixon.jota.shared.ServerEventListener;
import se.trixon.jota.shared.job.Job;
import se.trixon.jota.shared.job.JobComboBoxRenderer;
import se.trixon.jota.shared.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public final class SpeedDialPanel extends JPanel implements ConnectionListener, ServerEventListener, SpeedDialListener {

    private final ArrayList<SpeedDialButton> mButtons = new ArrayList<>();
    private JMenuItem mEditMenuItem;
    private JMenuItem mEditorMenuItem;
    private JMenuItem mResetMenuItem;
    private final JPopupMenu mPopupMenu = new JPopupMenu(Dict.JOB.toString());
    private SpeedDialButton mButton;
    private final HashSet<SpeedDialListener> mSpeedDialListeners = new HashSet<>();
    private final ClientOptions mOptions = ClientOptions.getInstance();
    private final Manager mManager = Manager.getInstance();
    private final ResourceBundle mBundle = SystemHelper.getBundle(MainFrame.class, "Bundle");
    private final AlmondOptions mAlmondOptions = AlmondOptions.getInstance();

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
            case FAILED:
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

    private void requestStartJob(Job job) {
        MainFrame mainFrame = (MainFrame) SwingUtilities.getRoot(this);
        mainFrame.requestStartJob(job);
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
        jobsComboBox.removeAllItems();
        jobsComboBox.setEnabled(false);
        mPopupMenu.removeAll();
        mButtons.stream().forEach((button) -> {
            button.setJobId(-1);
        });
    }

    private void loadConfiguration() throws RemoteException {
        clearConfiguration();

        if (mManager.isConnected() && mManager.hasJobs()) {
            DefaultComboBoxModel model = (DefaultComboBoxModel) jobsComboBox.getModel();
            mManager.getServerCommander().getJobs().stream().forEach((job) -> {
                model.addElement(job);
            });

            jobsComboBox.setModel(model);
            jobsComboBox.setEnabled(true);
            jobsComboBox.setRenderer(new JobComboBoxRenderer());
            mPopupMenu.removeAll();

            mEditMenuItem = new JMenuItem(Dict.EDIT.toString());
            mEditorMenuItem = new JMenuItem(mBundle.getString("jobEditor"));
            mResetMenuItem = new JMenuItem(Dict.RESET.toString());
            mResetMenuItem.addActionListener((ActionEvent e) -> {
                mButton.setJobId(-1);
                try {
                    mManager.getServerCommander().setSpeedDial(mButton.getIndex(), -1);
                } catch (RemoteException ex) {
                    Logger.getLogger(SpeedDialPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            });
            mPopupMenu.add(mEditMenuItem);
            mPopupMenu.add(mEditorMenuItem);
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
        if (mPopupMenu != null && mAlmondOptions.isForceLookAndFeel()) {
            try {
                UIManager.setLookAndFeel(SwingHelper.getLookAndFeelClassName(mAlmondOptions.getLookAndFeel()));
                SwingUtilities.updateComponentTreeUI(mPopupMenu);
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException ex) {
                //Xlog.timedErr(ex.getMessage());
            }
        }
    }

    private void init() {
        startButton.setIcon(MaterialIcon._Av.PLAY_ARROW.getImageIcon(AlmondUI.ICON_SIZE_NORMAL));
        startButton.setToolTipText(Dict.START.toString());

        final ActionListener editActionListener = (ActionEvent e) -> {
            MainFrame mainFrame = (MainFrame) SwingUtilities.getRoot(SpeedDialPanel.this);
            mainFrame.showEditor(mButton.getJob().getId(), true);
        };

        final ActionListener editorActionListener = (ActionEvent e) -> {
            MainFrame mainFrame = (MainFrame) SwingUtilities.getRoot(SpeedDialPanel.this);
            mainFrame.showEditor(mButton.getJob().getId(), false);
        };

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
                                mEditMenuItem.setEnabled(mButton.getJob() != null);
                                mEditorMenuItem.setEnabled(mButton.getJob() != null);
                                mEditorMenuItem.removeActionListener(editorActionListener);
                                mEditMenuItem.removeActionListener(editActionListener);
                                if (mButton.getJob() == null) {
                                    mEditMenuItem.setText(Dict.EDIT.toString());
                                } else {
                                    mEditMenuItem.setText(String.format("%s %s", Dict.EDIT.toString(), mButton.getJob().getName()));
                                }

                                if (!mButton.isEnabled() && evt.getButton() == MouseEvent.BUTTON1 || evt.getButton() == MouseEvent.BUTTON3) {
                                    mEditMenuItem.addActionListener(editActionListener);
                                    mEditorMenuItem.addActionListener(editorActionListener);
                                    mPopupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
                                }
                            }
                        });
                    }
                });

                button.addActionListener((ActionEvent evt) -> {
                    mSpeedDialListeners.stream().forEach((se.trixon.jota.client.ui_swing.SpeedDialListener speedDialListener) -> {
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
        ClientOptions.getInstance().getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            String key = evt.getKey();
            if (key.equalsIgnoreCase(ClientOptions.KEY_CUSTOM_COLORS)) {
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

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        topPanel = new javax.swing.JPanel();
        jobsComboBox = new javax.swing.JComboBox();
        toolBar = new javax.swing.JToolBar();
        startButton = new javax.swing.JButton();
        centerPanel = new javax.swing.JPanel();
        speedDialButton0 = new se.trixon.jota.client.ui_swing.SpeedDialButton();
        speedDialButton1 = new se.trixon.jota.client.ui_swing.SpeedDialButton();
        speedDialButton2 = new se.trixon.jota.client.ui_swing.SpeedDialButton();
        speedDialButton3 = new se.trixon.jota.client.ui_swing.SpeedDialButton();
        speedDialButton4 = new se.trixon.jota.client.ui_swing.SpeedDialButton();
        speedDialButton5 = new se.trixon.jota.client.ui_swing.SpeedDialButton();
        speedDialButton6 = new se.trixon.jota.client.ui_swing.SpeedDialButton();
        speedDialButton7 = new se.trixon.jota.client.ui_swing.SpeedDialButton();
        speedDialButton8 = new se.trixon.jota.client.ui_swing.SpeedDialButton();

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
    private javax.swing.JComboBox jobsComboBox;
    private se.trixon.jota.client.ui_swing.SpeedDialButton speedDialButton0;
    private se.trixon.jota.client.ui_swing.SpeedDialButton speedDialButton1;
    private se.trixon.jota.client.ui_swing.SpeedDialButton speedDialButton2;
    private se.trixon.jota.client.ui_swing.SpeedDialButton speedDialButton3;
    private se.trixon.jota.client.ui_swing.SpeedDialButton speedDialButton4;
    private se.trixon.jota.client.ui_swing.SpeedDialButton speedDialButton5;
    private se.trixon.jota.client.ui_swing.SpeedDialButton speedDialButton6;
    private se.trixon.jota.client.ui_swing.SpeedDialButton speedDialButton7;
    private se.trixon.jota.client.ui_swing.SpeedDialButton speedDialButton8;
    private javax.swing.JButton startButton;
    private javax.swing.JToolBar toolBar;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables
}
