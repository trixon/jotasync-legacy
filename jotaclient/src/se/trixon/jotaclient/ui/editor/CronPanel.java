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
package se.trixon.jotaclient.ui.editor;

import java.awt.Component;

/**
 *
 * @author Patrik Karlsson
 */
public class CronPanel extends EditPanel {
    
    private Component mRoot;

    /**
     * Creates new form CronPanel
     */
    public CronPanel() {
        init();
    }
    
    public boolean isCronActive() {
        return toggleButton.isSelected();
    }
    
    @Override
    public void save() {
    }

    public void setCronActive(boolean value) {
        toggleButton.setSelected(value);
    }
    
    private void init() {
        label.setVisible(false);
        toggleButton.setVisible(true);
        addButton.setVisible(true);
        editButton.setVisible(true);
        removeButton.setVisible(true);
        removeAllButton.setVisible(true);
    }
}
