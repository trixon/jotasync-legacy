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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.rmi.RemoteException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.PreferenceChangeEvent;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.AlmondOptions;
import se.trixon.almond.util.AlmondUI;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.FileHelper;
import se.trixon.almond.util.icons.material.swing.MaterialIcon;
import se.trixon.almond.util.swing.LogPanel;
import se.trixon.almond.util.swing.dialogs.Message;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;
import se.trixon.jota.client.ClientOptions;
import se.trixon.jota.client.Manager;
import se.trixon.jota.shared.ProcessEvent;
import se.trixon.jota.shared.job.Job;

/**
 *
 * @author Patrik Karlström
 */
public class TabItem extends JPanel {

    private boolean mClosable;
    private TabCloser mCloser;
    private Job mJob;
    private boolean mLastLineWasBlank;
    private boolean mLastRowWasProgress;
    private final Manager mManager = Manager.getInstance();
    private long mTimeFinished;
    private Progress mProgress;
    private final AlmondOptions mAlmondOptions = AlmondOptions.getInstance();
    private final ClientOptions mOptions = ClientOptions.INSTANCE;

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

    synchronized public void log(ProcessEvent processEvent, String string) {
        SwingUtilities.invokeLater(() -> {
            String line = string + "\n";

            LogPanel lp = logPanel;

            if (mOptions.isSplitDeletions() && StringUtils.startsWith(line, "deleting ")) {
                lp = deletionsLogPanel;
                if (lp.getParent() == null) {
                    tabbedPane.addTab(Dict.DELETIONS.toString(), MaterialIcon._Action.DELETE.getImageIcon(AlmondUI.ICON_SIZE_NORMAL), lp);
                }
            } else if (mOptions.isSplitErrors() && (StringUtils.startsWith(line, "rsync: ") || StringUtils.startsWith(line, "rsync error: "))) {
                lp = errorsLogPanel;
                if (lp.getParent() == null) {
                    tabbedPane.addTab(Dict.Dialog.ERRORS.toString(), MaterialIcon._Alert.ERROR_OUTLINE.getImageIcon(AlmondUI.ICON_SIZE_NORMAL), lp);
                }
            }

            if (mProgress.parse(line)) {
                progressBar.setIndeterminate(false);
                progressBar.setStringPainted(true);

                progressBar.setValue(mProgress.getPercentage());
                progressBar.setString(mProgress.toString());
                mLastRowWasProgress = true;
            } else {
                if (mLastRowWasProgress && mLastLineWasBlank) {
                    try {
                        int size = lp.getText().length();
                        lp.getTextArea().replaceRange(null, size - 1, size);
                    } catch (IllegalArgumentException e) {
                    }
                }
                lp.getTextArea().append(line);
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
        cancelButton.setVisible(false);
        startButton.setVisible(true);
        mTimeFinished = System.currentTimeMillis();
        mClosable = true;
        mCloser.getButton().setEnabled(true);
    }

    void setCloser(TabCloser tabCloser) {
        mCloser = tabCloser;
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
        String jobName = FileHelper.replaceInvalidChars(mJob.getName());

        String filename = String.format("%s_%s.%s", jobName, getFinishedTime(), ext);
        SimpleDialog.setSelectedFile(new File(filename));
        if (SimpleDialog.saveFile(new String[]{ext})) {
            try {
                FileUtils.writeStringToFile(SimpleDialog.getPath(), logPanel.getText(), Charset.defaultCharset());
            } catch (IOException ex) {
                Message.error(this, Dict.Dialog.TITLE_IO_ERROR.toString(), ex.getLocalizedMessage());
            }
        }
    }

    synchronized void start() {
        initSubTabs();
        progressBar.setIndeterminate(true);
        progressBar.setStringPainted(false);

        editButton.setEnabled(false);
        cancelButton.setVisible(true);
        startButton.setVisible(false);
        mClosable = false;
    }

    private void init() {
        logPanel.setWordWrap(mOptions.isWordWrap());
        deletionsLogPanel.setWordWrap(mOptions.isWordWrap());
        errorsLogPanel.setWordWrap(mOptions.isWordWrap());

        cancelButton.setToolTipText(Dict.CANCEL.toString());
        editButton.setToolTipText(Dict.EDIT.toString());
        startButton.setToolTipText(Dict.START.toString());

        mProgress = new Progress();
        updateIcons();

        mOptions.getPreferences().addPreferenceChangeListener((PreferenceChangeEvent evt) -> {
            if (evt.getKey().equalsIgnoreCase(ClientOptions.KEY_WORD_WRAP)) {
                logPanel.setWordWrap(mOptions.isWordWrap());
                deletionsLogPanel.setWordWrap(mOptions.isWordWrap());
                errorsLogPanel.setWordWrap(mOptions.isWordWrap());
            }
        });
    }

    private void initSubTabs() {
        logPanel.clear();
        errorsLogPanel.clear();
        deletionsLogPanel.clear();

        holderPanel.removeAll();
        tabbedPane.removeAll();

        if (mOptions.isSplitDeletions() || mOptions.isSplitErrors()) {
            holderPanel.add(tabbedPane);
            tabbedPane.addTab(Dict.LOG.toString(), MaterialIcon._Action.INFO_OUTLINE.getImageIcon(AlmondUI.ICON_SIZE_NORMAL), logPanel);
        } else {
            holderPanel.add(logPanel);
        }
    }

    void updateIcons() {
        cancelButton.setIcon(MaterialIcon._Navigation.CANCEL.getImageIcon(AlmondUI.ICON_SIZE_NORMAL));
        editButton.setIcon(MaterialIcon._Editor.MODE_EDIT.getImageIcon(AlmondUI.ICON_SIZE_NORMAL));
        startButton.setIcon(MaterialIcon._Av.PLAY_ARROW.getImageIcon(AlmondUI.ICON_SIZE_NORMAL));
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        logPanel = new se.trixon.almond.util.swing.LogPanel();
        errorsLogPanel = new se.trixon.almond.util.swing.LogPanel();
        deletionsLogPanel = new se.trixon.almond.util.swing.LogPanel();
        tabbedPane = new javax.swing.JTabbedPane();
        progressBar = new javax.swing.JProgressBar();
        toolBar = new javax.swing.JToolBar();
        editButton = new javax.swing.JButton();
        cancelButton = new javax.swing.JButton();
        startButton = new javax.swing.JButton();
        holderPanel = new javax.swing.JPanel();

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

        startButton.setFocusable(false);
        startButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        startButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        startButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                startButtonActionPerformed(evt);
            }
        });
        toolBar.add(startButton);

        add(toolBar, new java.awt.GridBagConstraints());

        holderPanel.setLayout(new java.awt.GridLayout(1, 0));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(holderPanel, gridBagConstraints);
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
                Message.error(this, Dict.Dialog.ERROR.toString(), Dict.JOB_NOT_FOUND.toString());
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
    private se.trixon.almond.util.swing.LogPanel deletionsLogPanel;
    private javax.swing.JButton editButton;
    private se.trixon.almond.util.swing.LogPanel errorsLogPanel;
    private javax.swing.JPanel holderPanel;
    private se.trixon.almond.util.swing.LogPanel logPanel;
    private javax.swing.JProgressBar progressBar;
    private javax.swing.JButton startButton;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables
}
