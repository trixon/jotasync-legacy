/* 
 * Copyright 2017 Patrik Karlsson.
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

import java.util.ArrayList;
import org.apache.commons.lang3.StringUtils;
import se.trixon.jota.shared.task.OptionSection;
import se.trixon.jota.shared.task.Task;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class TaskOptionsPanel extends TaskModule {

    /**
     * Creates new form TaskOptionsPanel
     */
    public TaskOptionsPanel() {
        initComponents();
        init();
    }

    @Override
    public void loadTask(Task task) {
        OptionSection optionSection = task.getOptionSection();
        String[] options = StringUtils.splitPreserveAllTokens(optionSection.getOptions(), " ");
        for (String option : options) {
            for (Object object : dualListPanel.getAvailableListPanel().getModel().toArray()) {
                OptionHandler optionHandler = (OptionHandler) object;
                if (option.contains("=")) {
                    String[] elements = StringUtils.split(option, "=", 2);
                    if (StringUtils.equals(elements[0], StringUtils.split(optionHandler.getArg(), "=", 2)[0])) {
                        optionHandler.setDynamicArg(elements[1]);
                        dualListPanel.getSelectedListPanel().getModel().addElement(object);
                        dualListPanel.getAvailableListPanel().getModel().removeElement(object);
                    }
                } else if (StringUtils.equals(option, optionHandler.getArg())) {
                    dualListPanel.getSelectedListPanel().getModel().addElement(object);
                    dualListPanel.getAvailableListPanel().getModel().removeElement(object);
                    break;
                }
            }
        }

        dualListPanel.getSelectedListPanel().updateModel();
        dualListPanel.getAvailableListPanel().updateModel();
    }

    @Override
    public Task saveTask(Task task) {
        OptionSection optionSection = task.getOptionSection();

        ArrayList<String> options = new ArrayList<>();

        for (Object object : dualListPanel.getSelectedListPanel().getModel().toArray()) {
            options.add(((OptionHandler) object).getArg());
        }

        optionSection.setOptions(StringUtils.join(options, " "));

        return task;
    }

    private void init() {
        mTitle = Dict.OPTIONS.toString();

        for (RsyncOption option : RsyncOption.values()) {
            option.setDynamicArg(null);
            dualListPanel.getAvailableListPanel().getModel().addElement(option);
        }

        dualListPanel.getAvailableListPanel().updateModel();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        dualListPanel = new se.trixon.jota.client.ui.editor.module.DualListPanel();

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dualListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 471, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(dualListPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private se.trixon.jota.client.ui.editor.module.DualListPanel dualListPanel;
    // End of variables declaration//GEN-END:variables
}
