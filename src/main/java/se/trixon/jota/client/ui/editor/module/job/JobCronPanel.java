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
package se.trixon.jota.client.ui.editor.module.job;

import se.trixon.jota.shared.job.Job;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class JobCronPanel extends JobModule {

    /**
     * Creates new form JobCron
     */
    public JobCronPanel() {
        initComponents();
        init();
    }

    @Override
    public void loadJob(Job job) {
        cronPanel.setCronActive(job.isCronActive());
        cronPanel.setCronItems(job.getCronItems());
    }

    @Override
    public Job saveJob(Job job) {
        job.setCronActive(cronPanel.isCronActive());
        job.setCronItems(cronPanel.getCronItems());

        return job;
    }

    private void init() {
        mTitle = Dict.SCHEDULER.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        cronPanel = new se.trixon.jota.client.ui.editor.module.job.CronEditorPanel();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(cronPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(cronPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 88, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private se.trixon.jota.client.ui.editor.module.job.CronEditorPanel cronPanel;
    // End of variables declaration//GEN-END:variables
}
