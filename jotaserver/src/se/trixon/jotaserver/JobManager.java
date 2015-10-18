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
package se.trixon.jotaserver;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import se.trixon.jota.JsonHelper;
import se.trixon.jota.job.Job;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public enum JobManager {

    INSTANCE;
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DETAILS = "details";
    private static final String KEY_ID = "id";
    private static final String KEY_LAST_RUN = "lastRun";
    private static final String KEY_NAME = "name";
    private static final String KEY_RUN_AFTER_FAILURE = "runAfterFailure";
    private static final String KEY_RUN_AFTER_FAILURE_COMMAND = "runAfterFailureCommand";
    private static final String KEY_RUN_AFTER_SUCCESS = "runAfterSuccess";
    private static final String KEY_RUN_AFTER_SUCCESS_COMMAND = "runAfterSuccessCommand";
    private static final String KEY_RUN_BEFORE = "runBefore";
    private static final String KEY_RUN_BEFORE_COMMAND = "runBeforeCommand";
    private static final String KEY_RUN_BEFORE_HALT_ON_ERROR = "runBeforeHaltOnError";
    private static final String KEY_TASKS = "tasks";
    private final LinkedList<Job> mJobs = new LinkedList<>();

    private JobManager() {
    }

    public Object[] getArray() {
        return mJobs.toArray();
    }

    public Job getJobById(long id) {
        for (Job job : mJobs) {
            if (job.getId() == id) {
                return job;
            }
        }

        return null;
    }

    public LinkedList<Job> getJobs() {
        return mJobs;
    }

    public JSONArray getJsonArray() {
        JSONArray array = new JSONArray();

        for (Job job : mJobs) {
            JSONObject object = new JSONObject();
            object.put(KEY_ID, job.getId());
            object.put(KEY_NAME, job.getName());
            object.put(KEY_DESCRIPTION, job.getDescription());
            object.put(KEY_DETAILS, job.getDetails());
            object.put(KEY_LAST_RUN, job.getLastRun());
            object.put(KEY_TASKS, job.getTasksString());

            object.put(KEY_RUN_AFTER_FAILURE, job.isRunAfterFailure());
            object.put(KEY_RUN_AFTER_FAILURE_COMMAND, job.getRunAfterFailureCommand());

            object.put(KEY_RUN_AFTER_SUCCESS, job.isRunAfterSuccess());
            object.put(KEY_RUN_AFTER_SUCCESS_COMMAND, job.getRunAfterSuccessCommand());

            object.put(KEY_RUN_BEFORE, job.isRunBefore());
            object.put(KEY_RUN_BEFORE_COMMAND, job.getRunBeforeCommand());
            object.put(KEY_RUN_BEFORE_HALT_ON_ERROR, job.isRunBeforeHaltOnError());

            array.add(object);
        }

        return array;
    }

    public boolean hasJobs() {
        return getJobs().size() > 0;
    }

    public DefaultComboBoxModel populateModel(DefaultComboBoxModel model) {

        model.removeAllElements();

        mJobs.stream().forEach((job) -> {
            model.addElement(job);
        });

        return model;
    }

    public DefaultListModel populateModel(DefaultListModel model) {
        model.clear();

        mJobs.stream().forEach((job) -> {
            model.addElement(job);
        });

        return model;
    }

    void setJobs(JSONArray array) {
        mJobs.clear();

        for (Object arrayItem : array) {
            JSONObject object = (JSONObject) arrayItem;
            Job job = new Job();
            job.setId(JsonHelper.getLong(object, KEY_ID));
            job.setName((String) object.get(KEY_NAME));
            job.setDescription((String) object.get(KEY_DESCRIPTION));
            job.setDetails((String) object.get(KEY_DETAILS));
            job.setLastRun(JsonHelper.getLong(object, KEY_LAST_RUN));
            String taskIds = (String) object.get(KEY_TASKS);
            job.setTasks(TaskManager.INSTANCE.getTasks(taskIds));

            job.setRunAfterFailure((boolean) object.get(KEY_RUN_AFTER_FAILURE));
            job.setRunAfterFailureCommand((String) object.get(KEY_RUN_AFTER_FAILURE_COMMAND));

            job.setRunAfterSuccess((boolean) object.get(KEY_RUN_AFTER_SUCCESS));
            job.setRunAfterSuccessCommand((String) object.get(KEY_RUN_AFTER_SUCCESS_COMMAND));

            job.setRunBefore((boolean) object.get(KEY_RUN_BEFORE));
            job.setRunBeforeCommand((String) object.get(KEY_RUN_BEFORE_COMMAND));
            job.setRunBeforeHaltOnError((boolean) object.get(KEY_RUN_BEFORE_HALT_ON_ERROR));

            mJobs.add(job);
        }

        Collections.sort(mJobs);
    }

    void setJobs(DefaultListModel model) {
        mJobs.clear();

        for (Object object : model.toArray()) {
            mJobs.add((Job) object);
        }

    }
}
