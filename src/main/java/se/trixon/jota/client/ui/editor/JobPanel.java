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
package se.trixon.jota.client.ui.editor;

import java.awt.Color;
import java.io.File;
import org.apache.commons.lang3.StringUtils;
import se.trixon.jota.shared.job.JobExecuteSection;
import se.trixon.jota.shared.job.Job;
import se.trixon.util.GraphicsHelper;
import se.trixon.util.swing.dialogs.FileChooserPanel;
import se.trixon.util.dictionary.Dict;
import se.trixon.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlsson
 */
public class JobPanel extends javax.swing.JPanel implements FileChooserPanel.FileChooserButtonListener {

    private Job mJob = new Job();

    /**
     * Creates new form JobPanel
     */
    public JobPanel() {
        initComponents();
        init();
    }

    public Job getJob() {
        Job job = new Job(mJob.getId(), nameTextField.getText(), descriptionTextField.getText(), detailsTextArea.getText());

        JobExecuteSection executeSection = job.getExecuteSection();
        executeSection.setBefore(beforePanel.isSelected());
        executeSection.setBeforeCommand(beforePanel.getPath());
        executeSection.setBeforeHaltOnError(beforeHaltOnErrorCheckBox.isSelected());

        executeSection.setAfterFailure(afterFailurePanel.isSelected());
        executeSection.setAfterFailureCommand(afterFailurePanel.getPath());

        executeSection.setAfterSuccess(afterSuccessPanel.isSelected());
        executeSection.setAfterSuccessCommand(afterSuccessPanel.getPath());

        executeSection.setAfter(afterPanel.isSelected());
        executeSection.setAfterCommand(afterPanel.getPath());

        job.setCronActive(cronPanel.isCronActive());
        job.setCronItems(cronPanel.getCronItems());

        if (!previewButton.getBackground().equals(resetButton.getBackground())) {
            job.setColorBackground(GraphicsHelper.colorToString(previewButton.getBackground()));
        }

        //if (!previewButton.getForeground().equals(resetButton.getForeground())) {
        job.setColorForeground(GraphicsHelper.colorToString(previewButton.getForeground()));
        //}
        int logMode = 0;
        if (logAppendRadioButton.isSelected()) {
            logMode = 0;
        } else if (logReplaceRadioButton.isSelected()) {
            logMode = 1;
        } else if (logUniqueRadioButton.isSelected()) {
            logMode = 2;
        }

        job.setLogOutput(logOutputCheckBox.isSelected());
        job.setLogErrors(logErrorsCheckBox.isSelected());
        job.setLogSeparateErrors(logSeparateCheckBox.isSelected());
        job.setLogMode(logMode);

        job.setLastRun(mJob.getLastRun());
        job.setLastRunExitCode(mJob.getLastRunExitCode());
        job.setTasks(mJob.getTasks());

        return job;
    }

    @Override
    public void onFileChooserCancel(FileChooserPanel fileChooserPanel) {
    }

    @Override
    public void onFileChooserCheckBoxChange(FileChooserPanel fileChooserPanel, boolean isSelected) {
        if (fileChooserPanel == beforePanel) {
            beforeHaltOnErrorCheckBox.setEnabled(isSelected);
        }
    }

    @Override
    public void onFileChooserDrop(FileChooserPanel fileChooserPanel) {
    }

    @Override
    public void onFileChooserOk(FileChooserPanel fileChooserPanel, File file) {
    }

    @Override
    public void onFileChooserPreSelect(FileChooserPanel fileChooserPanel) {
    }

    public void setJob(Job job) {
        mJob = job;
        JobExecuteSection executeSection = job.getExecuteSection();

        nameTextField.setText(mJob.getName());
        descriptionTextField.setText(mJob.getDescription());
        detailsTextArea.setText(mJob.getDetails());
        detailsTextArea.setCaretPosition(0);

        beforeHaltOnErrorCheckBox.setEnabled(executeSection.isBefore());
        beforeHaltOnErrorCheckBox.setSelected(executeSection.isBeforeHaltOnError());

        beforePanel.setSelected(executeSection.isBefore());
        beforePanel.setPath(executeSection.getBeforeCommand());
        beforePanel.setEnabled(beforePanel.isSelected());

        afterFailurePanel.setSelected(executeSection.isAfterFailure());
        afterFailurePanel.setPath(executeSection.getAfterFailureCommand());
        afterFailurePanel.setEnabled(afterFailurePanel.isSelected());

        afterSuccessPanel.setSelected(executeSection.isAfterSuccess());
        afterSuccessPanel.setPath(executeSection.getAfterSuccessCommand());
        afterSuccessPanel.setEnabled(afterSuccessPanel.isSelected());

        afterPanel.setSelected(executeSection.isAfter());
        afterPanel.setPath(executeSection.getAfterCommand());
        afterPanel.setEnabled(afterPanel.isSelected());

        cronPanel.setCronActive(job.isCronActive());
        cronPanel.setCronItems(job.getCronItems());

        if (StringUtils.isNotBlank(job.getColorBackground())) {
            previewButton.setBackground(Color.decode(job.getColorBackground()));
        }

        if (StringUtils.isNotBlank(job.getColorForeground())) {
            previewButton.setForeground(Color.decode(job.getColorForeground()));
        }

        int logMode = job.getLogMode();

        if (logMode == 0) {
            logAppendRadioButton.setSelected(true);
        } else if (logMode == 1) {
            logReplaceRadioButton.setSelected(true);
        } else if (logMode == 2) {
            logUniqueRadioButton.setSelected(true);
        }

        logOutputCheckBox.setSelected(mJob.isLogOutput());
        logErrorsCheckBox.setSelected(mJob.isLogErrors());
        logSeparateCheckBox.setSelected(mJob.isLogSeparateErrors());
    }

    private void init() {
        beforePanel.setButtonListener(this);

        nameTextField.requestFocus();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        logButtonGroup = new javax.swing.ButtonGroup();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionTextField = new javax.swing.JTextField();
        tabbedPane = new javax.swing.JTabbedPane();
        cronPanel = new se.trixon.jota.client.ui.editor.CronEditorPanel();
        runPanel = new javax.swing.JPanel();
        beforePanel = new se.trixon.util.swing.dialogs.FileChooserPanel();
        beforeHaltOnErrorCheckBox = new javax.swing.JCheckBox();
        afterFailurePanel = new se.trixon.util.swing.dialogs.FileChooserPanel();
        afterSuccessPanel = new se.trixon.util.swing.dialogs.FileChooserPanel();
        afterPanel = new se.trixon.util.swing.dialogs.FileChooserPanel();
        logPanel = new javax.swing.JPanel();
        logOutputCheckBox = new javax.swing.JCheckBox();
        logErrorsCheckBox = new javax.swing.JCheckBox();
        logSeparateCheckBox = new javax.swing.JCheckBox();
        logAppendRadioButton = new javax.swing.JRadioButton();
        logReplaceRadioButton = new javax.swing.JRadioButton();
        logUniqueRadioButton = new javax.swing.JRadioButton();
        appearancePanel = new javax.swing.JPanel();
        previewButton = new javax.swing.JButton();
        colorButton = new javax.swing.JButton();
        backgroundButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();
        detailsScrollPane = new javax.swing.JScrollPane();
        detailsTextArea = new javax.swing.JTextArea();

        nameLabel.setText(Dict.NAME.getString());

        descriptionLabel.setText(Dict.DESCRIPTION.getString());

        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);
        tabbedPane.addTab(Dict.SCHEDULE.getString(), cronPanel);

        beforePanel.setCheckBoxMode(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("se/trixon/jota/client/ui/editor/Bundle"); // NOI18N
        beforePanel.setHeader(bundle.getString("JobPanel.beforePanel.header")); // NOI18N

        beforeHaltOnErrorCheckBox.setText(Dict.STOP_ON_ERROR.toString());

        afterFailurePanel.setCheckBoxMode(true);
        afterFailurePanel.setHeader(bundle.getString("JobPanel.afterFailurePanel.header")); // NOI18N

        afterSuccessPanel.setCheckBoxMode(true);
        afterSuccessPanel.setHeader(bundle.getString("JobPanel.afterSuccessPanel.header")); // NOI18N

        afterPanel.setCheckBoxMode(true);
        afterPanel.setHeader(bundle.getString("JobPanel.afterPanel.header")); // NOI18N

        javax.swing.GroupLayout runPanelLayout = new javax.swing.GroupLayout(runPanel);
        runPanel.setLayout(runPanelLayout);
        runPanelLayout.setHorizontalGroup(
            runPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(runPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(runPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(beforePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(runPanelLayout.createSequentialGroup()
                        .addComponent(beforeHaltOnErrorCheckBox)
                        .addGap(0, 370, Short.MAX_VALUE))
                    .addComponent(afterSuccessPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(afterFailurePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(afterPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        runPanelLayout.setVerticalGroup(
            runPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(runPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(beforePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(beforeHaltOnErrorCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(afterFailurePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(afterSuccessPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(afterPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab(Dict.RUN.toString(), runPanel);

        logOutputCheckBox.setText(Dict.LOG_OUTPUT.toString());

        logErrorsCheckBox.setText(Dict.LOG_ERRORS.toString());

        logSeparateCheckBox.setText(Dict.LOG_SEPARATE_ERRORS.toString());

        logButtonGroup.add(logAppendRadioButton);
        logAppendRadioButton.setText(Dict.APPEND.toString());

        logButtonGroup.add(logReplaceRadioButton);
        logReplaceRadioButton.setText(Dict.REPLACE.toString());

        logButtonGroup.add(logUniqueRadioButton);
        logUniqueRadioButton.setText(Dict.UNIQUE.toString());

        javax.swing.GroupLayout logPanelLayout = new javax.swing.GroupLayout(logPanel);
        logPanel.setLayout(logPanelLayout);
        logPanelLayout.setHorizontalGroup(
            logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(logPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logSeparateCheckBox)
                    .addComponent(logErrorsCheckBox)
                    .addComponent(logOutputCheckBox))
                .addGap(18, 18, 18)
                .addGroup(logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logAppendRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
                    .addComponent(logReplaceRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(logUniqueRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        logPanelLayout.setVerticalGroup(
            logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, logPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(logOutputCheckBox)
                    .addComponent(logAppendRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logErrorsCheckBox)
                    .addComponent(logReplaceRadioButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(logPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(logSeparateCheckBox)
                    .addComponent(logUniqueRadioButton))
                .addContainerGap(194, Short.MAX_VALUE))
        );

        tabbedPane.addTab(Dict.LOG.toString(), logPanel);

        previewButton.setText("Lorem Ipsum"); // NOI18N

        colorButton.setText(Dict.FOREGROUND.toString());
        colorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorButtonActionPerformed(evt);
            }
        });

        backgroundButton.setText(Dict.BACKGROUND.toString());
        backgroundButton.setEnabled(false);
        backgroundButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                backgroundButtonActionPerformed(evt);
            }
        });

        resetButton.setText(Dict.RESET.toString());
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout appearancePanelLayout = new javax.swing.GroupLayout(appearancePanel);
        appearancePanel.setLayout(appearancePanelLayout);
        appearancePanelLayout.setHorizontalGroup(
            appearancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(appearancePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(previewButton, javax.swing.GroupLayout.PREFERRED_SIZE, 148, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(colorButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(backgroundButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(resetButton)
                .addContainerGap())
        );
        appearancePanelLayout.setVerticalGroup(
            appearancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(appearancePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(appearancePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(previewButton)
                    .addComponent(resetButton)
                    .addComponent(backgroundButton)
                    .addComponent(colorButton))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab(Dict.APPEARANCE.toString(), appearancePanel);

        detailsTextArea.setColumns(20);
        detailsTextArea.setRows(5);
        detailsScrollPane.setViewportView(detailsTextArea);

        tabbedPane.addTab(Dict.NOTE.toString(), detailsScrollPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(nameTextField)
                    .addComponent(descriptionTextField)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(nameLabel)
                            .addComponent(descriptionLabel))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 327, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        previewButton.setBackground(null);
        previewButton.setForeground(null);
    }//GEN-LAST:event_resetButtonActionPerformed

    private void backgroundButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_backgroundButtonActionPerformed
        SimpleDialog.setParent(this);
        previewButton.setBackground(SimpleDialog.selectColor(previewButton.getBackground()));
    }//GEN-LAST:event_backgroundButtonActionPerformed

    private void colorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorButtonActionPerformed
        SimpleDialog.setParent(this);
        previewButton.setForeground(SimpleDialog.selectColor(previewButton.getForeground()));
    }//GEN-LAST:event_colorButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private se.trixon.util.swing.dialogs.FileChooserPanel afterFailurePanel;
    private se.trixon.util.swing.dialogs.FileChooserPanel afterPanel;
    private se.trixon.util.swing.dialogs.FileChooserPanel afterSuccessPanel;
    private javax.swing.JPanel appearancePanel;
    private javax.swing.JButton backgroundButton;
    private javax.swing.JCheckBox beforeHaltOnErrorCheckBox;
    private se.trixon.util.swing.dialogs.FileChooserPanel beforePanel;
    private javax.swing.JButton colorButton;
    private se.trixon.jota.client.ui.editor.CronEditorPanel cronPanel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextField descriptionTextField;
    private javax.swing.JScrollPane detailsScrollPane;
    private javax.swing.JTextArea detailsTextArea;
    private javax.swing.JRadioButton logAppendRadioButton;
    private javax.swing.ButtonGroup logButtonGroup;
    private javax.swing.JCheckBox logErrorsCheckBox;
    private javax.swing.JCheckBox logOutputCheckBox;
    private javax.swing.JPanel logPanel;
    private javax.swing.JRadioButton logReplaceRadioButton;
    private javax.swing.JCheckBox logSeparateCheckBox;
    private javax.swing.JRadioButton logUniqueRadioButton;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton previewButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JPanel runPanel;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
