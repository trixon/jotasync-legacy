/* 
 * Copyright 2022 Patrik Karlström.
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

import java.awt.Component;
import se.trixon.almond.util.Dict;
import se.trixon.jota.client.ui.editor.module.JobPersistor;
import se.trixon.jota.client.ui.editor.module.Module;
import se.trixon.jota.client.ui.editor.module.job.JobCronPanel;
import se.trixon.jota.client.ui.editor.module.job.JobExecutePanel;
import se.trixon.jota.client.ui.editor.module.job.JobMiscPanel;
import se.trixon.jota.client.ui.editor.module.job.JobNotePanel;
import se.trixon.jota.shared.job.Job;

/**
 *
 * @author Patrik Karlström
 */
public class JobPanel extends javax.swing.JPanel {

    private final JobCronPanel mCronPanel = new JobCronPanel();
    private final JobExecutePanel mExecutePanel = new JobExecutePanel();
    private final JobMiscPanel mJobMiscPanel = new JobMiscPanel();
    private final JobNotePanel mNotePanel = new JobNotePanel();
    private Job mJob = new Job();

    /**
     * Creates new form JobPanel
     */
    public JobPanel() {
        initComponents();
        init();
    }

    public Job getJob() {
        saveJob();

        return mJob;
    }

    public void setJob(Job job) {
        mJob = job;
        loadJob();
        nameTextField.requestFocus();
    }

    private Component addModulePanel(Module modulePanel) {
        return tabbedPane.add(modulePanel.getTitle(), modulePanel);
    }

    private void init() {
        addModulePanel(mCronPanel);
        addModulePanel(mExecutePanel);
        addModulePanel(mJobMiscPanel);
        addModulePanel(mNotePanel);
    }

    private void loadJob() {
        nameTextField.setText(mJob.getName());
        descriptionTextField.setText(mJob.getDescription());

        for (Component component : tabbedPane.getComponents()) {
            if (component instanceof JobPersistor) {
                JobPersistor persistor = (JobPersistor) component;
                persistor.loadJob(mJob);
            }
        }
    }

    private void saveJob() {
        mJob.setName(nameTextField.getText());
        mJob.setDescription(descriptionTextField.getText());

        for (Component component : tabbedPane.getComponents()) {
            if (component instanceof JobPersistor) {
                JobPersistor persistor = (JobPersistor) component;
                persistor.saveJob(mJob);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        descriptionLabel = new javax.swing.JLabel();
        descriptionTextField = new javax.swing.JTextField();
        tabbedPane = new javax.swing.JTabbedPane();

        setLayout(new java.awt.GridBagLayout());

        nameLabel.setText(Dict.NAME.toString());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        add(nameLabel, gridBagConstraints);

        nameTextField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        add(nameTextField, gridBagConstraints);

        descriptionLabel.setText(Dict.DESCRIPTION.toString());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(descriptionLabel, gridBagConstraints);

        descriptionTextField.setColumns(20);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 4, 0, 0);
        add(descriptionTextField, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        add(tabbedPane, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextField descriptionTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables
}
