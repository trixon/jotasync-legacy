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
package se.trixon.jota.shared.task;

import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author Patrik Karlsson
 */
public class TaskExecuteSection extends TaskSection {

    public static final String KEY = "execute";
    private static final String KEY_AFTER = "runAfter";
    private static final String KEY_AFTER_COMMAND = "runAfterCommand";
    private static final String KEY_AFTER_FAILURE = "runAfterFailure";
    private static final String KEY_AFTER_FAILURE_COMMAND = "runAfterFailureCommand";
    private static final String KEY_AFTER_FAILURE_HALT_ON_ERROR = "runAfterFailureHaltOnError";
    private static final String KEY_AFTER_HALT_ON_ERROR = "runAfterHaltOnError";
    private static final String KEY_AFTER_SUCCESS = "runAfterSuccess";
    private static final String KEY_AFTER_SUCCESS_COMMAND = "runAfterSuccessCommand";
    private static final String KEY_AFTER_SUCCESS_HALT_ON_ERROR = "runAfterSuccessHaltOnError";
    private static final String KEY_BEFORE = "runBefore";
    private static final String KEY_BEFORE_COMMAND = "runBeforeCommand";
    private static final String KEY_BEFORE_HALT_ON_ERROR = "runBeforeHaltOnError";
    private static final String KEY_JOB_HALT_ON_ERROR = "jobHaltOnError";
    private boolean mAfter;
    private String mAfterCommand = "";
    private boolean mAfterFailure;
    private String mAfterFailureCommand = "";
    private boolean mAfterFailureHaltOnError;
    private boolean mAfterHaltOnError;
    private boolean mAfterSuccess;
    private String mAfterSuccessCommand = "";
    private boolean mAfterSuccessHaltOnError;
    private boolean mBefore;
    private String mBeforeCommand = "";
    private boolean mBeforeHaltOnError;
    private boolean mJobHaltOnError;

    public String getAfterCommand() {
        return mAfterCommand;
    }

    public String getAfterFailureCommand() {
        return mAfterFailureCommand;
    }

    public String getAfterSuccessCommand() {
        return mAfterSuccessCommand;
    }

    public String getBeforeCommand() {
        return mBeforeCommand;
    }

    @Override
    public List<String> getCommand() {
        return null;
    }

    @Override
    public JSONObject getJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(KEY_BEFORE, mBefore);
        jsonObject.put(KEY_BEFORE_COMMAND, mBeforeCommand);
        jsonObject.put(KEY_BEFORE_HALT_ON_ERROR, mBeforeHaltOnError);

        jsonObject.put(KEY_AFTER_FAILURE, mAfterFailure);
        jsonObject.put(KEY_AFTER_FAILURE_COMMAND, mAfterFailureCommand);
        jsonObject.put(KEY_AFTER_FAILURE_HALT_ON_ERROR, mAfterFailureHaltOnError);

        jsonObject.put(KEY_AFTER_SUCCESS, mAfterSuccess);
        jsonObject.put(KEY_AFTER_SUCCESS_COMMAND, mAfterSuccessCommand);
        jsonObject.put(KEY_AFTER_SUCCESS_HALT_ON_ERROR, mAfterSuccessHaltOnError);

        jsonObject.put(KEY_AFTER, mAfter);
        jsonObject.put(KEY_AFTER_COMMAND, mAfterCommand);
        jsonObject.put(KEY_AFTER_HALT_ON_ERROR, mAfterHaltOnError);

        jsonObject.put(KEY_JOB_HALT_ON_ERROR, mJobHaltOnError);

        return jsonObject;
    }

    public boolean isAfter() {
        return mAfter;
    }

    public boolean isAfterFailure() {
        return mAfterFailure;
    }

    public boolean isAfterFailureHaltOnError() {
        return mAfterFailureHaltOnError;
    }

    public boolean isAfterHaltOnError() {
        return mAfterHaltOnError;
    }

    public boolean isAfterSuccess() {
        return mAfterSuccess;
    }

    public boolean isAfterSuccessHaltOnError() {
        return mAfterSuccessHaltOnError;
    }

    public boolean isBefore() {
        return mBefore;
    }

    public boolean isBeforeHaltOnError() {
        return mBeforeHaltOnError;
    }

    public boolean isJobHaltOnError() {
        return mJobHaltOnError;
    }

    @Override
    public void loadFromJson(JSONObject jsonObject) {
        mBefore = optBoolean(jsonObject, KEY_BEFORE);
        mBeforeCommand = optString(jsonObject, KEY_BEFORE_COMMAND);
        mBeforeHaltOnError = optBoolean(jsonObject, KEY_BEFORE_HALT_ON_ERROR);

        mAfter = optBoolean(jsonObject, KEY_AFTER);
        mAfterCommand = optString(jsonObject, KEY_AFTER_COMMAND);
        mAfterHaltOnError = optBoolean(jsonObject, KEY_AFTER_HALT_ON_ERROR);

        mAfterFailure = optBoolean(jsonObject, KEY_AFTER_FAILURE);
        mAfterFailureCommand = optString(jsonObject, KEY_AFTER_FAILURE_COMMAND);
        mAfterFailureHaltOnError = optBoolean(jsonObject, KEY_AFTER_FAILURE_HALT_ON_ERROR);

        mAfterSuccess = optBoolean(jsonObject, KEY_AFTER_SUCCESS);
        mAfterSuccessCommand = optString(jsonObject, KEY_AFTER_SUCCESS_COMMAND);
        mAfterSuccessHaltOnError = optBoolean(jsonObject, KEY_AFTER_SUCCESS_HALT_ON_ERROR);

        mJobHaltOnError = optBoolean(jsonObject, KEY_JOB_HALT_ON_ERROR);
    }

    public void setAfter(boolean value) {
        mAfter = value;
    }

    public void setAfterCommand(String value) {
        mAfterCommand = value;
    }

    public void setAfterFailure(boolean value) {
        mAfterFailure = value;
    }

    public void setAfterFailureCommand(String value) {
        mAfterFailureCommand = value;
    }

    public void setAfterFailureHaltOnError(boolean value) {
        mAfterFailureHaltOnError = value;
    }

    public void setAfterHaltOnError(boolean value) {
        mAfterHaltOnError = value;
    }

    public void setAfterSuccess(boolean value) {
        mAfterSuccess = value;
    }

    public void setAfterSuccessCommand(String value) {
        mAfterSuccessCommand = value;
    }

    public void setAfterSuccessHaltOnError(boolean value) {
        mAfterSuccessHaltOnError = value;
    }

    public void setBefore(boolean value) {
        mBefore = value;
    }

    public void setBeforeCommand(String value) {
        mBeforeCommand = value;
    }

    public void setBeforeHaltOnError(boolean value) {
        mBeforeHaltOnError = value;
    }

    public void setJobHaltOnError(boolean value) {
        mJobHaltOnError = value;
    }
}
