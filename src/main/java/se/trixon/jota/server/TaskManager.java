/* 
 * Copyright 2021 Patrik Karlström.
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
package se.trixon.jota.server;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import se.trixon.jota.shared.task.Task;

/**
 *
 * @author Patrik Karlström
 */
enum TaskManager {

    INSTANCE;
    private List<String> mHistoryLines = new ArrayList<>();
    private final LinkedList<Task> mTasks = new LinkedList<>();

    private TaskManager() {
    }

    public boolean exists(Task task) {
        boolean exists = false;

        for (Task existingTask : mTasks) {
            if (task.getId() == existingTask.getId()) {
                exists = true;
                break;
            }
        }

        return exists;
    }

    public Object[] getArray() {
        return mTasks.toArray();
    }

    public Task getTaskById(long id) {
        Task foundTask = null;

        for (Task task : mTasks) {
            if (task.getId() == id) {
                foundTask = task;
                break;
            }
        }

        return foundTask;
    }

    public LinkedList<Task> getTasks() {
        return mTasks;
    }

    public List<Task> getTasks(ArrayList<Long> taskIds) {
        List<Task> tasks = new LinkedList<>();
        taskIds.forEach((id) -> {
            Task task = getTaskById(id);
            if (task != null) {
                tasks.add(getTaskById(id));
            }
        });

        return tasks;
    }

    public void setTasks(LinkedList<Task> tasks) {
        mTasks.clear();
        mTasks.addAll(tasks);
    }

    private void loadHistory(Task task) {
        StringBuilder builder = new StringBuilder();
        for (String line : mHistoryLines) {
            String id = String.valueOf(task.getId());
            if (StringUtils.contains(line, id)) {
                builder.append(StringUtils.remove(line, id + " ")).append("\n");
            }
        }
        task.setHistory(builder.toString());
    }

    void loadHistory() {
        try {
            mHistoryLines = FileUtils.readLines(JotaManager.getInstance().getHistoryFile(), Charset.defaultCharset());
            for (Task task : mTasks) {
                loadHistory(task);
            }
        } catch (IOException ex) {
            Logger.getLogger(JobManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
