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
package se.trixon.jota.job;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListModel;
import org.apache.commons.lang3.StringUtils;
import se.trixon.jota.task.Task;
import se.trixon.jota.task.TaskManager;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class Job implements Comparable<Job>, Serializable {

    public static OUTPUT TO_STRING = OUTPUT.VERBOSE;
    private String mDescription = "";
    private String mDetails = "";
    private long mId = System.currentTimeMillis();
    private long mLastRun = -1;
    private String mName = "";
    private boolean mRunAfterFailure;
    private String mRunAfterFailureCommand = "";
    private boolean mRunAfterSuccess;
    private String mRunAfterSuccessCommand = "";
    private boolean mRunBefore;
    private String mRunBeforeCommand = "";
    private boolean mRunBeforeHaltOnError;
    private List<Task> mTasks = new LinkedList<>();

    public Job() {
    }

    public Job(long id, String name, String description, String comment) {
        mId = id;
        mName = name;
        mDescription = description;
        mDetails = comment;
    }

    @Override
    public int compareTo(Job o) {
        return mName.compareTo(o.getName());
    }

    public String getDescription() {
        return mDescription;
    }

    public String getDetails() {
        return mDetails;
    }

    public long getId() {
        return mId;
    }

    public long getLastRun() {
        return mLastRun;
    }

    public String getName() {
        return mName;
    }

    public String getRunAfterFailureCommand() {
        return mRunAfterFailureCommand;
    }

    public String getRunAfterSuccessCommand() {
        return mRunAfterSuccessCommand;
    }

    public String getRunBeforeCommand() {
        return mRunBeforeCommand;
    }

    public List<Task> getTasks() {
        return mTasks;
    }

    public String getTasksString() {
        Long[] taskIds = new Long[mTasks.size()];

        for (int i = 0; i < taskIds.length; i++) {
            taskIds[i] = mTasks.get(i).getId();
        }

        return StringUtils.join(taskIds, TaskManager.SEPARATOR);
    }

    public boolean isRunAfterFailure() {
        return mRunAfterFailure;
    }

    public boolean isRunAfterFailureValid() {
        return isCommandValid(mRunAfterFailureCommand);
    }

    public boolean isRunAfterSuccess() {
        return mRunAfterSuccess;
    }

    public boolean isRunAfterSuccessValid() {
        return isCommandValid(mRunAfterSuccessCommand);
    }

    public boolean isRunBefore() {
        return mRunBefore;
    }

    public boolean isRunBeforeHaltOnError() {
        return mRunBeforeHaltOnError;
    }

    public boolean isRunBeforeValid() {
        return isCommandValid(mRunBeforeCommand);
    }

    public boolean isValid() {
        return !getName().isEmpty();
    }

    public void setDescription(String string) {
        mDescription = string;
    }

    public void setDetails(String string) {
        mDetails = string;
    }

    public void setId(long id) {
        mId = id;
    }

    public void setLastRun(long lastRun) {
        mLastRun = lastRun;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setRunAfterFailure(boolean runAfterFailure) {
        mRunAfterFailure = runAfterFailure;
    }

    public void setRunAfterFailureCommand(String runAfterFailureCommand) {
        mRunAfterFailureCommand = runAfterFailureCommand;
    }

    public void setRunAfterSuccess(boolean runAfterSuccess) {
        mRunAfterSuccess = runAfterSuccess;
    }

    public void setRunAfterSuccessCommand(String runAfterSuccessCommand) {
        mRunAfterSuccessCommand = runAfterSuccessCommand;
    }

    public void setRunBefore(boolean runBefore) {
        mRunBefore = runBefore;
    }

    public void setRunBeforeCommand(String runBeforeCommand) {
        mRunBeforeCommand = runBeforeCommand;
    }

    public void setRunBeforeHaltOnError(boolean runBeforeOnError) {
        mRunBeforeHaltOnError = runBeforeOnError;
    }

    public void setTasks(List<Task> tasks) {
        mTasks = tasks;
    }

    public void setTasks(DefaultListModel model) {
        mTasks.clear();

        for (Object object : model.toArray()) {
            mTasks.add((Task) object);
        }
    }

    @Override
    public String toString() {
        if (TO_STRING == OUTPUT.NORMAL) {
            return mName;
        } else {
            String description = mDescription;
            if (StringUtils.isEmpty(description)) {
                description = "&nbsp;";
            }
            return String.format("<html><b>%s</b><br /><i>%s</i></html>", mName, description);
        }
    }

    private boolean isCommandValid(String command) {
//        File file = new File(command);
//        return file.exists() && file.isFile();
        return true;
    }

    public enum OUTPUT {

        NORMAL, VERBOSE;
    }
}
