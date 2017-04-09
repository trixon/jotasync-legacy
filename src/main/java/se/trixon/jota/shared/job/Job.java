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
package se.trixon.jota.shared.job;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.DefaultListModel;
import org.apache.commons.lang3.StringUtils;
import se.trixon.jota.client.ui.editor.module.job.JobExecutePanel;
import se.trixon.jota.shared.Jota;
import se.trixon.jota.shared.task.Task;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class Job implements Comparable<Job>, Serializable {

    public static OUTPUT TO_STRING = OUTPUT.VERBOSE;

    private String mColorBackground;
    private String mColorForeground;
    private boolean mCronActive;
    private String mCronItems = "";
    private String mDescription = "";
    private String mDetails = "";
    private final JobExecuteSection mExecuteSection;
    private String mHistory = "";
    private long mId = System.currentTimeMillis();
    private long mLastRun = -1;
    private int mLastRunExitCode = -1;
    private boolean mLogErrors = true;
    private int mLogMode = 0;
    private boolean mLogOutput = true;
    private boolean mLogSeparateErrors = true;
    private String mName = "";
    private StringBuilder mSummaryBuilder;
    private List<Task> mTasks = new LinkedList<>();

    public Job() {
        mExecuteSection = new JobExecuteSection();
    }

    public Job(long id, String name, String description, String comment) {
        mId = id;
        mName = name;
        mDescription = description;
        mDetails = comment;
        mExecuteSection = new JobExecuteSection();
    }

    public void addHistory(String history) {
        mHistory = history + mHistory;
    }

    @Override
    public int compareTo(Job o) {
        return mName.compareTo(o.getName());
    }

    public String getCaption(boolean verbose) {
        String caption;
        if (verbose) {
            String template = "<html><center><h2><b>%s</b></h2><p><i>%s</i></p><br />%s %s</center></html>";
            caption = String.format(template, mName, mDescription, getLastRunDateTime("-"), getLastRunStatus());
        } else {
            String template = "<html><b>%s</b><i>%s</i> %s %s</html>";
            String description = mDescription;
            if (StringUtils.isEmpty(description)) {
                description = "&nbsp;";
            } else {
                description = String.format("(%s)", description);
            }
            caption = String.format(template, mName, description, getLastRunDateTime(""), getLastRunStatus());
        }

        return caption;
    }

    public String getColorBackground() {
        return mColorBackground;
    }

    public String getColorForeground() {
        return mColorForeground;
    }

    public String getCronItems() {
        return mCronItems;
    }

    public String getDescription() {
        return mDescription;
    }

    public String getDetails() {
        return mDetails;
    }

    public JobExecuteSection getExecuteSection() {
        return mExecuteSection;
    }

    public String getHistory() {
        return mHistory;
    }

    public long getId() {
        return mId;
    }

    public long getLastRun() {
        return mLastRun;
    }

    public String getLastRunDateTime(String replacement, long lastRun) {
        String lastRunDateTime = replacement;

        if (lastRun > 0) {
            Date date = new Date(lastRun);
            lastRunDateTime = new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(date);
        }

        return lastRunDateTime;
    }

    public String getLastRunDateTime(String replacement) {
        return getLastRunDateTime(replacement, mLastRun);
    }

    public int getLastRunExitCode() {
        return mLastRunExitCode;
    }

    public String getLastRunStatus() {
        String status = "";
        if (mLastRun > 0) {
            status = getLastRunExitCode() == 0 ? "☺" : "☹";
            //if (isRunnning()) {
            //    status = "∞";
            //}
        }

        return status;
    }

    public int getLogMode() {
        return mLogMode;
    }

    public String getName() {
        return mName;
    }

    public String getSummaryAsHtml() {
        mSummaryBuilder = new StringBuilder("<html><body>");
        mSummaryBuilder.append("<h1>").append(getName()).append("</h1>");
        ResourceBundle bundle = SystemHelper.getBundle(JobExecutePanel.class, "Bundle");

        addOptionalToSummary(mExecuteSection.isBefore(), mExecuteSection.getBeforeCommand(), bundle.getString("JobPanel.beforePanel.header"));
        if (mExecuteSection.isBefore() && mExecuteSection.isBeforeHaltOnError()) {
            mSummaryBuilder.append(Dict.STOP_ON_ERROR.toString());
        }

        addOptionalToSummary(mExecuteSection.isAfterFailure(), mExecuteSection.getAfterFailureCommand(), bundle.getString("JobPanel.afterFailurePanel.header"));
        addOptionalToSummary(mExecuteSection.isAfterSuccess(), mExecuteSection.getAfterSuccessCommand(), bundle.getString("JobPanel.afterSuccessPanel.header"));
        addOptionalToSummary(mExecuteSection.isAfter(), mExecuteSection.getAfterCommand(), bundle.getString("JobPanel.afterPanel.header"));

        for (Task task : getTasks()) {
            mSummaryBuilder.append("<hr>");
            mSummaryBuilder.append(task.getSummaryAsHtml());
        }

        mSummaryBuilder.append("</body></html>");

        return mSummaryBuilder.toString();
    }

    public List<Task> getTasks() {
        return mTasks;
    }

    public String getTasksString() {
        Long[] taskIds = new Long[mTasks.size()];

        for (int i = 0; i < taskIds.length; i++) {
            taskIds[i] = mTasks.get(i).getId();
        }

        return StringUtils.join(taskIds, Jota.TASK_SEPARATOR);
    }

    public boolean isCronActive() {
        return mCronActive;
    }

    public boolean isLogErrors() {
        return mLogErrors;
    }

    public boolean isLogOutput() {
        return mLogOutput;
    }

    public boolean isLogSeparateErrors() {
        return mLogSeparateErrors;
    }

    public boolean isValid() {
        return !getName().isEmpty();
    }

    public void setColorBackground(String colorBackground) {
        mColorBackground = colorBackground;
    }

    public void setColorForeground(String colorForeground) {
        mColorForeground = colorForeground;
    }

    public void setCronActive(boolean cronActive) {
        mCronActive = cronActive;
    }

    public void setCronItems(String cronItems) {
        mCronItems = cronItems;
    }

    public void setDescription(String string) {
        mDescription = string;
    }

    public void setDetails(String string) {
        mDetails = string;
    }

    public void setHistory(String history) {
        mHistory = history == null ? "" : history;
    }

    public void setId(long id) {
        mId = id;
    }

    public void setLastRun(long lastRun) {
        mLastRun = lastRun;
    }

    public void setLastRunExitCode(int lastRunExitCode) {
        mLastRunExitCode = lastRunExitCode;
    }

    public void setLogErrors(boolean logErrors) {
        mLogErrors = logErrors;
    }

    public void setLogMode(int logMode) {
        mLogMode = logMode;
    }

    public void setLogOutput(boolean logOutput) {
        mLogOutput = logOutput;
    }

    public void setLogSeparateErrors(boolean logSeparateErrors) {
        mLogSeparateErrors = logSeparateErrors;
    }

    public void setName(String name) {
        mName = name;
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
            String description = StringUtils.isBlank(mDescription) ? "&nbsp;" : mDescription;

            return String.format("<html><b>%s</b><br /><i>%s</i></html>", mName, description);
        }
    }

    private void addOptionalToSummary(boolean active, String command, String header) {
        if (active) {
            mSummaryBuilder.append(String.format("<p><b>%s</b><br /><i>%s</i></p>", header, command));
        }
    }

    public enum OUTPUT {

        NORMAL, VERBOSE;
    }
}
