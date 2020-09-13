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
package se.trixon.jotasync.core.actions;

import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
import javax.swing.SwingUtilities;
import org.openide.awt.DynamicMenuContent;
import org.openide.awt.Mnemonics;
import org.openide.util.NbBundle;

/**
 *
 * @author Patrik Karlström
 */
public class ScheduledSyncAction extends AbstractAction implements Runnable, DynamicMenuContent {

    private JCheckBoxMenuItem[] mMenuItems;
    private boolean mState;

    public static Action create() {
        return new ScheduledSyncAction();
    }

    private ScheduledSyncAction() {
        super(NbBundle.getMessage(ScheduledSyncAction.class, "CTL_ScheduledSync"));
        addPropertyChangeListener(pce -> {
            if (Action.ACCELERATOR_KEY.equals(pce.getPropertyName())) {
                synchronized (ScheduledSyncAction.this) {
                    mMenuItems = null;
                    createItems();
                }
            }
        });
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        //TODO
        //Toggle Options.getInstance().isSceduledSync()
    }

    @Override
    public JComponent[] getMenuPresenters() {
        createItems();
        updateState();

        return mMenuItems;
    }

    @Override
    public void run() {
        synchronized (this) {
            createItems();
            //mMenuItems[0].setSelected(Options.getInstance().isSceduledSync());
        }
    }

    @Override
    public JComponent[] synchMenuPresenters(JComponent[] items) {
        updateState();
        return mMenuItems;
    }

    private void createItems() {
        synchronized (this) {
            if (mMenuItems == null) {
                mMenuItems = new JCheckBoxMenuItem[1];
                mMenuItems[0] = new JCheckBoxMenuItem(this);
                mMenuItems[0].setIcon(null);
                Mnemonics.setLocalizedText(mMenuItems[0], NbBundle.getMessage(ScheduledSyncAction.class, "CTL_ScheduledSync"));
            }
        }
    }

    private void updateState() {
        if (EventQueue.isDispatchThread()) {
            run();
        } else {
            SwingUtilities.invokeLater(this);
        }
    }
}
