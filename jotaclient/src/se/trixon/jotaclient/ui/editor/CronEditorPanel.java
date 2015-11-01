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

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import se.trixon.util.BundleHelper;
import se.trixon.util.dictionary.Dict;
import se.trixon.util.swing.SwingHelper;
import se.trixon.util.swing.dialogs.cron.CronPanel;

/**
 *
 * @author Patrik Karlsson
 */
public class CronEditorPanel extends EditPanel {

    private final ResourceBundle mBundle = BundleHelper.getBundle(CronEditorPanel.class, "Bundle");
    private Component mRoot;

    /**
     * Creates new form CronPanel
     */
    public CronEditorPanel() {
        init();
        initListeners();
    }

    public String getSelectedJob() {
        return (String) list.getSelectedValue();
    }

    public boolean isCronActive() {
        return toggleButton.isSelected();
    }

    @Override
    public void save() {
    }

    public void setCronActive(boolean value) {
        toggleButton.setSelected(value);
    }

    private void addButtonActionPerformed(ActionEvent evt) {
        edit(null);
    }

    private void edit(String job) {
        String title;
        boolean add = job == null;
        if (job == null) {
            //job = new Job();
            title = Dict.ADD.getString();
        } else {
            title = Dict.EDIT.getString();
        }

        CronPanel jobPanel = new CronPanel();
        jobPanel.setCronString(job);
        SwingHelper.makeWindowResizable(jobPanel);

        int retval = JOptionPane.showOptionDialog(mRoot,
                jobPanel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if (retval == JOptionPane.OK_OPTION) {
//            Job modifiedJob = jobPanel.getJob();
//            if (modifiedJob.isValid()) {
//                if (add) {
//                    getModel().addElement(modifiedJob);
//                } else {
//                    getModel().set(getModel().indexOf(getSelectedJob()), modifiedJob);
//                }
//                sortModel();
//                list.setSelectedValue(modifiedJob, true);
//            } else {
//                showInvalidJobDialog();
//                edit(modifiedJob);
//            }
        }
    }

    private void editButtonActionPerformed(ActionEvent evt) {
        if (getSelectedJob() != null) {
            edit(getSelectedJob());
        }
    }

    private void init() {
        mRoot = SwingUtilities.getRoot(this);

        label.setVisible(false);
        toggleButton.setVisible(true);
        addButton.setVisible(true);
        editButton.setVisible(true);
        removeButton.setVisible(true);
        removeAllButton.setVisible(true);
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        //setModel();
        list.setSelectedIndex(0);
    }

    private void initListeners() {
        addButton.addActionListener(this::addButtonActionPerformed);
        editButton.addActionListener(this::editButtonActionPerformed);
        removeButton.addActionListener(this::removeButtonActionPerformed);
        removeAllButton.addActionListener(this::removeAllButtonActionPerformed);

        list.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                listMouseClicked(evt);
            }
        });
    }

    private void listMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2) {
            editButtonActionPerformed(null);
        }
    }

    private void removeAllButtonActionPerformed(ActionEvent evt) {
        if (!getModel().isEmpty()) {
            int retval = JOptionPane.showConfirmDialog(mRoot,
                    mBundle.getString("JobsPanel.message.removeAll"),
                    mBundle.getString("JobsPanel.title.removeAll"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                getModel().removeAllElements();
            }
        }
    }

    private void removeButtonActionPerformed(ActionEvent evt) {
        if (getSelectedJob() != null) {
            String message = String.format(mBundle.getString("JobsPanel.message.remove"), getSelectedJob());
            int retval = JOptionPane.showConfirmDialog(mRoot,
                    message,
                    mBundle.getString("JobsPanel.title.remove"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                getModel().removeElement(getSelectedJob());
            }
        }
    }
}
