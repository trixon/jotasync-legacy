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
package se.trixon.jota.shared.job;

import org.json.simple.JSONObject;

/**
 *
 * @author Patrik Karlsson
 */
public class JobExecuteSection extends JobSection {

    public static final String KEY = "execute";
    public static final String KEY_RUN_AFTER = "runAfter";
    public static final String KEY_RUN_AFTER_COMMAND = "runAfterCommand";
    public static final String KEY_RUN_AFTER_FAILURE = "runAfterFailure";
    public static final String KEY_RUN_AFTER_FAILURE_COMMAND = "runAfterFailureCommand";
    public static final String KEY_RUN_AFTER_SUCCESS = "runAfterSuccess";
    public static final String KEY_RUN_AFTER_SUCCESS_COMMAND = "runAfterSuccessCommand";
    public static final String KEY_RUN_BEFORE = "runBefore";
    public static final String KEY_RUN_BEFORE_COMMAND = "runBeforeCommand";
    public static final String KEY_RUN_BEFORE_HALT_ON_ERROR = "runBeforeHaltOnError";
    private boolean mRunAfter;
    private String mRunAfterCommand = "";
    private boolean mRunAfterFailure;
    private String mRunAfterFailureCommand = "";
    private boolean mRunAfterSuccess;
    private String mRunAfterSuccessCommand = "";
    private boolean mRunBefore;
    private String mRunBeforeCommand = "";
    private boolean mRunBeforeHaltOnError;

    @Override
    public JSONObject getJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(KEY_RUN_BEFORE, isRunBefore());
        jsonObject.put(KEY_RUN_BEFORE_COMMAND, getRunBeforeCommand());
        jsonObject.put(KEY_RUN_BEFORE_HALT_ON_ERROR, isRunBeforeHaltOnError());

        jsonObject.put(KEY_RUN_AFTER_FAILURE, isRunAfterFailure());
        jsonObject.put(KEY_RUN_AFTER_FAILURE_COMMAND, getRunAfterFailureCommand());

        jsonObject.put(KEY_RUN_AFTER_SUCCESS, isRunAfterSuccess());
        jsonObject.put(KEY_RUN_AFTER_SUCCESS_COMMAND, getRunAfterSuccessCommand());

        jsonObject.put(KEY_RUN_AFTER, isRunAfter());
        jsonObject.put(KEY_RUN_AFTER_COMMAND, getRunAfterCommand());

        return jsonObject;
    }

    public String getRunAfterCommand() {
        return mRunAfterCommand;
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

    public boolean isRunAfter() {
        return mRunAfter;
    }

    public boolean isRunAfterFailure() {
        return mRunAfterFailure;
    }

    public boolean isRunAfterSuccess() {
        return mRunAfterSuccess;
    }

    public boolean isRunBefore() {
        return mRunBefore;
    }

    public boolean isRunBeforeHaltOnError() {
        return mRunBeforeHaltOnError;
    }

    @Override
    public void loadFromJson(JSONObject jsonObject) {
        mRunBefore = optBoolean(jsonObject, KEY_RUN_BEFORE);
        mRunBeforeCommand = optString(jsonObject, KEY_RUN_BEFORE_COMMAND);
        mRunBeforeHaltOnError = optBoolean(jsonObject, KEY_RUN_BEFORE_HALT_ON_ERROR);

        mRunAfter = optBoolean(jsonObject, KEY_RUN_AFTER);
        mRunAfterCommand = optString(jsonObject, KEY_RUN_AFTER_COMMAND);

        mRunAfterFailure = optBoolean(jsonObject, KEY_RUN_AFTER_FAILURE);
        mRunAfterFailureCommand = optString(jsonObject, KEY_RUN_AFTER_FAILURE_COMMAND);

        mRunAfterSuccess = optBoolean(jsonObject, KEY_RUN_AFTER_SUCCESS);
        mRunAfterSuccessCommand = optString(jsonObject, KEY_RUN_AFTER_SUCCESS_COMMAND);
    }

    public void setRunAfter(boolean value) {
        mRunAfter = value;
    }

    public void setRunAfterCommand(String value) {
        mRunAfterCommand = value;
    }

    public void setRunAfterFailure(boolean value) {
        mRunAfterFailure = value;
    }

    public void setRunAfterFailureCommand(String value) {
        mRunAfterFailureCommand = value;
    }

    public void setRunAfterSuccess(boolean value) {
        mRunAfterSuccess = value;
    }

    public void setRunAfterSuccessCommand(String value) {
        mRunAfterSuccessCommand = value;
    }

    public void setRunBefore(boolean value) {
        mRunBefore = value;
    }

    public void setRunBeforeCommand(String value) {
        mRunBeforeCommand = value;
    }

    public void setRunBeforeHaltOnError(boolean value) {
        mRunBeforeHaltOnError = value;
    }
}
