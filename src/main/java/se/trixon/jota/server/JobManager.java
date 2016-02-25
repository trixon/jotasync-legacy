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
package se.trixon.jota.server;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedList;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import se.trixon.jota.shared.JsonHelper;
import se.trixon.jota.shared.job.JobExecuteSection;
import se.trixon.jota.shared.job.Job;

/**
 *
 * @author Patrik Karlsson
 */
enum JobManager {

    INSTANCE;
    private final LinkedList<Job> mJobs = new LinkedList<>();
    private static final String KEY_COLOR_BACKGROUND = "colorBackground";
    private static final String KEY_COLOR_FOREGROUND = "colorForeground";
    private static final String KEY_CRON_ACTIVE = "cronActive";
    private static final String KEY_CRON_ITEMS = "cronItems";
    private static final String KEY_DESCRIPTION = "description";
    private static final String KEY_DETAILS = "details";
    private static final String KEY_HISTORY = "history";
    private static final String KEY_ID = "id";
    private static final String KEY_LAST_RUN = "lastRun";
    private static final String KEY_LAST_RUN_EXIT_CODE = "lastRunExitCode";
    private static final String KEY_LOG_ERRORS = "logErrors";
    private static final String KEY_LOG_MODE = "logMode";
    private static final String KEY_LOG_OUTPUT = "logOutput";
    private static final String KEY_LOG_SEPARATE_ERRORS = "logSeparateErrors";
    private static final String KEY_NAME = "name";
    private static final String KEY_TASKS = "tasks";

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

            object.put(KEY_CRON_ACTIVE, job.isCronActive());
            object.put(KEY_CRON_ITEMS, job.getCronItems());

            object.put(KEY_HISTORY, job.getHistory());
            object.put(KEY_DETAILS, job.getDetails());
            object.put(KEY_LAST_RUN, job.getLastRun());
            object.put(KEY_LAST_RUN_EXIT_CODE, job.getLastRunExitCode());
            object.put(KEY_TASKS, job.getTasksString());

            object.put(KEY_COLOR_BACKGROUND, job.getColorBackground());
            object.put(KEY_COLOR_FOREGROUND, job.getColorForeground());

            object.put(KEY_LOG_OUTPUT, job.isLogOutput());
            object.put(KEY_LOG_ERRORS, job.isLogErrors());
            object.put(KEY_LOG_SEPARATE_ERRORS, job.isLogSeparateErrors());
            object.put(KEY_LOG_MODE, job.getLogMode());

            object.put(JobExecuteSection.KEY, job.getExecuteSection().getJson());

            array.add(object);
        }

        return array;
    }

    public boolean hasJobs() {
        return getJobs().size() > 0;
    }

    void setJobs(JSONArray array) {
        mJobs.clear();

        for (Object arrayItem : array) {
            JSONObject object = (JSONObject) arrayItem;
            Job job = new Job();
            job.setId(JsonHelper.getLong(object, KEY_ID));
            job.setName((String) object.get(KEY_NAME));
            job.setDescription((String) object.get(KEY_DESCRIPTION));
            job.setHistory((String) object.get(KEY_HISTORY));
            job.setDetails((String) object.get(KEY_DETAILS));
            job.setLastRun(JsonHelper.getLong(object, KEY_LAST_RUN));
            job.setLastRunExitCode(JsonHelper.getInt(object, KEY_LAST_RUN_EXIT_CODE));
            String taskIds = (String) object.get(KEY_TASKS);
            job.setTasks(TaskManager.INSTANCE.getTasks(taskIds));

            job.setCronActive(JsonHelper.optBoolean(object, KEY_CRON_ACTIVE));
            job.setCronItems((String) object.get(KEY_CRON_ITEMS));

            job.setColorBackground(JsonHelper.optString(object, KEY_COLOR_BACKGROUND));
            job.setColorForeground(JsonHelper.optString(object, KEY_COLOR_FOREGROUND));

            job.setLogOutput((boolean) object.get(KEY_LOG_OUTPUT));
            job.setLogErrors((boolean) object.get(KEY_LOG_ERRORS));
            job.setLogSeparateErrors((boolean) object.get(KEY_LOG_SEPARATE_ERRORS));
            job.setLogMode(JsonHelper.getInt(object, KEY_LOG_MODE));

            if (object.containsKey(JobExecuteSection.KEY)) {
                job.getExecuteSection().loadFromJson((JSONObject) object.get(JobExecuteSection.KEY));
            }

            mJobs.add(job);
        }

        Collections.sort(mJobs);
    }

    void setJobs(Job[] jobs) {
        mJobs.clear();
        mJobs.addAll(Arrays.asList(jobs));
    }
}
