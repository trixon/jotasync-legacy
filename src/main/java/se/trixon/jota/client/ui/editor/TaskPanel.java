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
package se.trixon.jota.client.ui.editor;

import java.awt.Component;
import java.awt.Container;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.border.EmptyBorder;
import se.trixon.almond.util.Dict;
import se.trixon.jota.client.ui.editor.module.Module;
import se.trixon.jota.client.ui.editor.module.TaskPersistor;
import se.trixon.jota.client.ui.editor.module.task.TaskEnvironmentPanel;
import se.trixon.jota.client.ui.editor.module.task.TaskExcludePanel;
import se.trixon.jota.client.ui.editor.module.task.TaskExecutePanel;
import se.trixon.jota.client.ui.editor.module.task.TaskIncludePanel;
import se.trixon.jota.client.ui.editor.module.task.TaskNotePanel;
import se.trixon.jota.client.ui.editor.module.task.TaskOptionsPanel;
import se.trixon.jota.client.ui.editor.module.task.TaskSourceDestPanel;
import se.trixon.jota.shared.task.Task;
import se.trixon.jota.shared.task.TaskVerifier;

/**
 *
 * @author Patrik Karlström
 */
public class TaskPanel extends javax.swing.JPanel {

    private Task mTask = new Task();
    private Mode mMode;
    private final TaskNotePanel mNotePanel = new TaskNotePanel();
    private final TaskEnvironmentPanel mEnvironmentPanel = new TaskEnvironmentPanel();
    private final TaskIncludePanel mIncludePanel = new TaskIncludePanel();
    private final TaskExcludePanel mExcludePanel = new TaskExcludePanel();
    private final TaskExecutePanel mExecutePanel = new TaskExecutePanel();
    private final TaskOptionsPanel mOptionsPanel = new TaskOptionsPanel();
    private final TaskSourceDestPanel mSourceDestPanel = new TaskSourceDestPanel();
    private TaskVerifier mTaskVerifier;

    /**
     * Creates new form TaskPanel
     */
    public TaskPanel() {
        initComponents();
        init();
    }

    public List<String> getCommand() {
        saveTask();
        return mTask.getCommand();
    }

    public Task getCommandBuilder() {
        return mTask;
    }

    public Task getTask() {
        saveTask();
        return mTask;
    }

//    public void setDialogDescriptor(DialogDescriptor dialogDescriptor) {
//        mDialogDescriptor = dialogDescriptor;
//        mDialogDescriptor.setButtonListener(new ActionListener() {
//
//            @Override
//            public void actionPerformed(ActionEvent e) {
//                Object[] addditionalOptions = mDialogDescriptor.getAdditionalOptions();
//
//                if (e.getSource() == addditionalOptions[0]) {
//                    mTaskVerifier = new TaskVerifier();
//                    saveTask();
//                    mTaskVerifier.verify(mTask);
//                }
//            }
//
//        });
//    }
    public void setTask(Task task) {
        mTask = task;
        loadTask();
    }

    private void init() {
        addModulePanel(mSourceDestPanel);
        addModulePanel(mExecutePanel);
        addModulePanel(mOptionsPanel);
        addModulePanel(mExcludePanel);
        addModulePanel(mNotePanel);
//        addModulePanel(mModuleIncludePanel);
//        addModulePanel(mModuleEnvironmentPanel);

        for (Component component : tabbedPane.getComponents()) {
            Container container = (Container) component;
            if (component instanceof Module && !(container.getComponent(0) instanceof JScrollPane)) {
                Module modulePanel = (Module) component;
                modulePanel.setBorder(new EmptyBorder(8, 8, 8, 8));
            }
        }
    }

    private Component addModulePanel(Module modulePanel) {
        return tabbedPane.add(modulePanel.getTitle(), modulePanel);
    }

    private void loadTask() {
        nameTextField.setText(mTask.getName());
        descriptionTextField.setText(mTask.getDescription());

        for (Component component : tabbedPane.getComponents()) {
            if (component instanceof TaskPersistor) {
                TaskPersistor persistor = (TaskPersistor) component;
                persistor.loadTask(mTask);
            }
        }
    }

    private void saveTask() {
        mTask.setName(nameTextField.getText());
        mTask.setDescription(descriptionTextField.getText());

        for (Component component : tabbedPane.getComponents()) {
            if (component instanceof TaskPersistor) {
                TaskPersistor persistor = (TaskPersistor) component;
                persistor.saveTask(mTask);
            }
        }
    }

    private boolean disableTab(Component tab) {
        try {
            tabbedPane.setEnabledAt(tabbedPane.indexOfComponent(tab), false);
            return true;
        } catch (IndexOutOfBoundsException e) {
            //Tab not found
            return false;
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

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });
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

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged

    }//GEN-LAST:event_tabbedPaneStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextField descriptionTextField;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    private enum Mode {

        ANDVANCED, BASIC;
    }
}
