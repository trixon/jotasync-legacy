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
package se.trixon.jota.client.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPopupMenu;
import javax.swing.JTabbedPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import se.trixon.almond.util.AlmondOptions;
import se.trixon.almond.util.AlmondUI;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.IconColor;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jota.client.ConnectionListener;
import se.trixon.jota.client.Manager;
import se.trixon.jota.shared.ProcessEvent;
import se.trixon.jota.shared.ServerEvent;
import se.trixon.jota.shared.ServerEventListener;
import se.trixon.jota.shared.job.Job;
import se.trixon.jota.shared.task.Task;

/**
 *
 * @author Patrik Karlsson
 */
public class TabHolder extends JTabbedPane implements ConnectionListener, ServerEventListener {

    private Action mCloseAction;
    private SpeedDialPanel mSpeedDialPanel;
    private HashMap<Long, TabItem> mJobMap = new HashMap<>();
    private final Manager mManager = Manager.getInstance();
    private MouseAdapter mMenuMouseAdapter;
    private Action mSaveAction;
    private final AlmondOptions mAlmondOptions = AlmondOptions.getInstance();
    private final ActionManager mActionManager = ActionManager.getInstance();

    /**
     * Creates new form ProgressPane
     */
    public TabHolder() {
        initComponents();
        init();
    }

    public SpeedDialPanel getSpeedDialPanel() {
        return mSpeedDialPanel;
    }

    @Override
    public void onConnectionConnect() {
        // nvm
    }

    @Override
    public void onConnectionDisconnect() {
        setSelectedComponent(mSpeedDialPanel);

        mJobMap.entrySet().stream().forEach((entry) -> {
            TabItem tabItem = entry.getValue();
            remove(tabItem);
        });

        mJobMap = new HashMap<>();
    }

    @Override
    public void onProcessEvent(ProcessEvent processEvent, Job job, Task task, Object object) {
        TabItem tabItem = getTabItem(job);

        switch (processEvent) {
            case STARTED:
                tabItem.start();
                setSelectedComponent(tabItem);
                updateTitle(job, "b");
                updateActionStates();
                break;
            case OUT:
            case ERR:
                tabItem.log(processEvent, (String) object);
                break;
            case CANCELED:
                tabItem.log(ProcessEvent.OUT, String.format("\n\n%s", Dict.JOB_INTERRUPTED.toString()));
                tabItem.enableSave();
                updateTitle(job, "i");
                updateActionStates();
                break;
            case FAILED:
                tabItem.log(ProcessEvent.OUT, String.format("\n\n%s", Dict.JOB_FAILED.toString()));
                tabItem.enableSave();
                updateTitle(job, "strike");
                updateActionStates();
                break;
            case FINISHED:
                if (object != null) {
                    tabItem.log(ProcessEvent.OUT, (String) object);
                }
                tabItem.enableSave();
                updateTitle(job, "normal");
                updateActionStates();
                break;
            default:
                break;
        }
    }

    @Override
    public void onServerEvent(ServerEvent serverEvent) {
    }

    private void displayTab(int index) {
        try {
            setSelectedIndex(index);
        } catch (IndexOutOfBoundsException e) {
            // nvm
        }
    }

    private synchronized void updateTitle(Job job, String format) {
        SwingUtilities.invokeLater(() -> {

            int index = indexOfComponent(getTabItem(job));
            try {
                setTitleAt(index, String.format("<html><%s>%s</%s></html>", format, job.getName(), format));
            } catch (Exception e) {
            }
        });
    }

    void closeTab() {
        if (getSelectedComponent() instanceof TabItem) {
            TabItem tabItem = (TabItem) getSelectedComponent();
            if (tabItem.isClosable()) {
                close(tabItem.getJob());
            }
        }
    }

    void saveTab() {
        if (getSelectedComponent() instanceof TabItem) {
            TabItem tabItem = (TabItem) getSelectedComponent();
            if (tabItem.isClosable()) {
                tabItem.save();
            }
        }
    }

    void initActions() {
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
        ActionMap actionMap = getRootPane().getActionMap();
        int commandMask = SystemHelper.getCommandMask();

        mCloseAction = mActionManager.getAction(ActionManager.CLOSE_TAB);
        mSaveAction = mActionManager.getAction(ActionManager.SAVE_TAB);

        for (int i = 0; i < 10; i++) {
            KeyStroke keyStroke = KeyStroke.getKeyStroke(0x31 + i, commandMask);
            String key = "key_" + i;
            final int tabIndex = i;
            AbstractAction action = new AbstractAction("Tab") {

                @Override
                public void actionPerformed(ActionEvent e) {
                    displayTab(tabIndex);
                }
            };
            inputMap.put(keyStroke, key);
            actionMap.put(key, action);
        }

        AbstractAction action = new AbstractAction("TabNext") {

            @Override
            public void actionPerformed(ActionEvent e) {
                displayTab(getSelectedIndex() + 1);
            }
        };

        KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, commandMask);
        String key = "nextTab";
        inputMap.put(keyStroke, key);
        actionMap.put(key, action);

        action = new AbstractAction("TabPrev") {

            @Override
            public void actionPerformed(ActionEvent e) {
                displayTab(Math.max(getSelectedIndex() - 1, 0));
            }

        };

        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_TAB, commandMask + InputEvent.SHIFT_MASK);
        key = "prevTab";
        inputMap.put(keyStroke, key);
        actionMap.put(key, action);

        mActionManager.addAppListener(new ActionManager.AppAdapter() {
            @Override
            public void onCancel(ActionEvent actionEvent) {
                if (getSelectedComponent() instanceof TabItem) {
                    TabItem tabItem = (TabItem) getSelectedComponent();
                    if (tabItem.isCancelable()) {
                        tabItem.cancel();
                    }
                }
            }

            @Override
            public void onMenu(ActionEvent actionEvent) {
                mMenuMouseAdapter.mousePressed(null);
            }
        });
    }

    void close(Job job) {
        TabItem panel = getTabItem(job);
        mJobMap.remove(job.getId());
        remove(panel);
    }

    private synchronized TabItem getTabItem(Job job) {
        TabItem tabItem;

        if (mJobMap.containsKey(job.getId())) {
            tabItem = mJobMap.get(job.getId());
        } else {
            tabItem = new TabItem(job);
            tabItem.getMenuButton().addMouseListener(mMenuMouseAdapter);
            tabItem.getCloseButton().setAction(mCloseAction);
            tabItem.getCloseButton().setText(null);

            add(tabItem, job.getName());
            TabCloser tabCloser = new TabCloser(this);
            tabCloser.getButton().addActionListener((ActionEvent e) -> {
                close(job);
            });
            tabCloser.postSetAction();
            tabItem.setCloser(tabCloser);
            setTabComponentAt(getTabCount() - 1, tabCloser);

            mJobMap.put(job.getId(), tabItem);
            setSelectedComponent(tabItem);
        }

        return tabItem;
    }

    private void init() {
        setFocusTraversalKeysEnabled(false);
        mSpeedDialPanel = new SpeedDialPanel();
        add(mSpeedDialPanel, MaterialIcon._Action.HOME.get(AlmondUI.ICON_SIZE_NORMAL, mAlmondOptions.getIconColor()));

        IconColor iconColor = mAlmondOptions.getIconColor();
        setIconAt(0, MaterialIcon._Action.HOME.get(AlmondUI.ICON_SIZE_NORMAL, iconColor));

        mJobMap.values().stream().forEach((tabItem) -> {
            tabItem.updateIcons(iconColor);
        });

        mManager.addConnectionListeners(this);
        mManager.getClient().addServerEventListener(this);
        mMenuMouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e == null || e.getButton() == MouseEvent.BUTTON1) {
                    Component component = ((TabListener) getSelectedComponent()).getMenuButton();
                    JPopupMenu popupMenu = MainFrame.getPopupMenu();
                    InputMap inputMap = popupMenu.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW);
                    ActionMap actionMap = popupMenu.getActionMap();
                    Action action = new AbstractAction("HideMenu") {

                        @Override
                        public void actionPerformed(ActionEvent e) {
                            popupMenu.setVisible(false);
                        }
                    };

                    String key = "HideMenu";
                    actionMap.put(key, action);
                    KeyStroke keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
                    inputMap.put(keyStroke, key);

                    if (popupMenu.isVisible()) {
                        popupMenu.setVisible(false);
                    } else {
                        popupMenu.show(component, component.getWidth() - popupMenu.getWidth(), component.getHeight());

                        int x = component.getLocationOnScreen().x + component.getWidth() - popupMenu.getWidth();
                        int y = component.getLocationOnScreen().y + component.getHeight();

                        popupMenu.setLocation(x, y);
                    }
                }
            }
        };

        mSpeedDialPanel.getMenuButton().addMouseListener(mMenuMouseAdapter);

        //FIXME Why is this necessary?
        setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
        setTabLayoutPolicy(WRAP_TAB_LAYOUT);
    }

    private void updateActionStates() {
        try {
            mCloseAction.setEnabled(false);
            mSaveAction.setEnabled(false);

            if (getSelectedComponent() instanceof TabItem) {
                TabItem tabItem = (TabItem) getSelectedComponent();
                if (tabItem.isClosable()) {
                    mCloseAction.setEnabled(true);
                    mSaveAction.setEnabled(true);
                }
            }
        } catch (NullPointerException e) {
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT
     * modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 0, 0, 0));
        setFont(getFont().deriveFont((getFont().getStyle() & ~java.awt.Font.ITALIC) & ~java.awt.Font.BOLD));
        addChangeListener(new javax.swing.event.ChangeListener() {
            public void stateChanged(javax.swing.event.ChangeEvent evt) {
                formStateChanged(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 292, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_formStateChanged
        updateActionStates();
    }//GEN-LAST:event_formStateChanged

    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
