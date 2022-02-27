/* 
 * Copyright 2022 Patrik Karlström.
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
package se.trixon.jota.shared.task;

import java.io.File;
import java.io.Serializable;
import java.util.ResourceBundle;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.Dict;
import se.trixon.jota.client.ui_swing.editor.module.task.TaskExcludePanel;
import se.trixon.jota.client.ui_swing.editor.module.task.TaskExecutePanel;

/**
 *
 * @author Patrik Karlström
 */
public class TaskValidator implements Serializable {

    private final StringBuilder mHtmlBuilder;
    private boolean mInvalid = false;
    private final StringBuilder mStringBuilder;
    private final Task mTask;

    public TaskValidator(Task task) {
        mTask = task;
        mHtmlBuilder = new StringBuilder("<h2>").append(mTask.getName()).append("</h2>");
        mStringBuilder = new StringBuilder();

        validateDirs();
        validateExecutors();
        validateExclusion();
    }

    public String getSummary() {
        return mStringBuilder.toString();
    }

    public String getSummaryAsHtml() {
        return mHtmlBuilder.toString();
    }

    public boolean isValid() {
        return !mInvalid;
    }

    private void addSummary(String header, String message) {
        mHtmlBuilder.append(String.format("<p><b>%s</b><br /><i>%s</i><br />&nbsp;</p>", header, message));
    }

    private void validateDir(String path, String message) {
        //TODO Add support for remote - well, don't fail...
        File file = new File(path);
        if (!file.isDirectory()) {
            addSummary(message, path);
            mInvalid = true;
        }
    }

    private void validateDirs() {
        validateDir(mTask.getSource(), Dict.DIRECTORY_NOT_FOUND_SOURCE.toString());
        validateDir(mTask.getDestination(), Dict.DIRECTORY_NOT_FOUND_DEST.toString());
    }

    private void validateExclusion() {
        ExcludeSection excludeSection = mTask.getExcludeSection();
        ResourceBundle bundle = SystemHelper.getBundle(TaskExcludePanel.class, "Bundle");

        validateFile(excludeSection.isManualFileUsed(), excludeSection.getManualFilePath(), bundle.getString("TaskExcludePanel.externalFilePanel.header"));
    }

    private void validateExecutors() {
        TaskExecuteSection executeSection = mTask.getExecuteSection();
        ResourceBundle bundle = SystemHelper.getBundle(TaskExecutePanel.class, "Bundle");

        validateFile(executeSection.isBefore(), executeSection.getBeforeCommand(), bundle.getString("TaskExecutePanel.beforePanel.header"));
        validateFile(executeSection.isAfterFailure(), executeSection.getAfterFailureCommand(), bundle.getString("TaskExecutePanel.afterFailurePanel.header"));
        validateFile(executeSection.isAfterSuccess(), executeSection.getAfterSuccessCommand(), bundle.getString("TaskExecutePanel.afterSuccessPanel.header"));
        validateFile(executeSection.isAfter(), executeSection.getAfterCommand(), bundle.getString("TaskExecutePanel.afterPanel.header"));
    }

    private void validateFile(boolean active, String command, String header) {
        File file = new File(command);
        if (active && !file.exists()) {
            mInvalid = true;
            addSummary(header, String.format("%s: %s", Dict.Dialog.TITLE_FILE_NOT_FOUND.toString(), command));
        }
    }
}
