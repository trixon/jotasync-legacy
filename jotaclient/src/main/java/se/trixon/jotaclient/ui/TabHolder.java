/*
 * Copyright 2015 Patrik Karlsson.
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
package se.trixon.jotaclient.ui;

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
import se.trixon.jota.ProcessEvent;
import se.trixon.jota.ServerEvent;
import se.trixon.jota.ServerEventListener;
import se.trixon.jota.job.Job;
import se.trixon.jota.task.Task;
import se.trixon.jotaclient.ConnectionListener;
import se.trixon.jotaclient.Manager;
import se.trixon.jotaclient.ui.MainFrame.ActionManager;
import se.trixon.util.SystemHelper;
import se.trixon.util.icon.Pict;

/**
 *
 * @author Patrik Karlsson
 */
public class TabHolder extends JTabbedPane implements ConnectionListener, ServerEventListener {

    private ActionManager mActionManager;
    private Action mCloseAction;
    private SpeedDialPanel mSpeedDialPanel;
    private HashMap<Long, TabItem> mJobMap = new HashMap<>();
    private final Manager mManager = Manager.getInstance();
    private MouseAdapter mMenuMouseAdapter;
    private Action mSaveAction;

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
                tabItem.log(ProcessEvent.OUT, "\n\nJob interrupted.");
                tabItem.enableSave();
                updateTitle(job, "i");
                updateActionStates();
                break;
            case FINISHED:
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
        mActionManager = ((MainFrame) SwingUtilities.getRoot(this)).getActionManager();
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

        action = new AbstractAction("DisplayMenu") {

            @Override
            public void actionPerformed(ActionEvent e) {
                mMenuMouseAdapter.mousePressed(null);
            }
        };

        key = "DisplayMenu";
        actionMap.put(key, action);
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_M, commandMask);
        inputMap.put(keyStroke, key);
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_F10, 0);
        inputMap.put(keyStroke, key);

        action = new AbstractAction("Cancel") {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (getSelectedComponent() instanceof TabItem) {
                    TabItem tabItem = (TabItem) getSelectedComponent();
                    if (tabItem.isCancelable()) {
                        tabItem.cancel();
                    }
                }
            }
        };

        key = "Cancel";
        actionMap.put(key, action);
        keyStroke = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        inputMap.put(keyStroke, key);
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
            tabItem.getSaveButton().setAction(mSaveAction);
            tabItem.getSaveButton().setText(null);
            tabItem.getCloseButton().setAction(mCloseAction);
            tabItem.getCloseButton().setText(null);

            add(tabItem, job.getName());
            mJobMap.put(job.getId(), tabItem);
            setSelectedComponent(tabItem);
        }

        return tabItem;
    }

    private void init() {
        setFocusTraversalKeysEnabled(false);
        mSpeedDialPanel = new SpeedDialPanel();
        add(mSpeedDialPanel, Pict.Actions.GO_HOME.get(UI.ICON_SIZE_LARGE));

        mManager.addConnectionListeners(this);
        mManager.getClient().addServerEventListener(this);
        mMenuMouseAdapter = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (e == null || e.getButton() == MouseEvent.BUTTON1) {
                    Component component = ((TabListener) getSelectedComponent()).getMenuButton();
                    JPopupMenu popupMenu = MainFrame.getPopupMenu();

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
        //setTabLayoutPolicy(SCROLL_TAB_LAYOUT);
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
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

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
            .addGap(0, 300, Short.MAX_VALUE)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void formStateChanged(javax.swing.event.ChangeEvent evt) {//GEN-FIRST:event_formStateChanged
        updateActionStates();
    }//GEN-LAST:event_formStateChanged


    // Variables declaration - do not modify//GEN-BEGIN:variables
    // End of variables declaration//GEN-END:variables
}
