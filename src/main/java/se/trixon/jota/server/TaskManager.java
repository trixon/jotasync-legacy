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
package se.trixon.jota.server;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import se.trixon.jota.shared.Jota;
import se.trixon.jota.shared.JsonHelper;
import se.trixon.jota.shared.task.ExcludeSection;
import se.trixon.jota.shared.task.OptionSection;
import se.trixon.jota.shared.task.Task;
import se.trixon.jota.shared.task.TaskExecuteSection;

/**
 *
 * @author Patrik Karlsson
 */
enum TaskManager {

    INSTANCE;
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DEST = "dest";
    private static final String KEY_DETAILS = "details";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_NO_ADDITIONAL_DIR = "additionalDir";
    private static final String KEY_SOURCE = "source";
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

    public JSONArray getJsonArray() {
        JSONArray array = new JSONArray();

        for (Task task : mTasks) {
            JSONObject object = new JSONObject();

            object.put(KEY_ID, task.getId());
            object.put(KEY_NAME, task.getName());
            object.put(KEY_SOURCE, task.getSource());
            object.put(KEY_DEST, task.getDestination());
            object.put(KEY_NO_ADDITIONAL_DIR, task.isNoAdditionalDir());
            object.put(KEY_DESCRIPTION, task.getDescription());
            object.put(KEY_DETAILS, task.getDetails());

            object.put(TaskExecuteSection.KEY, task.getExecuteSection().getJson());
            object.put(OptionSection.KEY, task.getOptionSection().getJson());
            object.put(ExcludeSection.KEY, task.getExcludeSection().getJson());

            array.add(object);
        }

        return array;
    }

    public String getJsonString() {
        return getJsonArray().toJSONString();
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

    public List<Task> getTasks(String taskIdsString) {
        List<Task> tasks = new LinkedList<>();
        String[] taskIds = StringUtils.split(taskIdsString, Jota.TASK_SEPARATOR);

        for (String taskId : taskIds) {
            long id = Long.parseLong(taskId);
            Task task = getTaskById(id);

            if (task != null) {
                tasks.add(getTaskById(id));
            }
        }

        return tasks;
    }

    public void setTasks(Task[] tasks) {
        mTasks.clear();
        mTasks.addAll(Arrays.asList(tasks));
    }

    public void setTasks(JSONArray array) {
        try {
            mHistoryLines = FileUtils.readLines(JotaManager.INSTANCE.getHistoryFile(), Charset.defaultCharset());
        } catch (IOException ex) {
            Logger.getLogger(JobManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        mTasks.clear();

        for (Object arrayItem : array) {
            JSONObject object = (JSONObject) arrayItem;

            Task task = new Task();
            task.setId(JsonHelper.getLong(object, KEY_ID));
            task.setName((String) object.get(KEY_NAME));
            task.setDescription((String) object.get(KEY_DESCRIPTION));
            task.setSource((String) object.get(KEY_SOURCE));
            task.setDestination((String) object.get(KEY_DEST));
            task.setNoAdditionalDir(JsonHelper.optBoolean(object, KEY_NO_ADDITIONAL_DIR));
            task.setDetails((String) object.get(KEY_DETAILS));

            if (object.containsKey(TaskExecuteSection.KEY)) {
                task.getExecuteSection().loadFromJson((JSONObject) object.get(TaskExecuteSection.KEY));
            }

            if (object.containsKey(OptionSection.KEY)) {
                task.getOptionSection().loadFromJson((JSONObject) object.get(OptionSection.KEY));
            }

            if (object.containsKey(ExcludeSection.KEY)) {
                task.getExcludeSection().loadFromJson((JSONObject) object.get(ExcludeSection.KEY));
            }

            loadHistory(task);
            mTasks.add(task);
        }

        Collections.sort(mTasks);
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
}
