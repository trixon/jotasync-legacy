/* 
 * Copyright 2019 Patrik Karlström.
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
package se.trixon.jota.client.ui_swing.editor.module;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JOptionPane;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import se.trixon.almond.util.AlmondOptions;
import se.trixon.almond.util.AlmondUI;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.IconColor;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.almond.util.swing.dialogs.Message;
import se.trixon.jota.client.ui_swing.editor.module.task.OptionHandler;
import se.trixon.jota.client.ui_swing.editor.module.task.RsyncOption;

/**
 *
 * @author Patrik Karlström
 */
public class DualListPanel extends javax.swing.JPanel {

    private static final int ICON_SIZE = AlmondUI.ICON_SIZE_NORMAL;
    private final AlmondOptions mAlmondOptions = AlmondOptions.getInstance();

    /**
     * Creates new form DualListPanel
     */
    public DualListPanel() {
        initComponents();
        init();
        initListeners();
    }

    public ListPanel getAvailableListPanel() {
        return availableListPanel;
    }

    public ListPanel getSelectedListPanel() {
        return selectedListPanel;
    }

    private void activate() {
        for (int index : availableListPanel.getList().getSelectedIndices()) {
            OptionHandler optionHandler = (OptionHandler) availableListPanel.getFilteredModel().elementAt(index);
            if (optionHandler.getLongArg().contains("=")) {
                String input = requestArg(optionHandler);
                if (input != null) {
                    selectedListPanel.getModel().addElement(optionHandler);
                    availableListPanel.getModel().removeElement(optionHandler);
                    optionHandler.setDynamicArg(input);
                }
            } else {
                selectedListPanel.getModel().addElement(optionHandler);
                availableListPanel.getModel().removeElement(optionHandler);
            }
        }

        selectedListPanel.updateModel();
        availableListPanel.updateModel();
    }

    private void deactivate() {
        for (int index : selectedListPanel.getList().getSelectedIndices()) {
            OptionHandler optionHandler = (OptionHandler) selectedListPanel.getFilteredModel().elementAt(index);
            availableListPanel.getModel().addElement(optionHandler);
            selectedListPanel.getModel().removeElement(optionHandler);
            optionHandler.setDynamicArg(null);
        }

        selectedListPanel.updateModel();
        availableListPanel.updateModel();
    }

    private void init() {
        IconColor iconColor = mAlmondOptions.getIconColor();

        removeAllButton.setIcon(MaterialIcon._Content.CLEAR.get(ICON_SIZE, iconColor));
        activateButton.setIcon(MaterialIcon._Navigation.ARROW_BACK.get(ICON_SIZE, iconColor));
        deactivateButton.setIcon(MaterialIcon._Navigation.ARROW_FORWARD.get(ICON_SIZE, iconColor));
    }

    private void initListeners() {
        availableListPanel.getList().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2) {
                    activate();
                }
            }
        });

        selectedListPanel.getList().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent evt) {
                if (evt.getButton() == MouseEvent.BUTTON1 && evt.getClickCount() == 2) {
                    deactivate();
                }
            }
        });
    }

    private String requestArg(OptionHandler optionHandler) {
        String input = JOptionPane.showInputDialog(this, optionHandler.getLongArg(), optionHandler.getTitle(), JOptionPane.PLAIN_MESSAGE);

        if (input != null) {
            input = input.trim();
            boolean invalidInput = StringUtils.isBlank(input);

            String[] intKeys = {"num", "port", "rate", "seconds", "size"};
            String argType = StringUtils.split(optionHandler.getLongArg(), "=", 2)[1].toLowerCase();

            boolean shouldBeInt = ArrayUtils.contains(intKeys, argType);

            if (shouldBeInt) {
                try {
                    Integer.parseInt(input);
                } catch (NullPointerException | NumberFormatException e) {
                    invalidInput = true;
                }
            } else if (optionHandler instanceof RsyncOption && optionHandler == RsyncOption.CHOWN) {
                invalidInput = input.startsWith(":")
                        || input.endsWith(":")
                        || StringUtils.countMatches(input, ":") != 1;
            }

            if (invalidInput) {
                Message.error(this, "Invalid input", "try again");
                input = requestArg(optionHandler);
            } else {
                return input;
            }
        }

        return input;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        selectedListPanel = new se.trixon.jota.client.ui_swing.editor.module.ListPanel();
        toolBar = new javax.swing.JToolBar();
        filler1 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        activateButton = new javax.swing.JButton();
        deactivateButton = new javax.swing.JButton();
        removeAllButton = new javax.swing.JButton();
        filler2 = new javax.swing.Box.Filler(new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 0), new java.awt.Dimension(0, 32767));
        availableListPanel = new se.trixon.jota.client.ui_swing.editor.module.ListPanel();

        setLayout(new java.awt.GridBagLayout());

        selectedListPanel.setHeader(Dict.SELECTED.toString());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(selectedListPanel, gridBagConstraints);

        toolBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        toolBar.setFloatable(false);
        toolBar.setOrientation(javax.swing.SwingConstants.VERTICAL);
        toolBar.setRollover(true);
        toolBar.add(filler1);

        activateButton.setToolTipText(Dict.ACTIVATE.toString());
        activateButton.setFocusable(false);
        activateButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        activateButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        activateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activateButtonActionPerformed(evt);
            }
        });
        toolBar.add(activateButton);

        deactivateButton.setToolTipText(Dict.DEACTIVATE.toString());
        deactivateButton.setFocusable(false);
        deactivateButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        deactivateButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        deactivateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deactivateButtonActionPerformed(evt);
            }
        });
        toolBar.add(deactivateButton);

        removeAllButton.setToolTipText(Dict.REMOVE_ALL.toString());
        removeAllButton.setFocusable(false);
        removeAllButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        removeAllButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        removeAllButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                removeAllButtonActionPerformed(evt);
            }
        });
        toolBar.add(removeAllButton);
        toolBar.add(filler2);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        add(toolBar, gridBagConstraints);

        availableListPanel.setHeader(Dict.AVAILABLE.toString());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(availableListPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void activateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activateButtonActionPerformed
        activate();
    }//GEN-LAST:event_activateButtonActionPerformed

    private void deactivateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deactivateButtonActionPerformed
        deactivate();
    }//GEN-LAST:event_deactivateButtonActionPerformed

    private void removeAllButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_removeAllButtonActionPerformed
        for (Object object : selectedListPanel.getModel().toArray()) {
            availableListPanel.getModel().addElement(object);
            ((OptionHandler) object).setDynamicArg(null);
        }

        selectedListPanel.getModel().clear();
        selectedListPanel.updateModel();
        availableListPanel.updateModel();
    }//GEN-LAST:event_removeAllButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton activateButton;
    private se.trixon.jota.client.ui_swing.editor.module.ListPanel availableListPanel;
    private javax.swing.JButton deactivateButton;
    private javax.swing.Box.Filler filler1;
    private javax.swing.Box.Filler filler2;
    private javax.swing.JButton removeAllButton;
    private se.trixon.jota.client.ui_swing.editor.module.ListPanel selectedListPanel;
    private javax.swing.JToolBar toolBar;
    // End of variables declaration//GEN-END:variables
}
