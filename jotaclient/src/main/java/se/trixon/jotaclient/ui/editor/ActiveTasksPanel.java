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
package se.trixon.jotaclient.ui.editor;

import java.awt.event.ActionEvent;
import javax.swing.DefaultListModel;
import se.trixon.jota.job.Job;
import se.trixon.jota.task.Task;
import se.trixon.util.dictionary.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class ActiveTasksPanel extends EditPanel {

    private Job mJob;

    public ActiveTasksPanel() {
        init();
        initListeners();
    }

    @Override
    public void save() {
        mJob.setTasks(getModel());
    }

    public void setJob(Job job) {
        mJob = job;
    }

    private void deactivateButtonActionPerformed(ActionEvent e) {
        int[] selectedIndices = list.getSelectedIndices();

        if (selectedIndices.length > 0) {
            for (int i = selectedIndices.length - 1; i >= 0; i--) {
                selectedIndices = list.getSelectedIndices();
                getModel().removeElementAt(selectedIndices[i]);
            }
            save();
        }
    }

    private void init() {
        label.setText(Dict.TASKS_ACTIVE.getString());

        moveLastButton.setVisible(true);
        moveDownButton.setVisible(true);
        moveFirstButton.setVisible(true);
        moveUpButton.setVisible(true);
        activateButton.setVisible(true);
        deactivateButton.setVisible(true);
    }

    private void initListeners() {
        deactivateButton.addActionListener(this::deactivateButtonActionPerformed);

        moveFirstButton.addActionListener((ActionEvent e) -> {
            move(Move.FIRST);
        });

        moveUpButton.addActionListener((ActionEvent e) -> {
            move(Move.UP);
        });

        moveDownButton.addActionListener((ActionEvent e) -> {
            move(Move.DOWN);
        });

        moveLastButton.addActionListener((ActionEvent e) -> {
            move(Move.LAST);
        });
    }

    private void move(Move move) {
        DefaultListModel model = getModel();
        int destinationIndex = 0;
        int index = list.getSelectedIndex();
        int size = model.size();

        if (list.getSelectedValuesList().size() == 1) {
            Task selectedTask = (Task) model.remove(index);

            switch (move) {
                case DOWN:
                    destinationIndex = Math.min(index + 1, size - 1);
                    break;

                case UP:
                    destinationIndex = Math.max(index - 1, 0);
                    break;

                case FIRST:
                    destinationIndex = 0;
                    break;

                case LAST:
                    destinationIndex = size - 1;
                    break;
            }

            model.add(destinationIndex, selectedTask);
            list.setSelectedIndex(destinationIndex);
            save();
        }
    }

    private enum Move {

        FIRST, UP, DOWN, LAST
    }
}
