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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import org.apache.commons.lang3.SerializationUtils;
import se.trixon.jota.client.Manager;
import se.trixon.jota.shared.job.Job;
import se.trixon.util.BundleHelper;
import se.trixon.util.dictionary.Dict;
import se.trixon.util.swing.SwingHelper;
import se.trixon.util.swing.dialogs.Message;

/**
 *
 * @author Patrik Karlsson
 */
public final class JobsPanel extends EditPanel {

    private final ResourceBundle mBundle = BundleHelper.getBundle(JobsPanel.class, "Bundle");
    private final HashSet<JobsListener> mJobsListeners = new HashSet<>();
    private final Manager mManager = Manager.getInstance();

    public JobsPanel(long jobId, boolean openJob) {
        init();
        initListeners();

        for (Job job : getJobs()) {
            if (job.getId() == jobId) {
                SwingUtilities.invokeLater(() -> {
                    list.setSelectedValue(job, true);
                    if (openJob) {
                        edit(job);
                    }
                });
                break;
            }
        }
    }

    public boolean addJobsListener(JobsListener jobsListener) {
        return mJobsListeners.add(jobsListener);
    }

    public ArrayList<Job> getJobs() {
        ArrayList<Job> jobs = new ArrayList<>();

        for (Object object : getModel().toArray()) {
            jobs.add((Job) object);
        }

        return jobs;
    }

    public Job getSelectedJob() {
        return (Job) list.getSelectedValue();
    }

    @Override
    public void save() {
        try {
            Job[] jobs = new Job[getModel().size()];
            for (int i = 0; i < jobs.length; i++) {
                jobs[i] = (Job) getModel().get(i);
            }
            mManager.getServerCommander().setJobs(jobs);
        } catch (RemoteException ex) {
            Logger.getLogger(JobsPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void addButtonActionPerformed(ActionEvent evt) {
        edit(null);
    }

    private void cloneButtonActionPerformed(ActionEvent evt) {
        if (getSelectedJob() != null) {
            Job job = SerializationUtils.clone(getSelectedJob());
            long id = System.currentTimeMillis();
            job.setId(id);
            job.setName(String.format("%s_%d", job.getName(), id));
            job.setHistory("");
            getModel().addElement(job);
            sortModel();
            list.setSelectedValue(job, true);
        }
    }

    private void edit(Job job) {
        String title;
        boolean add = job == null;
        String type = Dict.JOB.toString().toLowerCase();
        if (job == null) {
            job = new Job();
            title = String.format("%s %s", Dict.ADD.toString(), type);
        } else {
            title = String.format("%s %s", Dict.EDIT.toString(), type);
        }

        JobPanel jobPanel = new JobPanel();
        jobPanel.setJob(job);
        SwingHelper.makeWindowResizable(jobPanel);
        int retval = JOptionPane.showOptionDialog(getRoot(),
                jobPanel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if (retval == JOptionPane.OK_OPTION) {
            Job modifiedJob = jobPanel.getJob();
            if (modifiedJob.isValid() && !jobExists(modifiedJob)) {
                if (add) {
                    getModel().addElement(modifiedJob);
                } else {
                    getModel().set(getModel().indexOf(getSelectedJob()), modifiedJob);
                }
                sortModel();
                list.setSelectedValue(modifiedJob, true);
            } else {
                showInvalidJobDialog();
                edit(modifiedJob);
            }
        }
    }

    private void editButtonActionPerformed(ActionEvent evt) {
        if (getSelectedJob() != null) {
            edit(getSelectedJob());
        }
    }

    private void init() {
        label.setText(Dict.JOBS.toString());

        addButton.setVisible(true);
        cloneButton.setVisible(true);
        editButton.setVisible(true);
        removeButton.setVisible(true);
        removeAllButton.setVisible(true);

        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        try {
            DefaultListModel model = new DefaultListModel();
            mManager.getServerCommander().getJobs().stream().forEach((job) -> {
                model.addElement(job);
            });
            setModel(model);
        } catch (RemoteException ex) {
            Logger.getLogger(JobsPanel.class.getName()).log(Level.SEVERE, null, ex);
        }
        list.setSelectedIndex(0);
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

    private boolean jobExists(Job job) {
        boolean result = false;

        for (Object object : getModel().toArray()) {
            Job existingJob = (Job) object;
            if (existingJob.getName().equalsIgnoreCase(job.getName()) && existingJob.getId() != job.getId()) {
                result = true;
                break;
            }
        }

        return result;
    }

    private void listMouseClicked(java.awt.event.MouseEvent evt) {
        if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2) {
            editButtonActionPerformed(null);
        }
    }

    private void notifyJobListenersAdded() {
        for (JobsListener jobsListener : mJobsListeners) {
            try {
                jobsListener.onJobAdded();
            } catch (Exception e) {
            }
        }
    }

    private void notifyJobListenersRemoved() {
        for (JobsListener jobsListener : mJobsListeners) {
            try {
                jobsListener.onJobRemoved();
            } catch (Exception e) {
            }
        }
    }

    private void removeAllButtonActionPerformed(ActionEvent evt) {
        if (!getModel().isEmpty()) {
            int retval = JOptionPane.showConfirmDialog(getRoot(),
                    mBundle.getString("JobsPanel.message.removeAll"),
                    mBundle.getString("JobsPanel.title.removeAll"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                getModel().removeAllElements();
                notifyJobListenersRemoved();
            }
        }
    }

    private void removeButtonActionPerformed(ActionEvent evt) {
        if (getSelectedJob() != null) {
            String message = String.format(mBundle.getString("JobsPanel.message.remove"), getSelectedJob().getName());
            int retval = JOptionPane.showConfirmDialog(getRoot(),
                    message,
                    mBundle.getString("JobsPanel.title.remove"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                getModel().removeElement(getSelectedJob());
                notifyJobListenersRemoved();
            }
        }
    }

    private void showInvalidJobDialog() {
        Message.error(getRoot(), Dict.INVALID_INPUT.toString(), mBundle.getString("JobsPanel.invalid"));
    }

    public interface JobsListener {

        public void onJobAdded();

        public void onJobRemoved();
    }
}
