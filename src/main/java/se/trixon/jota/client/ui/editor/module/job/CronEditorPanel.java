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

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ResourceBundle;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import org.apache.commons.lang3.StringUtils;
import se.trixon.jota.client.ui.editor.EditPanel;
import se.trixon.util.BundleHelper;
import se.trixon.util.dictionary.Dict;
import se.trixon.util.swing.SwingHelper;
import se.trixon.util.swing.dialogs.Message;
import se.trixon.util.swing.dialogs.cron.CronPanel;

/**
 *
 * @author Patrik Karlsson
 */
public class CronEditorPanel extends EditPanel {

    private static final String CRON_ITEM_SEPARATOR = "|";
    private final ResourceBundle mBundle = BundleHelper.getBundle(CronEditorPanel.class, "Bundle");

    /**
     * Creates new form CronPanel
     */
    public CronEditorPanel() {
        init();
        initListeners();
    }

    public String getSelectedCronString() {
        return (String) list.getSelectedValue();
    }

    public boolean isCronActive() {
        return checkBox.isSelected();
    }

    @Override
    public void save() {
    }

    public void setCronActive(boolean value) {
        checkBox.setSelected(value);
        list.setEnabled(checkBox.isSelected());
    }

    String getCronItems() {
        return StringUtils.join(getModel().toArray(), CRON_ITEM_SEPARATOR);
    }

    void setCronItems(String cronItems) {
        for (String item : StringUtils.split(cronItems, CRON_ITEM_SEPARATOR)) {
            getModel().addElement(item);
        }
    }

    private void addButtonActionPerformed(ActionEvent evt) {
        edit(null);
    }

    private void edit(String cronString) {
        String title;
        boolean add = cronString == null;
        if (cronString == null) {
            title = Dict.ADD.getString();
        } else {
            title = Dict.EDIT.getString();
        }

        CronPanel cronPanel = new CronPanel();
        cronPanel.setCronString(cronString);
        SwingHelper.makeWindowResizable(cronPanel);

        int retval = JOptionPane.showOptionDialog(getRoot(),
                cronPanel,
                title,
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE,
                null,
                null,
                null);

        if (retval == JOptionPane.OK_OPTION) {
            String modifiedCronString = cronPanel.getCronString();
            if (cronPanel.isCronValid()) {
                if (add) {
                    getModel().addElement(modifiedCronString);
                } else {
                    getModel().set(getModel().indexOf(getSelectedCronString()), modifiedCronString);
                }
                sortModel();
                list.setSelectedValue(modifiedCronString, true);
            } else {
                Message.error(this, "Invalid cron string", modifiedCronString);
                edit(modifiedCronString);
            }
        }
    }

    private void editButtonActionPerformed(ActionEvent evt) {
        if (getSelectedCronString() != null) {
            edit(getSelectedCronString());
        }
    }

    private void init() {
        label.setVisible(false);
        addButton.setVisible(true);
        editButton.setVisible(true);
        removeButton.setVisible(true);
        removeAllButton.setVisible(true);
        checkBox.setVisible(true);
        checkBox.setText(Dict.ACTIVE.toString());
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        setModel(getModel());
        list.setSelectedIndex(0);
    }

    private void initListeners() {
        addButton.addActionListener(this::addButtonActionPerformed);
        editButton.addActionListener(this::editButtonActionPerformed);
        removeButton.addActionListener(this::removeButtonActionPerformed);
        removeAllButton.addActionListener(this::removeAllButtonActionPerformed);

        checkBox.addActionListener((java.awt.event.ActionEvent evt) -> {
            list.setEnabled(checkBox.isSelected());
        });

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
                    mBundle.getString("JobsPanel.message.removeAll"),
                    mBundle.getString("JobsPanel.title.removeAll"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                getModel().removeAllElements();
            }
        }
    }

    private void removeButtonActionPerformed(ActionEvent evt) {
        if (getSelectedCronString() != null) {
            String message = String.format(mBundle.getString("JobsPanel.message.remove"), getSelectedCronString());
            int retval = JOptionPane.showConfirmDialog(getRoot(),
                    message,
                    mBundle.getString("JobsPanel.title.remove"),
                    JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (retval == JOptionPane.OK_OPTION) {
                getModel().removeElement(getSelectedCronString());
            }
        }
    }
}
