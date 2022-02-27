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
package se.trixon.jota.client.ui.editor.module.job;

import se.trixon.almond.util.Dict;
import se.trixon.almond.util.swing.dialogs.SimpleDialog;
import se.trixon.jota.shared.job.Job;

/**
 *
 * @author Patrik Karlström
 */
public class JobMiscPanel extends JobModule {

    /**
     * Creates new form JobCron
     */
    public JobMiscPanel() {
        initComponents();
        init();
    }

    @Override
    public void loadJob(Job job) {
        int logMode = job.getLogMode();

        if (logMode == 0) {
            logAppendRadioButton.setSelected(true);
        } else if (logMode == 1) {
            logReplaceRadioButton.setSelected(true);
        } else if (logMode == 2) {
            logUniqueRadioButton.setSelected(true);
        }

        logOutputCheckBox.setSelected(job.isLogOutput());
        logErrorsCheckBox.setSelected(job.isLogErrors());
        logSeparateCheckBox.setSelected(job.isLogSeparateErrors());
    }

    @Override
    public Job saveJob(Job job) {
        int logMode = 0;
        if (logAppendRadioButton.isSelected()) {
            logMode = 0;
        } else if (logReplaceRadioButton.isSelected()) {
            logMode = 1;
        } else if (logUniqueRadioButton.isSelected()) {
            logMode = 2;
        }

        job.setLogOutput(logOutputCheckBox.isSelected());
        job.setLogErrors(logErrorsCheckBox.isSelected());
        job.setLogSeparateErrors(logSeparateCheckBox.isSelected());
        job.setLogMode(logMode);

        return job;
    }

    private void init() {
        mTitle = Dict.MISCELLANEOUS.toString();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        logButtonGroup = new javax.swing.ButtonGroup();
        loggingLabel = new javax.swing.JLabel();
        logOutputCheckBox = new javax.swing.JCheckBox();
        logErrorsCheckBox = new javax.swing.JCheckBox();
        logSeparateCheckBox = new javax.swing.JCheckBox();
        logAppendRadioButton = new javax.swing.JRadioButton();
        logReplaceRadioButton = new javax.swing.JRadioButton();
        logUniqueRadioButton = new javax.swing.JRadioButton();
        appearanceLabel = new javax.swing.JLabel();
        colorButton = new javax.swing.JButton();
        resetButton = new javax.swing.JButton();

        loggingLabel.setFont(loggingLabel.getFont().deriveFont(loggingLabel.getFont().getStyle() | java.awt.Font.BOLD, loggingLabel.getFont().getSize()+3));
        loggingLabel.setText(Dict.LOGGING.toString());

        logOutputCheckBox.setText(Dict.LOG_OUTPUT.toString());

        logErrorsCheckBox.setText(Dict.LOG_ERRORS.toString());

        logSeparateCheckBox.setText(Dict.LOG_SEPARATE_ERRORS.toString());

        logButtonGroup.add(logAppendRadioButton);
        logAppendRadioButton.setText(Dict.APPEND.toString());

        logButtonGroup.add(logReplaceRadioButton);
        logReplaceRadioButton.setText(Dict.REPLACE.toString());

        logButtonGroup.add(logUniqueRadioButton);
        logUniqueRadioButton.setText(Dict.UNIQUE.toString());

        appearanceLabel.setFont(appearanceLabel.getFont().deriveFont(appearanceLabel.getFont().getStyle() | java.awt.Font.BOLD, appearanceLabel.getFont().getSize()+3));
        appearanceLabel.setText(Dict.APPEARANCE.toString());

        colorButton.setText(Dict.FOREGROUND.toString());
        colorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                colorButtonActionPerformed(evt);
            }
        });

        resetButton.setText(Dict.RESET.toString());
        resetButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                resetButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(logSeparateCheckBox)
                            .addComponent(logErrorsCheckBox)
                            .addComponent(logOutputCheckBox))
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(logAppendRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, 432, Short.MAX_VALUE)
                            .addComponent(logReplaceRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(logUniqueRadioButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(loggingLabel)
                            .addComponent(appearanceLabel))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(colorButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(resetButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(loggingLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(logOutputCheckBox)
                    .addComponent(logAppendRadioButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(logErrorsCheckBox)
                    .addComponent(logReplaceRadioButton, javax.swing.GroupLayout.Alignment.TRAILING))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(logSeparateCheckBox)
                    .addComponent(logUniqueRadioButton))
                .addGap(18, 18, 18)
                .addComponent(appearanceLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(resetButton)
                    .addComponent(colorButton))
                .addContainerGap(78, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void colorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_colorButtonActionPerformed
        SimpleDialog.setParent(this);
        colorButton.setForeground(SimpleDialog.selectColor(colorButton.getForeground()));
    }//GEN-LAST:event_colorButtonActionPerformed

    private void resetButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_resetButtonActionPerformed
        colorButton.setForeground(null);
    }//GEN-LAST:event_resetButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel appearanceLabel;
    private javax.swing.JButton colorButton;
    private javax.swing.JRadioButton logAppendRadioButton;
    private javax.swing.ButtonGroup logButtonGroup;
    private javax.swing.JCheckBox logErrorsCheckBox;
    private javax.swing.JCheckBox logOutputCheckBox;
    private javax.swing.JRadioButton logReplaceRadioButton;
    private javax.swing.JCheckBox logSeparateCheckBox;
    private javax.swing.JRadioButton logUniqueRadioButton;
    private javax.swing.JLabel loggingLabel;
    private javax.swing.JButton resetButton;
    // End of variables declaration//GEN-END:variables
}
