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
package se.trixon.jotaserver;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import se.trixon.jota.Jota;
import se.trixon.jota.JsonHelper;
import se.trixon.jota.task.Task;
import se.trixon.jota.task.Task.ExecuteSection;

/**
 *
 * @author Patrik Karlsson
 */
public enum TaskManager {

    INSTANCE;
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DEST = "dest";
    private static final String KEY_DETAILS = "details";
    private static final String KEY_ID = "id";
    private static final String KEY_NAME = "name";
    private static final String KEY_SOURCE = "source";
    private static final String KEY_TYPE = "type";
    private static final String SECTION_ENVIRONMENT = "environment";
    private static final String SECTION_EXCLUDE = "exclude";
    private static final String SECTION_EXECUTE = "execute";
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
            JSONObject environmentObject = new JSONObject();
            JSONObject excludeObject = new JSONObject();
            JSONObject executeObject = new JSONObject();

            object.put(KEY_ID, task.getId());
            object.put(KEY_NAME, task.getName());
            object.put(KEY_TYPE, task.getType());
            object.put(KEY_SOURCE, task.getSource());
            object.put(KEY_DEST, task.getDestination());
            object.put(KEY_DESCRIPTION, task.getDescription());
            object.put(KEY_DETAILS, task.getDetails());

            object.put(SECTION_ENVIRONMENT, environmentObject);
            object.put(SECTION_EXCLUDE, excludeObject);

            ExecuteSection executeSection = task.getExecuteSection();
            executeObject.put(ExecuteSection.KEY_RUN_BEFORE, executeSection.isRunBefore());
            executeObject.put(ExecuteSection.KEY_RUN_BEFORE_COMMAND, executeSection.getRunBeforeCommand());
            executeObject.put(ExecuteSection.KEY_RUN_BEFORE_HALT_ON_ERROR, executeSection.isRunBeforeHaltOnError());

            executeObject.put(ExecuteSection.KEY_RUN_AFTER_FAILURE, executeSection.isRunAfterFailure());
            executeObject.put(ExecuteSection.KEY_RUN_AFTER_FAILURE_COMMAND, executeSection.getRunAfterFailureCommand());

            executeObject.put(ExecuteSection.KEY_RUN_AFTER_SUCCESS, executeSection.isRunAfterSuccess());
            executeObject.put(ExecuteSection.KEY_RUN_AFTER_SUCCESS_COMMAND, executeSection.getRunAfterSuccessCommand());

            executeObject.put(ExecuteSection.KEY_RUN_AFTER, executeSection.isRunAfter());
            executeObject.put(ExecuteSection.KEY_RUN_AFTER_COMMAND, executeSection.getRunAfterCommand());

            object.put(SECTION_EXECUTE, executeObject);

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

    public DefaultListModel populateModel(DefaultListModel model) {
        model.clear();

        for (Task job : mTasks) {
            model.addElement(job);
        }

        return model;
    }

    public void setTasks(DefaultListModel model) {
        mTasks.clear();

        for (Object object : model.toArray()) {
            mTasks.add((Task) object);
        }
    }

    public void setTasks(JSONArray array) {
        mTasks.clear();

        for (Object arrayItem : array) {
            JSONObject object = (JSONObject) arrayItem;
            JSONObject environmentObject = (JSONObject) object.get(SECTION_ENVIRONMENT);
            JSONObject excludeObject = (JSONObject) object.get(SECTION_EXCLUDE);

            Task task = new Task();
            task.setId(JsonHelper.getLong(object, KEY_ID));
            task.setName((String) object.get(KEY_NAME));
            task.setDescription((String) object.get(KEY_DESCRIPTION));
            task.setType(JsonHelper.getInt(object, KEY_TYPE));
            task.setSource((String) object.get(KEY_SOURCE));
            task.setDestination((String) object.get(KEY_DEST));
            task.setDetails((String) object.get(KEY_DETAILS));

            JSONObject executeObject = (JSONObject) object.get(SECTION_EXECUTE);
            ExecuteSection executeSection = task.getExecuteSection();

            executeSection.setRunBefore((boolean) executeObject.get(ExecuteSection.KEY_RUN_BEFORE));
            executeSection.setRunBeforeCommand((String) executeObject.get(ExecuteSection.KEY_RUN_BEFORE_COMMAND));
            executeSection.setRunBeforeHaltOnError((boolean) executeObject.get(ExecuteSection.KEY_RUN_BEFORE_HALT_ON_ERROR));

            executeSection.setRunAfter((boolean) executeObject.get(ExecuteSection.KEY_RUN_AFTER));
            executeSection.setRunAfterCommand((String) executeObject.get(ExecuteSection.KEY_RUN_AFTER_COMMAND));

            executeSection.setRunAfterFailure((boolean) executeObject.get(ExecuteSection.KEY_RUN_AFTER_FAILURE));
            executeSection.setRunAfterFailureCommand((String) executeObject.get(ExecuteSection.KEY_RUN_AFTER_FAILURE_COMMAND));

            executeSection.setRunAfterSuccess((boolean) executeObject.get(ExecuteSection.KEY_RUN_AFTER_SUCCESS));
            executeSection.setRunAfterSuccessCommand((String) executeObject.get(ExecuteSection.KEY_RUN_AFTER_SUCCESS_COMMAND));

            mTasks.add(task);
        }

        Collections.sort(mTasks);
    }
}
