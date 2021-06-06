/* 
 * Copyright 2021 Patrik Karlström.
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

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.DefaultListModel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import se.trixon.jota.shared.job.Job;
import se.trixon.jota.shared.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class EditorPanel extends JPanel implements JobsPanel.JobsListener, TasksPanel.TasksListener {

    private final ActiveTasksPanel mActiveTasksPanel;
    private final JobsPanel mJobsPanel;
    private final TasksPanel mTasksPanel;

    /**
     * Creates new form EditorPanel
     *
     * @param jobId
     * @param openJob
     */
    public EditorPanel(long jobId, boolean openJob) {
        mTasksPanel = new TasksPanel();
        mJobsPanel = new JobsPanel(jobId, openJob);
        mActiveTasksPanel = new ActiveTasksPanel();

        initComponents();
        init();
        initListeners();

        SwingUtilities.invokeLater(() -> {
            loadActiveTasks();
        });
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

    @Override
    public void onTaskChanged() {
        updateTasksInJobs();
        loadActiveTasks();
    }

    public void save() {
        mJobsPanel.save();
        mTasksPanel.save();
    }

    private void activateButtonActionPerformed(ActionEvent e) {
        if (mJobsPanel.getSelectedJob() != null) {
            List selectedItems = mTasksPanel.list.getSelectedValuesList();
            DefaultListModel activeTasks = mActiveTasksPanel.getModel();

            for (Object selectedItem : selectedItems) {
                Task task = (Task) selectedItem;
                boolean missing = true;

                for (Object object : activeTasks.toArray()) {
                    Task activeTask = (Task) object;
                    if (task.getId() == activeTask.getId()) {
                        missing = false;
                        break;
                    }
                }

                if (missing) {
                    activeTasks.addElement(task);
                }
            }
            mActiveTasksPanel.save();
        }
    }

    private void init() {
        mJobsPanel.addJobsListener(this);
        mTasksPanel.addTasksListener(this);
        add(mJobsPanel);
        add(mActiveTasksPanel);
        add(mTasksPanel);
        setPreferredSize(new Dimension(720, 480));
    }

    private void initListeners() {
        mActiveTasksPanel.activateButton.addActionListener(this::activateButtonActionPerformed);

        mJobsPanel.list.addListSelectionListener((ListSelectionEvent e) -> {
            loadActiveTasks();
        });

        mJobsPanel.list.addMouseListener(new MouseAdapter() {

            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON1) {
                    loadActiveTasks();
                }
            }
        });
    }

    private void updateTasksInJobs() {
        ArrayList<Task> existingTasks = mTasksPanel.getTasks();
        for (Job job : mJobsPanel.getJobs()) {
            ArrayList<Task> tasksToRemoveFromJob = new ArrayList<>();

            for (Task jobTask : job.getTasks()) {
                boolean exist = false;

                for (Task existingTask : existingTasks) {
                    if (jobTask.getId() == existingTask.getId()) {
                        exist = true;
                        break;
                    }
                }

                if (!exist) {
                    tasksToRemoveFromJob.add(jobTask);
                }
            }

            job.getTasks().removeAll(tasksToRemoveFromJob);
        }
    }

    private void loadActiveTasks() {
        mActiveTasksPanel.getModel().clear();
        if (!mJobsPanel.list.isSelectionEmpty()) {
            Job job = mJobsPanel.getSelectedJob();
            mActiveTasksPanel.setJob(job);
            for (Task task : job.getTasks()) {
                for (Object object : mTasksPanel.getModel().toArray()) {
                    Task existingTask = (Task) object;
                    if (task.getId() == existingTask.getId()) {
                        task.setName(existingTask.getName());
                        break;
                    }
                }
                mActiveTasksPanel.getModel().addElement(task);
            }
            mActiveTasksPanel.list.setModel(mActiveTasksPanel.getModel());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setLayout(new java.awt.GridLayout(1, 2));
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
