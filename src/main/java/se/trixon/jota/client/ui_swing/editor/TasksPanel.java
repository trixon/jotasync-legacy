/* 
 * Copyright 2020 Patrik Karlström.
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
package se.trixon.jota.client.ui_swing.editor;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.SerializationUtils;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.swing.SwingHelper;
import se.trixon.almond.util.swing.dialogs.Message;
import se.trixon.jota.client.Manager;
import se.trixon.jota.shared.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class TasksPanel extends EditPanel {

    private final ResourceBundle mBundle = SystemHelper.getBundle(TasksPanel.class, "Bundle");
    private final Manager mManager = Manager.getInstance();
    private final HashSet<TasksListener> mTaskListeners = new HashSet<>();

    public TasksPanel() {
        init();
        initListeners();
    }

    public boolean addTasksListener(TasksListener tasksListener) {
        return mTaskListeners.add(tasksListener);
    }

    public ArrayList<Task> getTasks() {
        ArrayList<Task> tasks = new ArrayList<>();

        for (Object object : getModel().toArray()) {
            tasks.add((Task) object);
        }

        return tasks;
    }

    @Override
    public void save() {
        try {
            LinkedList<Task> tasks = new LinkedList<>();

            for (int i = 0; i < getModel().getSize(); i++) {
                tasks.add((Task) getModel().get(i));
            }
            mManager.getServerCommander().setTasks(tasks);
        } catch (RemoteException ex) {
            Logger.getLogger(JobsPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addButtonActionPerformed(ActionEvent evt) {
        edit(null);
    }

    private void cloneButtonActionPerformed(ActionEvent evt) {
        int[] indices = list.getSelectedIndices();

        if (indices.length > 0) {
            ArrayList<Task> clonedTasks = new ArrayList<>();
            for (int index : indices) {
                Task task = (Task) getModel().getElementAt(index);
                long id = System.currentTimeMillis();

                task = SerializationUtils.clone(task);

                task.setId(id);
                task.setName(String.format("%s_%d", task.getName(), id));
                clonedTasks.add(task);
            }

            for (Task task : clonedTasks) {
                getModel().addElement(task);
            }

            sortModel();

            for (int i = 0; i < indices.length; i++) {
                indices[i] = getModel().indexOf(clonedTasks.get(i));
            }

            list.setSelectedIndices(indices);
            edit(getSelectedTask());
        }
    }

    private void edit(Task task) {
        String title;
        boolean add = task == null;
        String type = Dict.TASK.toString().toLowerCase();
        if (task == null) {
            task = new Task();
            title = String.format("%s %s", Dict.ADD.toString(), type);
        } else {
            title = String.format("%s %s", Dict.EDIT.toString(), type);
        }

        TaskPanel taskPanel = new TaskPanel();
        taskPanel.setTask(task);
        SwingHelper.makeWindowResizable(taskPanel);

        int retval = JOptionPane.showOptionDialog(getRoot(),
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
                notifyTaskListenersChanged();
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
        label.setText(Dict.TASKS.toString());

        addButton.setVisible(true);
        cloneButton.setVisible(true);
        editButton.setVisible(true);
        removeButton.setVisible(true);
        removeAllButton.setVisible(true);

        try {
            DefaultListModel model = new DefaultListModel();
            mManager.getServerCommander().getTasks().stream().forEach((task) -> {
                model.addElement(task);
            });
            setModel(model);
        } catch (RemoteException ex) {
            Logger.getLogger(JobsPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void initListeners() {
        addButton.addActionListener(this::addButtonActionPerformed);
        cloneButton.addActionListener(this::cloneButtonActionPerformed);
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

    private void notifyTaskListenersChanged() {
        for (TasksListener tasksListener : mTaskListeners) {
            try {
                tasksListener.onTaskChanged();
            } catch (Exception e) {
            }
        }
    }

    private void removeAllButtonActionPerformed(ActionEvent evt) {
        if (!getModel().isEmpty()) {
            int retval = JOptionPane.showConfirmDialog(getRoot(),
                    mBundle.getString("TasksPanel.message.removeAll"),
                    mBundle.getString("TasksPanel.title.removeAll"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                getModel().removeAllElements();
                notifyTaskListenersChanged();
            }
        }
    }

    private void removeButtonActionPerformed(ActionEvent evt) {
        int[] indices = list.getSelectedIndices();

        if (indices.length > 0) {
            StringBuilder sb = new StringBuilder();
            for (int index : indices) {
                Task task = (Task) getModel().getElementAt(index);
                sb.append(task.getName()).append("\n");
            }

            String message = String.format(mBundle.getString("TasksPanel.message.remove"), sb.toString());
            int retval = JOptionPane.showConfirmDialog(getRoot(),
                    message,
                    mBundle.getString("TasksPanel.title.remove"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                for (int i = indices.length - 1; i >= 0; i--) {
                    getModel().removeElement(getModel().getElementAt(indices[i]));
                }

                sortModel();
                notifyTaskListenersChanged();
            }
        }
    }

    private void showInvalidTaskDialog() {
        Message.error(getRoot(), Dict.INVALID_INPUT.toString(), mBundle.getString("TasksPanel.invalid"));
    }

    public interface TasksListener {

        public void onTaskChanged();
    }
}
