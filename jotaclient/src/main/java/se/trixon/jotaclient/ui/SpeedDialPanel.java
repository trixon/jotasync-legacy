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
import javax.swing.DefaultComboBoxModel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import se.trixon.jota.ServerEvent;
import se.trixon.jota.ServerEventListener;
import se.trixon.jota.job.Job;
import se.trixon.jotaclient.ConnectionListener;
import se.trixon.jotaclient.Manager;
import se.trixon.jotaclient.Options;
import se.trixon.util.dictionary.Dict;
import se.trixon.util.swing.SwingHelper;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class SpeedDialPanel extends JPanel implements ConnectionListener, ServerEventListener {

    private final ArrayList<SpeedDialButton> mButtons = new ArrayList<>();
    private JMenuItem mResetMenuItem;
    private final JPopupMenu mPopupMenu = new JPopupMenu(Dict.JOB.getString());
    private SpeedDialButton mButton;
    private final HashSet<SpeedDialListener> mSpeedDialListeners = new HashSet<>();
    private final Options mOptions = Options.INSTANCE;
    private final Manager mManager = Manager.getInstance();

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
            SwingHelper.enableComponents(this, true);
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
            SwingHelper.enableComponents(this, false);
            clearConfiguration();
        });
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
                } catch (RemoteException ex) {
                    Logger.getLogger(SpeedDialPanel.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
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
        mButtons.clear();
        int index = -1;

        for (Component component : getComponents()) {
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

        jobsComboBox = new javax.swing.JComboBox();
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
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        add(jobsComboBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(speedDialButton0, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(speedDialButton1, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(speedDialButton2, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(speedDialButton3, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(speedDialButton4, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(speedDialButton5, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(speedDialButton6, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(speedDialButton7, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.insets = new java.awt.Insets(8, 8, 8, 8);
        add(speedDialButton8, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jobsComboBox;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton0;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton1;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton2;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton3;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton4;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton5;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton6;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton7;
    private se.trixon.jotaclient.ui.SpeedDialButton speedDialButton8;
    // End of variables declaration//GEN-END:variables
}
