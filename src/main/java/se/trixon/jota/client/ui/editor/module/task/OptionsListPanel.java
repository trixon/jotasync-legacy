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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import se.trixon.jota.client.ui.editor.EditPanel;
import se.trixon.jota.client.ui.editor.TasksPanel;
import se.trixon.util.BundleHelper;
import se.trixon.util.dictionary.Dict;
import se.trixon.util.swing.SwingHelper;
import se.trixon.util.swing.dialogs.Message;

/**
 *
 * @author Patrik Karlsson
 */
public class OptionsListPanel extends EditPanel {

    private final ResourceBundle mBundle = BundleHelper.getBundle(TasksPanel.class, "Bundle");

    public OptionsListPanel() {
        init();
        initListeners();
    }

    @Override
    public void save() {
    }

    private void addButtonActionPerformed(ActionEvent evt) {
        edit(null);
    }

    private void edit(RsyncOption option) {
        String title;
        boolean add = option == null;
        if (option == null) {
            //option = 
            title = Dict.ADD.getString();
        } else {
            title = Dict.EDIT.getString();
        }

        RsyncOptionPanel optionPanel = new RsyncOptionPanel();
        SwingHelper.makeWindowResizable(optionPanel);

        int retval = JOptionPane.showOptionDialog(getRoot(),
                optionPanel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if (retval == JOptionPane.OK_OPTION) {
            for (RsyncOption selectedItem : optionPanel.getSelectedItems()) {
                if (getModel().indexOf(selectedItem) == -1) {
                    getModel().addElement(selectedItem);

                }
            }
            sortModel();
            //list.setSelectedValue(modifiedOption, true);
//            Option modifiedOption = optionPanel.getOption();
//            if (modifiedOption.isValid()) {
//                if (add) {
//                    getModel().addElement(modifiedOption);
//                } else {
//                    getModel().set(getModel().indexOf(getSelectedOption()), modifiedOption);
//                }
//                
//                sortModel();
//                list.setSelectedValue(modifiedOption, true);
            //notifyTaskListenersChanged();
//            } else {
//                showInvalidTaskDialog();
//                edit(modifiedOption);
//            }
        }
    }

    private void editButtonActionPerformed(ActionEvent evt) {
        if (getSelectedOption() != null) {
            edit(getSelectedOption());
        }
    }

    private RsyncOption getSelectedOption() {
        return (RsyncOption) list.getSelectedValue();
    }

    private void init() {
        label.setVisible(false);

        addButton.setVisible(true);
        editButton.setVisible(true);
        removeButton.setVisible(true);
        removeAllButton.setVisible(true);

        setModel(getModel());
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
            int retval = JOptionPane.showConfirmDialog(getRoot(),
                    mBundle.getString("TasksPanel.message.removeAll"),
                    mBundle.getString("TasksPanel.title.removeAll"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                getModel().removeAllElements();
                //notifyTaskListenersChanged();
            }
        }
    }

    private void removeButtonActionPerformed(ActionEvent evt) {
        if (getSelectedOption() != null) {
            String message = String.format(mBundle.getString("TasksPanel.message.remove"), getSelectedOption().getDescription());
            int retval = JOptionPane.showConfirmDialog(getRoot(),
                    message,
                    mBundle.getString("TasksPanel.title.remove"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                getModel().removeElement(getSelectedOption());
                sortModel();
                //notifyTaskListenersChanged();
            }
        }
    }

    private void showInvalidTaskDialog() {
        Message.error(getRoot(), Dict.INVALID_INPUT.getString(), mBundle.getString("TasksPanel.invalid"));
    }
}
