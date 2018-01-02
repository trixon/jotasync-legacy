/*
 * Copyright 2018 Patrik Karlsson.
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
import javax.swing.JFileChooser;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.dialogs.FileChooserPanel;
import se.trixon.jota.shared.task.Task;

/**
 *
 * @author Patrik Karlsson
 */
public class TaskSourceDestPanel extends TaskModule implements FileChooserPanel.FileChooserButtonListener {

    /**
     * Creates new form TaskBasicPanel
     */
    public TaskSourceDestPanel() {
        initComponents();
        init();
    }

    @Override
    public void loadTask(Task task) {
        sourcePanel.setPath(task.getSource());
        destinationPanel.setPath(task.getDestination());
        noAdditionalDirCheckBox.setSelected(task.isNoAdditionalDir());

        noAdditionalDirUpdate(noAdditionalDirCheckBox.isSelected());
    }

    @Override
    public void onFileChooserCancel(FileChooserPanel fileChooserPanel) {
        // nvm
    }

    @Override
    public void onFileChooserCheckBoxChange(FileChooserPanel fileChooserPanel, boolean isSelected) {
        // nvm
    }

    @Override
    public void onFileChooserDrop(FileChooserPanel fileChooserPanel) {
        // nvm
    }

    @Override
    public void onFileChooserOk(FileChooserPanel fileChooserPanel, File file) {
        if (fileChooserPanel == sourcePanel) {
            noAdditionalDirUpdate(noAdditionalDirCheckBox.isSelected());
        }
    }

    @Override
    public void onFileChooserPreSelect(FileChooserPanel fileChooserPanel) {
        // nvm
    }

    @Override
    public Task saveTask(Task task) {
        noAdditionalDirUpdate(noAdditionalDirCheckBox.isSelected());
        task.setSource(sourcePanel.getPath());
        task.setDestination(destinationPanel.getPath());

        task.setNoAdditionalDir(noAdditionalDirCheckBox.isSelected());

        return task;
    }

    private void noAdditionalDirUpdate(boolean selected) {
        String path = sourcePanel.getPath();

        while (path.endsWith("/")) {
            path = StringUtils.removeEnd(path, "/");
        }

        if (selected) {
            path = StringUtils.appendIfMissing(path, "/");
        }

        sourcePanel.setPath(path);
    }

    private void init() {
        mTitle = Dict.SOURCE_AND_DEST.toString();
        sourcePanel.setMode(JFileChooser.DIRECTORIES_ONLY);
        destinationPanel.setMode(JFileChooser.DIRECTORIES_ONLY);
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        sourcePanel = new se.trixon.almond.util.swing.dialogs.FileChooserPanel();
        destinationPanel = new se.trixon.almond.util.swing.dialogs.FileChooserPanel();
        noAdditionalDirCheckBox = new javax.swing.JCheckBox();
        swapSourceDestButton = new javax.swing.JButton();
        fillPanel = new javax.swing.JPanel();

        setAlignmentX(0.0F);
        setLayout(new java.awt.GridBagLayout());

        sourcePanel.setHeader(Dict.SOURCE.getString());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        add(sourcePanel, gridBagConstraints);

        destinationPanel.setHeader(Dict.DESTINATION.getString());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        add(destinationPanel, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("se/trixon/jota/client/ui/editor/module/task/Bundle"); // NOI18N
        noAdditionalDirCheckBox.setText(bundle.getString("TaskPanel.forceSourceSlashCheckBox.text")); // NOI18N
        noAdditionalDirCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                noAdditionalDirCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        add(noAdditionalDirCheckBox, gridBagConstraints);

        swapSourceDestButton.setText(bundle.getString("TaskPanel.swapSourceDestButton.text")); // NOI18N
        swapSourceDestButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                swapSourceDestButtonActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_END;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        add(swapSourceDestButton, gridBagConstraints);

        javax.swing.GroupLayout fillPanelLayout = new javax.swing.GroupLayout(fillPanel);
        fillPanel.setLayout(fillPanelLayout);
        fillPanelLayout.setHorizontalGroup(
            fillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        fillPanelLayout.setVerticalGroup(
            fillPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(fillPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void swapSourceDestButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_swapSourceDestButtonActionPerformed
        String tempPath = sourcePanel.getPath();
        sourcePanel.setPath(destinationPanel.getPath());
        destinationPanel.setPath(tempPath);

        noAdditionalDirUpdate(noAdditionalDirCheckBox.isSelected());
    }//GEN-LAST:event_swapSourceDestButtonActionPerformed

    private void noAdditionalDirCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_noAdditionalDirCheckBoxActionPerformed
        noAdditionalDirUpdate(noAdditionalDirCheckBox.isSelected());
    }//GEN-LAST:event_noAdditionalDirCheckBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private se.trixon.almond.util.swing.dialogs.FileChooserPanel destinationPanel;
    private javax.swing.JPanel fillPanel;
    private javax.swing.JCheckBox noAdditionalDirCheckBox;
    private se.trixon.almond.util.swing.dialogs.FileChooserPanel sourcePanel;
    private javax.swing.JButton swapSourceDestButton;
    // End of variables declaration//GEN-END:variables
}
