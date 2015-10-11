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
import javax.swing.SwingUtilities;
import se.trixon.jota.task.Task;
import se.trixon.jota.task.TaskManager;
import se.trixon.util.BundleHelper;
import se.trixon.util.dictionary.Dict;
import se.trixon.util.swing.SwingHelper;
import se.trixon.util.swing.dialogs.Message;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class TasksPanel extends EditPanel {

    private final ResourceBundle mBundle = BundleHelper.getBundle(TasksPanel.class, "Bundle");
    private Component mRoot;

    public TasksPanel() {
        init();
        initListeners();
    }

    @Override
    public void save() {
        TaskManager.INSTANCE.setTasks(getModel());
    }

    private void addButtonActionPerformed(ActionEvent evt) {
        edit(null);
    }

    private void edit(Task task) {
        String title;
        boolean add = task == null;
        if (task == null) {
            task = new Task();
            title = Dict.ADD.getString();
        } else {
            title = Dict.EDIT.getString();
        }

        TaskPanel taskPanel = new TaskPanel();
        taskPanel.setTask(task);
        SwingHelper.makeWindowResizable(taskPanel);

        int retval = JOptionPane.showOptionDialog(mRoot,
                taskPanel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if (retval == JOptionPane.OK_OPTION) {
            Task modifiedTask = taskPanel.getTask();
            if (modifiedTask.isValid()) {
                if (add) {
                    getModel().addElement(modifiedTask);
                } else {
                    getModel().set(getModel().indexOf(getSelectedTask()), modifiedTask);
                }
                sortModel();
                list.setSelectedValue(modifiedTask, true);
            } else {
                showInvalidTaskDialog();
                edit(modifiedTask);
            }
        }
    }

    private void editButtonActionPerformed(ActionEvent evt) {
        if (getSelectedTask() != null) {
            edit(getSelectedTask());
        }
    }

    private Task getSelectedTask() {
        return (Task) list.getSelectedValue();
    }

    private void init() {
        mRoot = SwingUtilities.getRoot(this);
        label.setText(Dict.TASKS_AVAILABLE.getString());

        addButton.setVisible(true);
        editButton.setVisible(true);
        removeButton.setVisible(true);
        removeAllButton.setVisible(true);

        setModel(TaskManager.INSTANCE.populateModel(getModel()));
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
                    mBundle.getString("TasksPanel.message.removeAll"),
                    mBundle.getString("TasksPanel.title.removeAll"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                getModel().removeAllElements();
            }
        }
    }

    private void removeButtonActionPerformed(ActionEvent evt) {
        if (getSelectedTask() != null) {
            String message = String.format(mBundle.getString("TasksPanel.message.remove"), getSelectedTask().getName());
            int retval = JOptionPane.showConfirmDialog(mRoot,
                    message,
                    mBundle.getString("TasksPanel.title.remove"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                getModel().removeElement(getSelectedTask());
                sortModel();
            }
        }
    }

    private void showInvalidTaskDialog() {
        Message.error(mRoot, Dict.INVALID_INPUT.getString(), mBundle.getString("TasksPanel.invalid"));
    }
}
