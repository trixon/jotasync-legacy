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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import se.trixon.jota.job.Job;
import se.trixon.jota.task.Task;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class EditorPanel extends javax.swing.JPanel implements JobsPanel.JobsListener {

    private final ActiveTasksPanel mActiveTasksPanel;
    private final JobsPanel mJobsPanel;
    private final TasksPanel mTasksPanel;
    private boolean mTasksSaveable = false;

    /**
     * Creates new form EditorPanel
     */
    public EditorPanel() {
        mTasksPanel = new TasksPanel();
        mJobsPanel = new JobsPanel();
        mActiveTasksPanel = new ActiveTasksPanel();

        initComponents();
        init();
        initListeners();
    }

    @Override
    public void onJobAdded() {
        loadActiveTasks();
    }

    @Override
    public void onJobRemoved() {
        mJobsPanel.list.setSelectedIndex(0);
        loadActiveTasks();
    }

    public void save() {
        mJobsPanel.save();
        mTasksPanel.save();
    }

    private void activateButtonActionPerformed(ActionEvent e) {
        System.out.println("activateButtonActionPerformed");
        System.out.println(e.toString());
        if (mJobsPanel.getSelectedJob() != null) {
            List selectedItems = mTasksPanel.list.getSelectedValuesList();
            System.out.println("num of selected tasks: " + selectedItems.size());
            DefaultListModel activeTasks = mActiveTasksPanel.getModel();
            System.out.println("num of active tasks: " + activeTasks.size());

            for (Object selectedItem : selectedItems) {
                Task task = (Task) selectedItem;
                if (!activeTasks.contains(task)) {
                    activeTasks.addElement(task);
                }
            }
        }
    }

    private void init() {
        mJobsPanel.addJobsListener(this);
        add(mJobsPanel);
        add(mActiveTasksPanel);
        add(mTasksPanel);
        loadActiveTasks();
        setPreferredSize(new Dimension(720, 480));
    }

    private void initListeners() {
        mActiveTasksPanel.activateButton.addActionListener(this::activateButtonActionPerformed);

        mJobsPanel.list.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    loadActiveTasks();
                }
            }

        });

        mActiveTasksPanel.getModel().addListDataListener(new ListDataListener() {

            @Override
            public void contentsChanged(ListDataEvent e) {
                saveActiveTasks();
            }

            @Override
            public void intervalAdded(ListDataEvent e) {
                saveActiveTasks();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                saveActiveTasks();
            }
        });

        mTasksPanel.getModel().addListDataListener(new ListDataListener() {

            @Override
            public void contentsChanged(ListDataEvent e) {
                loadActiveTasks();
            }

            @Override
            public void intervalAdded(ListDataEvent e) {
                loadActiveTasks();
            }

            @Override
            public void intervalRemoved(ListDataEvent e) {
                loadActiveTasks();
            }
        });
    }

    private void loadActiveTasks() {
        mTasksSaveable = false;
        mActiveTasksPanel.getModel().clear();
        if (!mJobsPanel.list.isSelectionEmpty()) {
            Job job = mJobsPanel.getSelectedJob();
            for (Task task : job.getTasks()) {
                mActiveTasksPanel.getModel().addElement(task);
            }
        }
        mTasksSaveable = true;
    }

    private void saveActiveTasks() {
        if (mTasksSaveable) {
            mJobsPanel.getSelectedJob().setTasks(mActiveTasksPanel.getModel());
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

        setLayout(new java.awt.GridLayout(1, 2));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
