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
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import se.trixon.jota.shared.task.Task;
import se.trixon.jota.shared.task.TaskVerifier;
import se.trixon.jota.client.ui.editor.task_modules.ModuleNotePanel;
import se.trixon.jota.client.ui.editor.task_modules.ModuleEnvironmentPanel;
import se.trixon.jota.client.ui.editor.task_modules.ModuleExcludePanel;
import se.trixon.jota.client.ui.editor.task_modules.ModuleExecutePanel;
import se.trixon.jota.client.ui.editor.task_modules.ModuleIncludePanel;
import se.trixon.jota.client.ui.editor.task_modules.ModuleOptionsPanel;
import se.trixon.jota.client.ui.editor.task_modules.ModulePanel;
import se.trixon.util.dictionary.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class TaskPanel extends javax.swing.JPanel {

    private Task mTask = new Task();
    private Mode mMode;
    private final ModuleNotePanel mModuleNotePanel = new ModuleNotePanel();
//    private final ModuleLogPanel mModuleLogPanel = new ModuleLogPanel();
    private final ModuleEnvironmentPanel mModuleEnvironmentPanel = new ModuleEnvironmentPanel();
    private final ModuleIncludePanel mModuleIncludePanel = new ModuleIncludePanel();
    private final ModuleExcludePanel mModuleExcludePanel = new ModuleExcludePanel();
    private final ModuleExecutePanel mModuleExecutePanel = new ModuleExecutePanel();
    private final ModuleOptionsPanel mModuleOptionsPanel = new ModuleOptionsPanel();
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
        setMode(Mode.BASIC);
    }

    private void init() {
        String bundlePath = this.getClass().getPackage().getName().replace(".", "/") + "/Bundle";
        typeComboBox.setModel(new DefaultComboBoxModel(new String[]{
            ResourceBundle.getBundle(bundlePath).getString("TaskPanel.typeBackup.text"),
            ResourceBundle.getBundle(bundlePath).getString("TaskPanel.typeSync.text")}));

        addModulePanel(mModuleOptionsPanel);
        addModulePanel(mModuleExecutePanel);
        addModulePanel(mModuleExcludePanel);
        addModulePanel(mModuleNotePanel);

//        addModulePanel(mModuleIncludePanel);
//        addModulePanel(mModuleEnvironmentPanel);
//        addModulePanel(mModuleLogPanel);
        for (Component component : tabbedPane.getComponents()) {
            if (component instanceof ModulePanel) {
                ModulePanel modulePanel = (ModulePanel) component;
                //modulePanel.setBorder(new EmptyBorder(8, 8, 8, 8));
            }
        }

        sourcePanel.setMode(JFileChooser.FILES_AND_DIRECTORIES);
        destinationPanel.setMode(JFileChooser.FILES_AND_DIRECTORIES);
    }

    private Component addModulePanel(ModulePanel modulePanel) {
        return tabbedPane.add(modulePanel.getTitle(), modulePanel);
    }

    private void loadTask() {
        nameTextField.setText(mTask.getName());
        typeComboBox.setSelectedIndex(mTask.getType());

        sourcePanel.setPath(mTask.getSource());
        destinationPanel.setPath(mTask.getDestination());

        for (Component component : tabbedPane.getComponents()) {
            if (component instanceof ModulePanel) {
                ModulePanel modulePanel = (ModulePanel) component;
                modulePanel.loadTask(mTask);
            }
        }
    }

    private void saveTask() {
        mTask.setName(nameTextField.getText());
        mTask.setType(typeComboBox.getSelectedIndex());

        mTask.setSource(sourcePanel.getPath());
        mTask.setDestination(destinationPanel.getPath());

        for (Component component : tabbedPane.getComponents()) {
            if (component instanceof ModulePanel) {
                ModulePanel modulePanel = (ModulePanel) component;
                mTask = modulePanel.saveTask(mTask);
            }
        }
    }

    private void setMode(Mode mode) {
        mMode = mode;

        if (mMode == Mode.ANDVANCED) {
            modeButton.setText(Dict.BASIC.getString() + " <<");
        } else {
            modeButton.setText(Dict.ADVANCED.getString() + " >>");
        }

        tabbedPane.setVisible(mode == Mode.ANDVANCED);

        try {
            SwingUtilities.getWindowAncestor(this).pack();
        } catch (Exception e) {
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
        typePanel = new javax.swing.JPanel();
        typeComboBox = new javax.swing.JComboBox();
        typeLabel = new javax.swing.JLabel();
        sourceDestPanel = new javax.swing.JPanel();
        sourcePanel = new se.trixon.util.swing.dialogs.FileChooserPanel();
        destinationPanel = new se.trixon.util.swing.dialogs.FileChooserPanel();
        modeButton = new javax.swing.JButton();
        forceSourceSlashCheckBox = new javax.swing.JCheckBox();
        swapSourceDestButton = new javax.swing.JButton();
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

        typeComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Backup source to destination", "Synchronize source and destination" }));

        typeLabel.setText(Dict.TYPE.getString());

        javax.swing.GroupLayout typePanelLayout = new javax.swing.GroupLayout(typePanel);
        typePanel.setLayout(typePanelLayout);
        typePanelLayout.setHorizontalGroup(
            typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(typeComboBox, 0, 285, Short.MAX_VALUE)
                    .addGroup(typePanelLayout.createSequentialGroup()
                        .addComponent(typeLabel)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        typePanelLayout.setVerticalGroup(
            typePanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(typePanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(typeLabel)
                .addGap(0, 0, 0)
                .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, 0))
        );

        nameTypePanel.add(typePanel);

        topPanel.add(nameTypePanel);

        sourcePanel.setHeader(Dict.SOURCE.getString());

        destinationPanel.setHeader(Dict.DESTINATION.getString());

        modeButton.setText("Mode"); // NOI18N
        modeButton.setPreferredSize(new java.awt.Dimension(168, 0));
        modeButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modeButtonActionPerformed(evt);
            }
        });

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("se/trixon/jota/client/ui/editor/Bundle"); // NOI18N
        forceSourceSlashCheckBox.setText(bundle.getString("TaskPanel.forceSourceSlashCheckBox.text")); // NOI18N

        swapSourceDestButton.setText(bundle.getString("TaskPanel.swapSourceDestButton.text")); // NOI18N
        swapSourceDestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                swapSourceDestButtonActionPerformed(evt);
            }
        });

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
                        .addComponent(modeButton, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
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
                    .addComponent(modeButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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

    private void modeButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modeButtonActionPerformed
        if (mMode == Mode.ANDVANCED) {
            setMode(Mode.BASIC);
        } else {
            setMode(Mode.ANDVANCED);
        }
    }//GEN-LAST:event_modeButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private se.trixon.util.swing.dialogs.FileChooserPanel destinationPanel;
    private javax.swing.JCheckBox forceSourceSlashCheckBox;
    private javax.swing.JButton modeButton;
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
    private javax.swing.JLabel typeLabel;
    private javax.swing.JPanel typePanel;
    // End of variables declaration//GEN-END:variables

    private enum Mode {

        ANDVANCED, BASIC;
    }
}
