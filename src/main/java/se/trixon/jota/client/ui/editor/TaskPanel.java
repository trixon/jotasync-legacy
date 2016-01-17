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

import java.awt.Component;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JFileChooser;
import se.trixon.jota.client.ui.editor.module.Module;
import se.trixon.jota.client.ui.editor.module.TaskPersistor;
import se.trixon.jota.client.ui.editor.module.task.TaskEnvironmentPanel;
import se.trixon.jota.client.ui.editor.module.task.TaskExcludePanel;
import se.trixon.jota.client.ui.editor.module.task.TaskExecutePanel;
import se.trixon.jota.client.ui.editor.module.task.TaskIncludePanel;
import se.trixon.jota.client.ui.editor.module.task.TaskNotePanel;
import se.trixon.jota.client.ui.editor.module.task.TaskOptionsPanel;
import se.trixon.jota.shared.task.Task;
import se.trixon.jota.shared.task.TaskVerifier;
import se.trixon.util.dictionary.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class TaskPanel extends javax.swing.JPanel {
    
    private Task mTask = new Task();
    private Mode mMode;
    private final TaskNotePanel mNotePanel = new TaskNotePanel();
//    private final ModuleLogPanel mModuleLogPanel = new ModuleLogPanel();
    private final TaskEnvironmentPanel mEnvironmentPanel = new TaskEnvironmentPanel();
    private final TaskIncludePanel mIncludePanel = new TaskIncludePanel();
    private final TaskExcludePanel mExcludePanel = new TaskExcludePanel();
    private final TaskExecutePanel mExecutePanel = new TaskExecutePanel();
    private final TaskOptionsPanel mOptionsPanel = new TaskOptionsPanel();
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
        return mTask.build();
    }
    
    public String getCommandAsString() {
        getCommand();
        return mTask.getCommandAsString();
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
        String bundlePath = this.getClass().getPackage().getName().replace(".", "/") + "/Bundle";
        typeComboBox.setModel(new DefaultComboBoxModel(new String[]{
            ResourceBundle.getBundle(bundlePath).getString("TaskPanel.typeBackup.text"),
            ResourceBundle.getBundle(bundlePath).getString("TaskPanel.typeSync.text")}));
        
        addModulePanel(mOptionsPanel);
        addModulePanel(mExecutePanel);
        addModulePanel(mExcludePanel);
        addModulePanel(mNotePanel);

//        addModulePanel(mModuleIncludePanel);
//        addModulePanel(mModuleEnvironmentPanel);
//        addModulePanel(mModuleLogPanel);
        for (Component component : tabbedPane.getComponents()) {
            if (component instanceof Module) {
                Module modulePanel = (Module) component;
                //modulePanel.setBorder(new EmptyBorder(8, 8, 8, 8));
            }
        }
        
        sourcePanel.setMode(JFileChooser.FILES_AND_DIRECTORIES);
        destinationPanel.setMode(JFileChooser.FILES_AND_DIRECTORIES);
    }
    
    private Component addModulePanel(Module modulePanel) {
        return tabbedPane.add(modulePanel.getTitle(), modulePanel);
    }
    
    private void loadTask() {
        nameTextField.setText(mTask.getName());
        descriptionTextField.setText(mTask.getDescription());
        typeComboBox.setSelectedIndex(mTask.getType());
        
        sourcePanel.setPath(mTask.getSource());
        destinationPanel.setPath(mTask.getDestination());
        
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
        mTask.setType(typeComboBox.getSelectedIndex());
        
        mTask.setSource(sourcePanel.getPath());
        mTask.setDestination(destinationPanel.getPath());
        
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
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        topPanel = new javax.swing.JPanel();
        nameTypePanel = new javax.swing.JPanel();
        namePanel = new javax.swing.JPanel();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        descriptionPanel = new javax.swing.JPanel();
        descriptionLabel = new javax.swing.JLabel();
        descriptionTextField = new javax.swing.JTextField();
        sourceDestPanel = new javax.swing.JPanel();
        sourcePanel = new se.trixon.util.swing.dialogs.FileChooserPanel();
        destinationPanel = new se.trixon.util.swing.dialogs.FileChooserPanel();
        forceSourceSlashCheckBox = new javax.swing.JCheckBox();
        swapSourceDestButton = new javax.swing.JButton();
        typeComboBox = new javax.swing.JComboBox();
        tabbedPane = new javax.swing.JTabbedPane();

        topPanel.setLayout(new javax.swing.BoxLayout(topPanel, javax.swing.BoxLayout.PAGE_AXIS));

        nameTypePanel.setLayout(new java.awt.GridLayout(1, 0));

        nameLabel.setText(Dict.NAME.getString());

        javax.swing.GroupLayout namePanelLayout = new javax.swing.GroupLayout(namePanel);
        namePanel.setLayout(namePanelLayout);
        namePanelLayout.setHorizontalGroup(
            namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(namePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameTextField)
                    .addComponent(nameLabel))
                .addContainerGap())
        );
        namePanelLayout.setVerticalGroup(
            namePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(namePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(nameLabel)
                .addGap(0, 0, 0)
                .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        nameTypePanel.add(namePanel);

        descriptionLabel.setText(Dict.DESCRIPTION.getString());

        javax.swing.GroupLayout descriptionPanelLayout = new javax.swing.GroupLayout(descriptionPanel);
        descriptionPanel.setLayout(descriptionPanelLayout);
        descriptionPanelLayout.setHorizontalGroup(
            descriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(descriptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(descriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(descriptionPanelLayout.createSequentialGroup()
                        .addComponent(descriptionLabel)
                        .addGap(0, 196, Short.MAX_VALUE))
                    .addComponent(descriptionTextField))
                .addContainerGap())
        );
        descriptionPanelLayout.setVerticalGroup(
            descriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(descriptionPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(descriptionLabel)
                .addGap(0, 0, Short.MAX_VALUE)
                .addComponent(descriptionTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        nameTypePanel.add(descriptionPanel);

        topPanel.add(nameTypePanel);

        sourcePanel.setHeader(Dict.SOURCE.getString());

        destinationPanel.setHeader(Dict.DESTINATION.getString());

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("se/trixon/jota/client/ui/editor/Bundle"); // NOI18N
        forceSourceSlashCheckBox.setText(bundle.getString("TaskPanel.forceSourceSlashCheckBox.text")); // NOI18N

        swapSourceDestButton.setText(bundle.getString("TaskPanel.swapSourceDestButton.text")); // NOI18N
        swapSourceDestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                swapSourceDestButtonActionPerformed(evt);
            }
        });

        typeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Backup source to destination", "Synchronize source and destination" }));

        javax.swing.GroupLayout sourceDestPanelLayout = new javax.swing.GroupLayout(sourceDestPanel);
        sourceDestPanel.setLayout(sourceDestPanelLayout);
        sourceDestPanelLayout.setHorizontalGroup(
            sourceDestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceDestPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(sourceDestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sourcePanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(destinationPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(sourceDestPanelLayout.createSequentialGroup()
                        .addComponent(typeComboBox, 0, 202, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(forceSourceSlashCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(swapSourceDestButton)))
                .addContainerGap())
        );
        sourceDestPanelLayout.setVerticalGroup(
            sourceDestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(sourceDestPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(sourcePanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(destinationPanel, javax.swing.GroupLayout.PREFERRED_SIZE, 54, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(sourceDestPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(forceSourceSlashCheckBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(swapSourceDestButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );

        topPanel.add(sourceDestPanel);

        tabbedPane.addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                tabbedPaneStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(topPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(tabbedPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(topPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(tabbedPane)
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void tabbedPaneStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_tabbedPaneStateChanged
    }//GEN-LAST:event_tabbedPaneStateChanged

    private void swapSourceDestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_swapSourceDestButtonActionPerformed
        String tempPath = sourcePanel.getPath();
        sourcePanel.setPath(destinationPanel.getPath());
        destinationPanel.setPath(tempPath);
    }//GEN-LAST:event_swapSourceDestButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JTextField descriptionTextField;
    private se.trixon.util.swing.dialogs.FileChooserPanel destinationPanel;
    private javax.swing.JCheckBox forceSourceSlashCheckBox;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JPanel namePanel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JPanel nameTypePanel;
    private javax.swing.JPanel sourceDestPanel;
    private se.trixon.util.swing.dialogs.FileChooserPanel sourcePanel;
    private javax.swing.JButton swapSourceDestButton;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JPanel topPanel;
    private javax.swing.JComboBox typeComboBox;
    // End of variables declaration//GEN-END:variables

    private enum Mode {
        
        ANDVANCED, BASIC;
    }
}
