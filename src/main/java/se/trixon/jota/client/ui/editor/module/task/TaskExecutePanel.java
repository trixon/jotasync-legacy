/* 
 * Copyright 2017 Patrik Karlsson.
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
package se.trixon.jota.client.ui.editor.module.task;

import java.io.File;
import se.trixon.jota.shared.task.Task;
import se.trixon.jota.shared.task.TaskExecuteSection;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.dialogs.FileChooserPanel;

/**
 *
 * @author Patrik Karlsson
 */
public class TaskExecutePanel extends TaskModule implements FileChooserPanel.FileChooserButtonListener {

    /**
     * Creates new form ModulePanel
     */
    public TaskExecutePanel() {
        initComponents();
        init();
    }

    @Override
    public void loadTask(Task task) {
        TaskExecuteSection executeSection = task.getExecuteSection();

        beforePanel.setSelected(executeSection.isBefore());
        beforePanel.setPath(executeSection.getBeforeCommand());
        beforePanel.setEnabled(beforePanel.isSelected());
        beforeHaltOnErrorCheckBox.setEnabled(executeSection.isBefore());
        beforeHaltOnErrorCheckBox.setSelected(executeSection.isBeforeHaltOnError());

        afterFailurePanel.setSelected(executeSection.isAfterFailure());
        afterFailurePanel.setPath(executeSection.getAfterFailureCommand());
        afterFailurePanel.setEnabled(afterFailurePanel.isSelected());
        afterFailureHaltOnErrorCheckBox.setEnabled(executeSection.isAfterFailure());
        afterFailureHaltOnErrorCheckBox.setSelected(executeSection.isAfterFailureHaltOnError());

        afterSuccessPanel.setSelected(executeSection.isAfterSuccess());
        afterSuccessPanel.setPath(executeSection.getAfterSuccessCommand());
        afterSuccessPanel.setEnabled(afterSuccessPanel.isSelected());
        afterSuccessHaltOnErrorCheckBox.setEnabled(executeSection.isAfterSuccess());
        afterSuccessHaltOnErrorCheckBox.setSelected(executeSection.isAfterSuccessHaltOnError());

        afterPanel.setSelected(executeSection.isAfter());
        afterPanel.setPath(executeSection.getAfterCommand());
        afterPanel.setEnabled(afterPanel.isSelected());
        afterHaltOnErrorCheckBox.setEnabled(executeSection.isAfter());
        afterHaltOnErrorCheckBox.setSelected(executeSection.isAfterHaltOnError());

        jobHaltOnErrorCheckBox.setSelected(executeSection.isJobHaltOnError());
    }

    @Override
    public void onFileChooserCancel(FileChooserPanel fileChooserPanel) {
    }

    @Override
    public void onFileChooserCheckBoxChange(FileChooserPanel fileChooserPanel, boolean isSelected) {
        if (fileChooserPanel == beforePanel) {
            beforeHaltOnErrorCheckBox.setEnabled(isSelected);
        } else if (fileChooserPanel == afterFailurePanel) {
            afterFailureHaltOnErrorCheckBox.setEnabled(isSelected);
        } else if (fileChooserPanel == afterSuccessPanel) {
            afterSuccessHaltOnErrorCheckBox.setEnabled(isSelected);
        } else if (fileChooserPanel == afterPanel) {
            afterHaltOnErrorCheckBox.setEnabled(isSelected);
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

    @Override
    public Task saveTask(Task task) {
        TaskExecuteSection executeSection = task.getExecuteSection();

        executeSection.setBefore(beforePanel.isSelected());
        executeSection.setBeforeCommand(beforePanel.getPath());
        executeSection.setBeforeHaltOnError(beforeHaltOnErrorCheckBox.isSelected());

        executeSection.setAfterFailure(afterFailurePanel.isSelected());
        executeSection.setAfterFailureCommand(afterFailurePanel.getPath());
        executeSection.setAfterFailureHaltOnError(afterFailureHaltOnErrorCheckBox.isSelected());

        executeSection.setAfterSuccess(afterSuccessPanel.isSelected());
        executeSection.setAfterSuccessCommand(afterSuccessPanel.getPath());
        executeSection.setAfterSuccessHaltOnError(afterSuccessHaltOnErrorCheckBox.isSelected());

        executeSection.setAfter(afterPanel.isSelected());
        executeSection.setAfterCommand(afterPanel.getPath());
        executeSection.setAfterHaltOnError(afterHaltOnErrorCheckBox.isSelected());

        executeSection.setJobHaltOnError(jobHaltOnErrorCheckBox.isSelected());

        return task;
    }

    private void init() {
        mTitle = Dict.RUN.getString();

        beforePanel.setButtonListener(this);
        afterFailurePanel.setButtonListener(this);
        afterSuccessPanel.setButtonListener(this);
        afterPanel.setButtonListener(this);
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

        beforePanel = new se.trixon.almond.util.swing.dialogs.FileChooserPanel();
        beforeHaltOnErrorCheckBox = new javax.swing.JCheckBox();
        jSeparator1 = new javax.swing.JSeparator();
        afterFailurePanel = new se.trixon.almond.util.swing.dialogs.FileChooserPanel();
        afterFailureHaltOnErrorCheckBox = new javax.swing.JCheckBox();
        jSeparator2 = new javax.swing.JSeparator();
        afterSuccessPanel = new se.trixon.almond.util.swing.dialogs.FileChooserPanel();
        afterSuccessHaltOnErrorCheckBox = new javax.swing.JCheckBox();
        jSeparator3 = new javax.swing.JSeparator();
        afterPanel = new se.trixon.almond.util.swing.dialogs.FileChooserPanel();
        jSeparator4 = new javax.swing.JSeparator();
        afterHaltOnErrorCheckBox = new javax.swing.JCheckBox();
        jobHaltOnErrorCheckBox = new javax.swing.JCheckBox();
        jPanel1 = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        beforePanel.setCheckBoxMode(true);
        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("se/trixon/jota/client/ui/editor/module/task/Bundle"); // NOI18N
        beforePanel.setHeader(bundle.getString("TaskExecutePanel.beforePanel.header")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(beforePanel, gridBagConstraints);

        beforeHaltOnErrorCheckBox.setText(Dict.STOP_ON_ERROR.toString());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.weightx = 1.0;
        add(beforeHaltOnErrorCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(jSeparator1, gridBagConstraints);

        afterFailurePanel.setAlignmentX(0.0F);
        afterFailurePanel.setCheckBoxMode(true);
        afterFailurePanel.setHeader(bundle.getString("TaskExecutePanel.afterFailurePanel.header")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(afterFailurePanel, gridBagConstraints);

        afterFailureHaltOnErrorCheckBox.setText(Dict.STOP_ON_ERROR.toString());
        afterFailureHaltOnErrorCheckBox.setAlignmentY(0.0F);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(afterFailureHaltOnErrorCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(jSeparator2, gridBagConstraints);

        afterSuccessPanel.setAlignmentX(0.0F);
        afterSuccessPanel.setCheckBoxMode(true);
        afterSuccessPanel.setHeader(bundle.getString("TaskExecutePanel.afterSuccessPanel.header")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(afterSuccessPanel, gridBagConstraints);

        afterSuccessHaltOnErrorCheckBox.setText(Dict.STOP_ON_ERROR.toString());
        afterSuccessHaltOnErrorCheckBox.setAlignmentY(0.0F);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(afterSuccessHaltOnErrorCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 8;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(jSeparator3, gridBagConstraints);

        afterPanel.setAlignmentX(0.0F);
        afterPanel.setCheckBoxMode(true);
        afterPanel.setHeader(bundle.getString("TaskExecutePanel.afterPanel.header")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 9;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(afterPanel, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 11;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 6, 0);
        add(jSeparator4, gridBagConstraints);

        afterHaltOnErrorCheckBox.setText(Dict.STOP_ON_ERROR.toString());
        afterHaltOnErrorCheckBox.setAlignmentY(0.0F);
        afterHaltOnErrorCheckBox.setEnabled(false);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 10;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(afterHaltOnErrorCheckBox, gridBagConstraints);

        jobHaltOnErrorCheckBox.setText(bundle.getString("TaskExecutePanel.jobHaltOnErrorCheckBox.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 12;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        add(jobHaltOnErrorCheckBox, gridBagConstraints);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 13;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jPanel1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox afterFailureHaltOnErrorCheckBox;
    private se.trixon.almond.util.swing.dialogs.FileChooserPanel afterFailurePanel;
    private javax.swing.JCheckBox afterHaltOnErrorCheckBox;
    private se.trixon.almond.util.swing.dialogs.FileChooserPanel afterPanel;
    private javax.swing.JCheckBox afterSuccessHaltOnErrorCheckBox;
    private se.trixon.almond.util.swing.dialogs.FileChooserPanel afterSuccessPanel;
    private javax.swing.JCheckBox beforeHaltOnErrorCheckBox;
    private se.trixon.almond.util.swing.dialogs.FileChooserPanel beforePanel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JSeparator jSeparator1;
    private javax.swing.JSeparator jSeparator2;
    private javax.swing.JSeparator jSeparator3;
    private javax.swing.JSeparator jSeparator4;
    private javax.swing.JCheckBox jobHaltOnErrorCheckBox;
    // End of variables declaration//GEN-END:variables
}
