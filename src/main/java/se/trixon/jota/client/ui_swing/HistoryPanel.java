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
package se.trixon.jota.client.ui_swing;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.DefaultListModel;
import javax.swing.JList;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.Dict;
import se.trixon.jota.client.ClientOptions;
import se.trixon.jota.client.Manager;
import se.trixon.jota.shared.job.Job;
import se.trixon.jota.shared.task.Task;

/**
 *
 * @author Patrik Karlström
 */
public class HistoryPanel extends javax.swing.JPanel {

    private final Manager mManager = Manager.getInstance();
    private final HashMap<Long, ArrayList<String>> mHistoryMap = new HashMap<>();
    private final ClientOptions mOptions = ClientOptions.INSTANCE;

    /**
     * Creates new form LogViewerPanel
     */
    public HistoryPanel() {
        initComponents();
        init();
    }

    private ArrayList<String> getList(Long key) {
        if (!mHistoryMap.containsKey(key)) {
            ArrayList<String> list = new ArrayList<>();
            mHistoryMap.put(key, list);
        }

        return mHistoryMap.get(key);
    }

    private void init() {
        ListSelectionListener listSelectionListener = (ListSelectionEvent e) -> {
            if (!e.getValueIsAdjusting()) {
                logPanel.clear();
                JList list = (JList) e.getSource();
                HistoryItem historyItem = (HistoryItem) list.getSelectedValue();
                if (historyItem != null) {
                    logPanel.println(historyItem.getHistory());
                }
            }
        };

        jobList.addListSelectionListener(listSelectionListener);
        taskList.addListSelectionListener(listSelectionListener);
    }

    private void refresh() {
        String history = "";
        logPanel.clear();
        mHistoryMap.clear();

        try {
            history = mManager.getServerCommander().getHistory();
            String lines[] = history.split("\\r?\\n");

            for (String line : lines) {
                String[] lineItems = StringUtils.split(line, null, 2);
                Long key = Long.parseLong(lineItems[0]);
                if (mOptions.isDisplayDryRun() || (!mOptions.isDisplayDryRun() && !lineItems[1].endsWith(")"))) {
                    getList(key).add(lineItems[1]);
                }
            }

            LinkedList<Job> jobs = mManager.getServerCommander().getJobs();
            LinkedList<Task> tasks = mManager.getServerCommander().getTasks();

            ArrayList<HistoryItem> jobHistoryItems = new ArrayList<>();
            ArrayList<HistoryItem> taskHistoryItems = new ArrayList<>();

            for (Long key : mHistoryMap.keySet()) {
                boolean isJob = false;
                boolean isTask = false;

                for (Job job : jobs) {
                    if (job.getId() == key) {
                        HistoryItem historyItem = new HistoryItem(key, job.getName(), String.join("\n", mHistoryMap.get(key)));
                        jobHistoryItems.add(historyItem);
                        isJob = true;
                        break;
                    }
                }

                if (!isJob) {
                    for (Task task : tasks) {
                        if (task.getId() == key) {
                            HistoryItem historyItem = new HistoryItem(key, task.getName(), String.join("\n", mHistoryMap.get(key)));
                            taskHistoryItems.add(historyItem);
                            isTask = true;
                            break;
                        }
                    }
                }
            }

            jobHistoryItems.sort((HistoryItem o1, HistoryItem o2) -> o1.getName().compareTo(o2.getName()));
            taskHistoryItems.sort((HistoryItem o1, HistoryItem o2) -> o1.getName().compareTo(o2.getName()));

            DefaultListModel<HistoryItem> jobModel = new DefaultListModel<>();
            jobHistoryItems.forEach((historyItem) -> {
                jobModel.addElement(historyItem);
            });
            jobList.setModel(jobModel);

            DefaultListModel<HistoryItem> taskModel = new DefaultListModel<>();
            taskHistoryItems.forEach((historyItem) -> {
                taskModel.addElement(historyItem);
            });
            taskList.setModel(taskModel);
        } catch (RemoteException ex) {
            Logger.getLogger(HistoryPanel.class.getName()).log(Level.SEVERE, null, ex);
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

        tabbedPane = new javax.swing.JTabbedPane();
        jobScrollPane = new javax.swing.JScrollPane();
        jobList = new javax.swing.JList<>();
        taskScrollPane = new javax.swing.JScrollPane();
        taskList = new javax.swing.JList<>();
        logPanel = new se.trixon.almond.util.swing.LogPanel();

        addComponentListener(new java.awt.event.ComponentAdapter() {
            public void componentShown(java.awt.event.ComponentEvent evt) {
                formComponentShown(evt);
            }
        });
        setLayout(new java.awt.GridBagLayout());

        tabbedPane.setTabLayoutPolicy(javax.swing.JTabbedPane.SCROLL_TAB_LAYOUT);

        jobList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        jobScrollPane.setViewportView(jobList);

        tabbedPane.addTab(Dict.JOBS.toString(), jobScrollPane);

        taskList.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
        taskScrollPane.setViewportView(taskList);

        tabbedPane.addTab(Dict.TASKS.toString(), taskScrollPane);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 0.5;
        gridBagConstraints.weighty = 1.0;
        add(tabbedPane, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(logPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void formComponentShown(java.awt.event.ComponentEvent evt) {//GEN-FIRST:event_formComponentShown
        refresh();
    }//GEN-LAST:event_formComponentShown

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JList<HistoryItem> jobList;
    private javax.swing.JScrollPane jobScrollPane;
    private se.trixon.almond.util.swing.LogPanel logPanel;
    private javax.swing.JTabbedPane tabbedPane;
    private javax.swing.JList<HistoryItem> taskList;
    private javax.swing.JScrollPane taskScrollPane;
    // End of variables declaration//GEN-END:variables
}
