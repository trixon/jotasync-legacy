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
package se.trixon.jotaclient.ui.editor;

import java.awt.Color;
import java.io.File;
import org.apache.commons.lang3.StringUtils;
import se.trixon.jota.job.Job;
import se.trixon.util.GraphicsHelper;
import se.trixon.util.swing.dialogs.FileChooserPanel;
import se.trixon.util.dictionary.Dict;
import se.trixon.util.swing.dialogs.SimpleDialog;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
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

        job.setRunAfterFailure(afterFailureFileChooserPanel.isSelected());
        job.setRunAfterFailureCommand(afterFailureFileChooserPanel.getPath().trim());

        job.setRunAfterSuccess(afterSuccessFileChooserPanel.isSelected());
        job.setRunAfterSuccessCommand(afterSuccessFileChooserPanel.getPath().trim());

        job.setRunBefore(beforeFileChooserPanel.isSelected());
        job.setRunBeforeCommand(beforeFileChooserPanel.getPath().trim());
        job.setRunBeforeHaltOnError(beforeHaltCheckBox.isSelected());

        job.setCronActive(cronPanel.isCronActive());
        job.setCronItems(cronPanel.getCronItems());

        if (!previewButton.getBackground().equals(resetButton.getBackground())) {
            job.setColorBackground(GraphicsHelper.colorToString(previewButton.getBackground()));
        }

        //if (!previewButton.getForeground().equals(resetButton.getForeground())) {
        job.setColorForeground(GraphicsHelper.colorToString(previewButton.getForeground()));
        //}

        return job;
    }

    @Override
    public void onFileChooserCancel(FileChooserPanel fileChooserPanel) {
    }

    @Override
    public void onFileChooserCheckBoxChange(FileChooserPanel fileChooserPanel, boolean isSelected) {
        if (fileChooserPanel == beforeFileChooserPanel) {
            beforeHaltCheckBox.setEnabled(isSelected);
        }

        if (beforeFileChooserPanel.isSelected() || afterFailureFileChooserPanel.isSelected() || afterSuccessFileChooserPanel.isSelected()) {
//            mNotificationLineSupport.setWarningMessage("External commands does not take arguments.");
        } else {
//            mNotificationLineSupport.clearMessages();
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
        nameTextField.setText(mJob.getName());
        descriptionTextField.setText(mJob.getDescription());
        detailsTextArea.setText(mJob.getDetails());
        detailsTextArea.setCaretPosition(0);

        afterFailureFileChooserPanel.setEnabled(mJob.isRunAfterFailure());
        afterFailureFileChooserPanel.setPath(mJob.getRunAfterFailureCommand());
        afterFailureFileChooserPanel.setSelected(mJob.isRunAfterFailure());

        afterSuccessFileChooserPanel.setEnabled(mJob.isRunAfterSuccess());
        afterSuccessFileChooserPanel.setPath(mJob.getRunAfterSuccessCommand());
        afterSuccessFileChooserPanel.setSelected(mJob.isRunAfterSuccess());

        beforeFileChooserPanel.setEnabled(mJob.isRunBefore());
        beforeFileChooserPanel.setPath(mJob.getRunBeforeCommand());
        beforeFileChooserPanel.setSelected(mJob.isRunBefore());
        beforeHaltCheckBox.setEnabled(mJob.isRunBefore());
        beforeHaltCheckBox.setSelected(mJob.isRunBeforeHaltOnError());

        cronPanel.setCronActive(job.isCronActive());
        cronPanel.setCronItems(job.getCronItems());

        if (StringUtils.isNotBlank(job.getColorBackground())) {
            previewButton.setBackground(Color.decode(job.getColorBackground()));
        }

        if (StringUtils.isNotBlank(job.getColorForeground())) {
            previewButton.setForeground(Color.decode(job.getColorForeground()));
        }
    }

    private void init() {
        beforeFileChooserPanel.setButtonListener(this);
        afterSuccessFileChooserPanel.setButtonListener(this);
        afterFailureFileChooserPanel.setButtonListener(this);

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

        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionTextField = new javax.swing.JTextField();
        tabbedPane = new javax.swing.JTabbedPane();
        cronPanel = new se.trixon.jotaclient.ui.editor.CronEditorPanel();
        runBeforePanel = new javax.swing.JPanel();
        beforeFileChooserPanel = new se.trixon.util.swing.dialogs.FileChooserPanel();
        beforeHaltCheckBox = new javax.swing.JCheckBox();
        runAfterPanel = new javax.swing.JPanel();
        afterSuccessFileChooserPanel = new se.trixon.util.swing.dialogs.FileChooserPanel();
        afterFailureFileChooserPanel = new se.trixon.util.swing.dialogs.FileChooserPanel();
        jPanel1 = new javax.swing.JPanel();
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

        beforeFileChooserPanel.setCheckBoxMode(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("se/trixon/jotaclient/ui/editor/Bundle"); // NOI18N
        beforeFileChooserPanel.setHeader(bundle.getString("JobPanel.beforeFileChooserPanel.header")); // NOI18N

        beforeHaltCheckBox.setText(bundle.getString("JobPanel.beforeHaltCheckBox.text")); // NOI18N

        javax.swing.GroupLayout runBeforePanelLayout = new javax.swing.GroupLayout(runBeforePanel);
        runBeforePanel.setLayout(runBeforePanelLayout);
        runBeforePanelLayout.setHorizontalGroup(
            runBeforePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(runBeforePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(runBeforePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(beforeFileChooserPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                    .addGroup(runBeforePanelLayout.createSequentialGroup()
                        .addComponent(beforeHaltCheckBox)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        runBeforePanelLayout.setVerticalGroup(
            runBeforePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(runBeforePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(beforeFileChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(beforeHaltCheckBox)
                .addContainerGap(41, Short.MAX_VALUE))
        );

        tabbedPane.addTab(Dict.RUN_BEFORE.getString(), runBeforePanel);

        afterSuccessFileChooserPanel.setCheckBoxMode(true);
        afterSuccessFileChooserPanel.setHeader(bundle.getString("JobPanel.afterSuccessFileChooserPanel.header")); // NOI18N

        afterFailureFileChooserPanel.setCheckBoxMode(true);
        afterFailureFileChooserPanel.setHeader(bundle.getString("JobPanel.afterFailureFileChooserPanel.header")); // NOI18N

        javax.swing.GroupLayout runAfterPanelLayout = new javax.swing.GroupLayout(runAfterPanel);
        runAfterPanel.setLayout(runAfterPanelLayout);
        runAfterPanelLayout.setHorizontalGroup(
            runAfterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(runAfterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(runAfterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(afterSuccessFileChooserPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 466, Short.MAX_VALUE)
                    .addComponent(afterFailureFileChooserPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        runAfterPanelLayout.setVerticalGroup(
            runAfterPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(runAfterPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(afterSuccessFileChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(afterFailureFileChooserPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        tabbedPane.addTab(Dict.RUN_AFTER.getString(), runAfterPanel);

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

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
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
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(previewButton)
                    .addComponent(resetButton)
                    .addComponent(backgroundButton)
                    .addComponent(colorButton))
                .addContainerGap(95, Short.MAX_VALUE))
        );

        tabbedPane.addTab(Dict.APPEARANCE.toString(), jPanel1);

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
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
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
                .addComponent(tabbedPane, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE))
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
    private se.trixon.util.swing.dialogs.FileChooserPanel afterFailureFileChooserPanel;
    private se.trixon.util.swing.dialogs.FileChooserPanel afterSuccessFileChooserPanel;
    private javax.swing.JButton backgroundButton;
    private se.trixon.util.swing.dialogs.FileChooserPanel beforeFileChooserPanel;
    private javax.swing.JCheckBox beforeHaltCheckBox;
    private javax.swing.JButton colorButton;
    private se.trixon.jotaclient.ui.editor.CronEditorPanel cronPanel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextField descriptionTextField;
    private javax.swing.JScrollPane detailsScrollPane;
    private javax.swing.JTextArea detailsTextArea;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton previewButton;
    private javax.swing.JButton resetButton;
    private javax.swing.JPanel runAfterPanel;
    private javax.swing.JPanel runBeforePanel;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
