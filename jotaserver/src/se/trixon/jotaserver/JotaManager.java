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

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.prefs.Preferences;
import org.apache.commons.io.FileUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import se.trixon.jota.JsonHelper;
import se.trixon.util.Xlog;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public enum JotaManager {

    INSTANCE;
    private static final String KEY_JOBS = "jobs";
    private static final String KEY_TASKS = "tasks";
    private static final String KEY_VERSION = "version";
    private static final int sVersion = 1;
    private final File mDirectory;
    private final File mJobFile;
    private final JobManager mJobManager = JobManager.INSTANCE;
    private final File mLogDirectory;
    private final File mLogFile;
    private final Preferences mPreferences;
    private final TaskManager mTaskManager = TaskManager.INSTANCE;
    private int mVersion;

    private JotaManager() {
        mDirectory = new File(System.getProperty("user.home"), ".config/jotasync");
        mLogDirectory = new File(mDirectory, "log");
        mJobFile = new File(mDirectory, "jobs.json");
        mLogFile = new File(mDirectory, "jotasync.log");

        mPreferences = Preferences.userNodeForPackage(this.getClass());

        try {
            FileUtils.forceMkdir(mDirectory);
        } catch (IOException ex) {
            Xlog.timedErr(ex.getLocalizedMessage());
        }

        try {
            FileUtils.forceMkdir(mLogDirectory);
        } catch (IOException ex) {
            Xlog.timedErr(ex.getLocalizedMessage());
        }
    }

    public File getDirectory() {
        return mDirectory;
    }

    public File getJobFile() {
        return mJobFile;
    }

    public JobManager getJobManager() {
        return mJobManager;
    }

    public File getLogDirectory() {
        return mLogDirectory;
    }

    public File getLogFile() {
        return mLogFile;
    }

    public TaskManager getTaskManager() {
        return mTaskManager;
    }

    public int getVersion() {
        return mVersion;
    }

    public void load() throws IOException {
        if (mJobFile.exists()) {
            JSONObject jsonObject = (JSONObject) JSONValue.parse(FileUtils.readFileToString(mJobFile));
            mVersion = JsonHelper.getInt(jsonObject, KEY_VERSION);
            JSONArray jobsArray = (JSONArray) jsonObject.get(KEY_JOBS);
            JSONArray tasksArray = (JSONArray) jsonObject.get(KEY_TASKS);

            mTaskManager.setTasks(tasksArray);
            mJobManager.setJobs(jobsArray);
        }
    }

    public void save() throws IOException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put(KEY_TASKS, mTaskManager.getJsonArray());
        jsonObject.put(KEY_JOBS, mJobManager.getJsonArray());
        jsonObject.put(KEY_VERSION, sVersion);

        String jsonString = jsonObject.toJSONString();
        FileUtils.writeStringToFile(mJobFile, jsonString);
        String tag = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        mPreferences.put(tag, jsonString);
    }
}
