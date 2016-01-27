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
package se.trixon.jota.client.ui.editor.module.task;

import javax.swing.DefaultListModel;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JTextField;

/**
 *
 * @author Patrik Karlsson
 */
public abstract class BaseOptionPanel extends javax.swing.JPanel {

    private final DefaultListModel mModel = new DefaultListModel();

    /**
     * Creates new form OptionPanel
     */
    public BaseOptionPanel() {
        initComponents();

        list.setModel(mModel);
    }

    public String getHeader() {
        return label.getText();
    }

    public JTextField getFilterTextField() {
        return filterTextField;
    }

    public JLabel getLabel() {
        return label;
    }

    public JList<String> getList() {
        return list;
    }

    public DefaultListModel getModel() {
        return mModel;
    }

    public void setHeader(String value) {
        label.setText(value);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        label = new javax.swing.JLabel();
        filterTextField = new javax.swing.JTextField();
        scrollPane = new javax.swing.JScrollPane();
        list = new javax.swing.JList<>();

        scrollPane.setViewportView(list);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(label)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(filterTextField)
            .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 26, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(label)
                .addGap(0, 0, 0)
                .addComponent(filterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(scrollPane, javax.swing.GroupLayout.DEFAULT_SIZE, 124, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField filterTextField;
    private javax.swing.JLabel label;
    private javax.swing.JList<String> list;
    private javax.swing.JScrollPane scrollPane;
    // End of variables declaration//GEN-END:variables
}
