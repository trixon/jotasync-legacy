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

import java.io.File;
import java.io.IOException;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import se.trixon.jota.client.Manager;
import se.trixon.jota.shared.ProcessEvent;
import se.trixon.jota.shared.job.Job;
import se.trixon.util.dictionary.Dict;
import se.trixon.util.icon.Pict;
import se.trixon.util.swing.dialogs.Message;
import se.trixon.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlsson
 */
public class TabItem extends JPanel implements TabListener {

    private boolean mClosable;
    private Job mJob;
    private boolean mLastLineWasBlank;
    private boolean mLastRowWasProgress;
    private final Manager mManager = Manager.getInstance();
    private long mTimeFinished;
    private Progress mProgress;

    /**
     * Creates new form TabItem
     *
     * @param job
     */
    public TabItem(Job job) {
        initComponents();
        init();
        mJob = job;
        progressBar.setValue(0);
    }

    public Job getJob() {
        return mJob;
    }

    public JButton getCloseButton() {
        return closeButton;
    }

    @Override
    public JButton getMenuButton() {
        return menuButton;
    }

    public JButton getSaveButton() {
        return saveButton;
    }

    synchronized public void log(ProcessEvent processEvent, String string) {
        SwingUtilities.invokeLater(() -> {
            StringBuilder builder = new StringBuilder(string).append("\n");
            if (processEvent == ProcessEvent.ERR) {
                builder.insert(0, "E: ");
            }

            String line = builder.toString();
            if (mProgress.parse(line)) {
                progressBar.setIndeterminate(false);
                progressBar.setStringPainted(true);

                progressBar.setValue(mProgress.getPercentage());
                progressBar.setString(mProgress.toString());
                mLastRowWasProgress = true;
            } else {
                if (mLastRowWasProgress && mLastLineWasBlank) {
                    int size = logPanel.getText().length();
                    logPanel.getTextArea().replaceRange(null, size - 1, size);
                }
                logPanel.getTextArea().append(line);
                mLastLineWasBlank = StringUtils.isBlank(line);
                mLastRowWasProgress = false;
            }

        });
    }

    void cancel() {
        try {
            mManager.getServerCommander().stopJob(mJob);
        } catch (RemoteException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    void enableSave() {
        progressBar.setIndeterminate(false);
        progressBar.setValue(100);
        progressBar.setStringPainted(false);
        editButton.setEnabled(true);
        startButton.setEnabled(true);
        saveButton.setEnabled(true);
        cancelButton.setVisible(false);
        closeButton.setVisible(true);
        mTimeFinished = System.currentTimeMillis();
        mClosable = true;
    }

    private String getFinishedTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyyMMdd_HHmmss");
        return simpleDateFormat.format(new Date(mTimeFinished));
    }

    boolean isCancelable() {
        return cancelButton.isVisible() && cancelButton.isEnabled();
    }

    boolean isClosable() {
        return mClosable;
    }

    void save() {
        String ext = "log";
        FileNameExtensionFilter filter = new FileNameExtensionFilter(Dict.EXTENSION_FILTER_LOG.toString(), ext);

        SimpleDialog.clearFilters();
        SimpleDialog.addFilter(filter);
        SimpleDialog.setFilter(filter);
        SimpleDialog.setParent(this);

        String filename = String.format("%s_%s.%s", mJob.getName(), getFinishedTime(), ext);
        SimpleDialog.setSelectedFile(new File(filename));
        if (SimpleDialog.saveFile(new String[]{ext})) {
            try {
                FileUtils.writeStringToFile(SimpleDialog.getPath(), logPanel.getText());
            } catch (IOException ex) {
                Message.error(this, Dict.IO_ERROR_TITLE.toString(), ex.getLocalizedMessage());
            }
        }
    }

    synchronized void start() {
        logPanel.clear();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(false);

        editButton.setEnabled(false);
        startButton.setEnabled(false);
        saveButton.setEnabled(false);
        cancelButton.setVisible(true);
        closeButton.setVisible(false);
        mClosable = false;
    }

    private void init() {
        logPanel.getTextArea().setLineWrap(true);
        cancelButton.setIcon(Pict.Actions.PROCESS_STOP.get(UI.ICON_SIZE_LARGE));
        editButton.setIcon(Pict.Actions.DOCUMENT_EDIT.get(UI.ICON_SIZE_LARGE));
        menuButton.setIcon(Pict.Custom.MENU.get(UI.ICON_SIZE_LARGE));
        startButton.setIcon(Pict.Actions.MEDIA_PLAYBACK_START.get(UI.ICON_SIZE_LARGE));

        cancelButton.setToolTipText(Dict.CANCEL.toString());
        editButton.setToolTipText(Dict.EDIT.toString());
        menuButton.setToolTipText(Dict.MENU.toString());
        startButton.setToolTipText(Dict.START.toString());

        closeButton.setVisible(false);

        mProgress = new Progress();
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

        progressBar = new javax.swing.JProgressBar();
        toolBar = new javax.swing.JToolBar();
        saveButton = new javax.swing.JButton();
        editButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        closeButton = new javax.swing.JButton();
        startButton = new javax.swing.JButton();
        menuButton = new javax.swing.JButton();
        logPanel = new se.trixon.util.swing.LogPanel();

        setLayout(new java.awt.GridBagLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.weightx = 1.0;
        add(progressBar, gridBagConstraints);

        toolBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        saveButton.setFocusable(false);
        saveButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        saveButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(saveButton);

        editButton.setFocusable(false);
        editButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        editButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        editButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editButtonActionPerformed(evt);
            }
        });
        toolBar.add(editButton);

        cancelButton.setFocusable(false);
        cancelButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        cancelButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        cancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cancelButtonActionPerformed(evt);
            }
        });
        toolBar.add(cancelButton);

        closeButton.setFocusable(false);
        closeButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        closeButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(closeButton);

        startButton.setFocusable(false);
        startButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        startButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        toolBar.add(startButton);

        menuButton.setFocusable(false);
        menuButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        menuButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        toolBar.add(menuButton);

        add(toolBar, new java.awt.GridBagConstraints());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(logPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void cancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cancelButtonActionPerformed
        cancel();
    }//GEN-LAST:event_cancelButtonActionPerformed

    private void editButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editButtonActionPerformed
        MainFrame mainFrame = (MainFrame) SwingUtilities.getRoot(this);
        mainFrame.showEditor(mJob.getId(), true);
    }//GEN-LAST:event_editButtonActionPerformed

    private void startButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_startButtonActionPerformed
        try {
            Job job = mManager.getServerCommander().getJob(mJob.getId());
            if (job == null) {
                Message.error(this, Dict.ERROR.toString(), Dict.JOB_NOT_FOUND.toString());
            } else {
                mJob = job;
                MainFrame mainFrame = (MainFrame) SwingUtilities.getRoot(this);
                mainFrame.requestStartJob(job);
            }
        } catch (RemoteException ex) {
            Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_startButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton cancelButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JButton editButton;
    private se.trixon.util.swing.LogPanel logPanel;
    private javax.swing.JButton menuButton;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton saveButton;
    private javax.swing.JButton startButton;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables
}
