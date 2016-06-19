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

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ResourceBundle;
import se.trixon.jota.client.ui.editor.module.job.JobExecutePanel;
import se.trixon.jota.shared.job.Job;
import se.trixon.jota.shared.job.JobExecuteSection;
import se.trixon.jota.shared.task.Task;
import se.trixon.jota.shared.task.TaskValidator;
import se.trixon.almond.util.BundleHelper;
import se.trixon.almond.util.Dict;

/**
 *
 * @author Patrik Karlsson
 */
public class JobValidator implements Serializable {

    private final StringBuilder mHtmlBuilder;
    private boolean mInvalid = false;
    private final Job mJob;
    private final ServerOptions mOptions = ServerOptions.INSTANCE;
    private boolean mRsync;
    private final StringBuilder mStringBuilder;

    public JobValidator(Job job) {
        mJob = job;
        mStringBuilder = new StringBuilder();
        mHtmlBuilder = new StringBuilder("<html>");

        validateRsync();
        mHtmlBuilder.append("<h1>").append(mJob.getName()).append("</h1>");
        validateExecutors();
        validateTasks();
    }

    public String getSummary() {
        return mStringBuilder.toString();
    }

    public String getSummaryAsHtml() {
        return mHtmlBuilder.toString();
    }

    public boolean isRsync() {
        return mRsync;
    }

    public boolean isValid() {
        return !mInvalid;
    }

    private void addSummary(String header, String message) {
        mHtmlBuilder.append(String.format("<p><b>%s</b><br /><i>%s</i><br />&nbsp;</p>", header, message));
        mStringBuilder.append(header).append(message).append("\n");
    }

    private void validateExecutor(boolean active, String command, String key) {
        ResourceBundle bundle = BundleHelper.getBundle(JobExecutePanel.class, "Bundle");
        File file = new File(command);
        if (active && !file.exists()) {
            mInvalid = true;
            addSummary(bundle.getString(key), String.format("%s: %s", Dict.FILE_NOT_FOUND_TITLE.toString(), command));
        }
    }

    private void validateExecutors() {
        JobExecuteSection executeSection = mJob.getExecuteSection();

        validateExecutor(executeSection.isBefore(), executeSection.getBeforeCommand(), "JobPanel.beforePanel.header");
        validateExecutor(executeSection.isAfterFailure(), executeSection.getAfterFailureCommand(), "JobPanel.afterFailurePanel.header");
        validateExecutor(executeSection.isAfterSuccess(), executeSection.getAfterSuccessCommand(), "JobPanel.afterSuccessPanel.header");
        validateExecutor(executeSection.isAfter(), executeSection.getAfterCommand(), "JobPanel.afterPanel.header");
    }

    private void validateRsync() {
        try {
            ProcessBuilder processBuilder = new ProcessBuilder(new String[]{mOptions.getRsyncPath(), "--version"});
            Process process = processBuilder.start();
            process.waitFor();
            mRsync = true;
        } catch (InterruptedException | IOException ex) {
            mInvalid = true;
            mRsync = false;
            addSummary("Command not found", mOptions.getRsyncPath());
        }
    }

    private void validateTasks() {
        for (Task task : mJob.getTasks()) {
            TaskValidator taskValidator = new TaskValidator(task);
            if (!taskValidator.isValid()) {
                mHtmlBuilder.append("<hr>");
                mHtmlBuilder.append(taskValidator.getSummaryAsHtml());
                mInvalid = true;
            }
        }
    }
}
