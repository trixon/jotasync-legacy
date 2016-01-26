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
public class OptionSection extends TaskSection {

    public static final String KEY = "option";
    private static final String KEY_ADDITIONAL_OPTIONS = "additionalOptions";
    private static final String KEY_BACKUP = "backup";
    private static final String KEY_CHECKSUM = "checksum";
    private static final String KEY_COMPRESS = "compress";
    private static final String KEY_DELETE = "delete";
    private static final String KEY_DEVICES = "devices";
    private static final String KEY_DIRS = "dirs";
    private static final String KEY_EXISTING = "existing";
    private static final String KEY_GROUP = "group";
    private static final String KEY_HARD_LINKS = "hardLinks";
    private static final String KEY_IGNORE_EXISTING = "ignoreExisting";
    private static final String KEY_ITEMIZE_CHANGES = "itemizeChanges";
    private static final String KEY_LINKS = "links";
    private static final String KEY_MODIFY_WINDOW = "modifyWindow";
    private static final String KEY_NUMERIC_IDS = "numericIds";
    private static final String KEY_ONE_FILE_SYSTEM = "oneFilesystem";
    private static final String KEY_OPTIONS = "options";
    private static final String KEY_OWNER = "owner";
    private static final String KEY_PARTIAL_PROGRESS = "partialProgress";
    private static final String KEY_PERMS = "perms";
    private static final String KEY_PROGRESS = "progress";
    private static final String KEY_PROTECT_ARGS = "protectArgs";
    private static final String KEY_SIZE_ONLY = "sizeOnly";
    private static final String KEY_TIMES = "times";
    private static final String KEY_UPDATE = "update";
    private static final String KEY_VERBOSE = "verbose";

    private String mAdditionalOptions = "";
    private boolean mBackup;
    private boolean mChecksum;
    private boolean mCompress;
    private boolean mDelete;
    private boolean mDevices;
    private boolean mDirs;
    private boolean mExisting;
    private boolean mGroup;
    private boolean mHardLinks;
    private boolean mIgnoreExisting;
    private boolean mItemizeChanges;
    private boolean mLinks;
    private boolean mModifyWindow;
    private boolean mNumericIds;
    private boolean mOneFileSystem;
    private String mOptions = "";
    private boolean mOwner;
    private boolean mPartialProgress;
    private boolean mPerms;
    private boolean mProgress = true;
    private boolean mProtectArgs;
    private boolean mSizeOnly;
    private boolean mTimes;
    private boolean mUpdate;
    private boolean mVerbose = true;

    public String getAdditionalOptions() {
        return mAdditionalOptions;
    }

    @Override
    public List<String> getCommand() {
        mCommand.clear();

        for (String option : mOptions.split(" ")) {
            add(option);
        }

        if (mTimes) {
            add("--times");
        }
        if (mOwner) {
            add("--owner");
        }
        if (mPerms) {
            add("--perms");
        }
        if (mGroup) {
            add("--group");
        }
        if (mDelete) {
            add("--delete");
        }
        if (mVerbose) {
            add("--verbose");
        }
        if (mIgnoreExisting) {
            add("--ignore-existing");
        }
        if (mUpdate) {
            add("--update");
        }
        if (mOneFileSystem) {
            add("--one-file-system");
        }
        if (mProgress) {
            add("--progress");
        }
        if (mSizeOnly) {
            add("--size-only");
        }
        if (mModifyWindow) {
            add("--modify-window=1");
        }
        if (mChecksum) {
            add("--checksum");
        }
        if (mDevices) {
            add("--devices");
            add("--specials");
        }
        if (mPartialProgress) {
            add("--partial");
            add("--progress");
        }
        if (mLinks) {
            add("--links");
        }
        if (mBackup) {
            add("--backup");
        }
        if (mDirs) {
            add("--dirs");
        } else {
            add("--recursive");
        }
        if (mCompress) {
            add("--compress");
        }
        if (mExisting) {
            add("--existing");
        }
        if (mNumericIds) {
            add("--numeric-ids");
        }
        if (mHardLinks) {
            add("--hard-links");
        }
        if (mItemizeChanges) {
            add("--itemize-changes");
        }
        if (mProtectArgs) {
            add("--protect-args");
        }

        return mCommand;
    }

    @Override
    public JSONObject getJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(KEY_ADDITIONAL_OPTIONS, mAdditionalOptions);
        jsonObject.put(KEY_OPTIONS, mOptions);
        jsonObject.put(KEY_BACKUP, mBackup);
        jsonObject.put(KEY_CHECKSUM, mChecksum);
        jsonObject.put(KEY_COMPRESS, mCompress);
        jsonObject.put(KEY_DELETE, mDelete);
        jsonObject.put(KEY_DEVICES, mDevices);
        jsonObject.put(KEY_DIRS, mDirs);
        jsonObject.put(KEY_EXISTING, mExisting);
        jsonObject.put(KEY_GROUP, mGroup);
        jsonObject.put(KEY_HARD_LINKS, mHardLinks);
        jsonObject.put(KEY_IGNORE_EXISTING, mIgnoreExisting);
        jsonObject.put(KEY_ITEMIZE_CHANGES, mItemizeChanges);
        jsonObject.put(KEY_LINKS, mLinks);
        jsonObject.put(KEY_MODIFY_WINDOW, mModifyWindow);
        jsonObject.put(KEY_NUMERIC_IDS, mNumericIds);
        jsonObject.put(KEY_ONE_FILE_SYSTEM, mOneFileSystem);
        jsonObject.put(KEY_OWNER, mOwner);
        jsonObject.put(KEY_PARTIAL_PROGRESS, mPartialProgress);
        jsonObject.put(KEY_PERMS, mPerms);
        jsonObject.put(KEY_PROGRESS, mProgress);
        jsonObject.put(KEY_PROTECT_ARGS, mProtectArgs);
        jsonObject.put(KEY_SIZE_ONLY, mSizeOnly);
        jsonObject.put(KEY_TIMES, mTimes);
        jsonObject.put(KEY_UPDATE, mUpdate);
        jsonObject.put(KEY_VERBOSE, mVerbose);

        return jsonObject;
    }

    public String getOptions() {
        return mOptions;
    }

    public boolean isBackup() {
        return mBackup;
    }

    public boolean isChecksum() {
        return mChecksum;
    }

    public boolean isCompress() {
        return mCompress;
    }

    public boolean isDelete() {
        return mDelete;
    }

    public boolean isDevices() {
        return mDevices;
    }

    public boolean isDirs() {
        return mDirs;
    }

    public boolean isExisting() {
        return mExisting;
    }

    public boolean isGroup() {
        return mGroup;
    }

    public boolean isHardLinks() {
        return mHardLinks;
    }

    public boolean isIgnoreExisting() {
        return mIgnoreExisting;
    }

    public boolean isItemizeChanges() {
        return mItemizeChanges;
    }

    public boolean isLinks() {
        return mLinks;
    }

    public boolean isModifyWindow() {
        return mModifyWindow;
    }

    public boolean isNumericIds() {
        return mNumericIds;
    }

    public boolean isOneFileSystem() {
        return mOneFileSystem;
    }

    public boolean isOwner() {
        return mOwner;
    }

    public boolean isPartialProgress() {
        return mPartialProgress;
    }

    public boolean isPerms() {
        return mPerms;
    }

    public boolean isProgress() {
        return mProgress;
    }

    public boolean isProtectArgs() {
        return mProtectArgs;
    }

    public boolean isSizeOnly() {
        return mSizeOnly;
    }

    public boolean isTimes() {
        return mTimes;
    }

    public boolean isUpdate() {
        return mUpdate;
    }

    public boolean isVerbose() {
        return mVerbose;
    }

    @Override
    public void loadFromJson(JSONObject jsonObject) {
        mAdditionalOptions = optString(jsonObject, KEY_ADDITIONAL_OPTIONS);
        mOptions = optString(jsonObject, KEY_OPTIONS);
        mBackup = optBoolean(jsonObject, KEY_BACKUP);
        mChecksum = optBoolean(jsonObject, KEY_CHECKSUM);
        mCompress = optBoolean(jsonObject, KEY_COMPRESS);
        mDelete = optBoolean(jsonObject, KEY_DELETE);
        mDevices = optBoolean(jsonObject, KEY_DEVICES);
        mDirs = optBoolean(jsonObject, KEY_DIRS);
        mExisting = optBoolean(jsonObject, KEY_EXISTING);
        mGroup = optBoolean(jsonObject, KEY_GROUP);
        mHardLinks = optBoolean(jsonObject, KEY_HARD_LINKS);
        mIgnoreExisting = optBoolean(jsonObject, KEY_IGNORE_EXISTING);
        mItemizeChanges = optBoolean(jsonObject, KEY_ITEMIZE_CHANGES);
        mLinks = optBoolean(jsonObject, KEY_LINKS);
        mModifyWindow = optBoolean(jsonObject, KEY_MODIFY_WINDOW);
        mNumericIds = optBoolean(jsonObject, KEY_NUMERIC_IDS);
        mOneFileSystem = optBoolean(jsonObject, KEY_ONE_FILE_SYSTEM);
        mOwner = optBoolean(jsonObject, KEY_OWNER);
        mPartialProgress = optBoolean(jsonObject, KEY_PARTIAL_PROGRESS);
        mPerms = optBoolean(jsonObject, KEY_PERMS);
        mProgress = optBoolean(jsonObject, KEY_PROGRESS);
        mProtectArgs = optBoolean(jsonObject, KEY_PROTECT_ARGS);
        mSizeOnly = optBoolean(jsonObject, KEY_SIZE_ONLY);
        mTimes = optBoolean(jsonObject, KEY_TIMES);
        mUpdate = optBoolean(jsonObject, KEY_UPDATE);
        mVerbose = optBoolean(jsonObject, KEY_VERBOSE);
    }

    public void setAdditionalOptions(String value) {
        mAdditionalOptions = value;
    }

    public void setBackup(boolean value) {
        mBackup = value;
    }

    public void setChecksum(boolean value) {
        mChecksum = value;
    }

    public void setCompress(boolean value) {
        mCompress = value;
    }

    public void setDelete(boolean value) {
        mDelete = value;
    }

    public void setDevices(boolean value) {
        mDevices = value;
    }

    public void setDirs(boolean value) {
        mDirs = value;
    }

    public void setExisting(boolean value) {
        mExisting = value;
    }

    public void setGroup(boolean value) {
        mGroup = value;
    }

    public void setHardLinks(boolean value) {
        mHardLinks = value;
    }

    public void setIgnoreExisting(boolean value) {
        mIgnoreExisting = value;
    }

    public void setItemizeChanges(boolean value) {
        mItemizeChanges = value;
    }

    public void setLinks(boolean value) {
        mLinks = value;
    }

    public void setModifyWindow(boolean value) {
        mModifyWindow = value;
    }

    public void setNumericIds(boolean value) {
        mNumericIds = value;
    }

    public void setOneFileSystem(boolean value) {
        mOneFileSystem = value;
    }

    public void setOptions(String value) {
        mOptions = value;
    }

    public void setOwner(boolean value) {
        mOwner = value;
    }

    public void setPartialProgress(boolean value) {
        mPartialProgress = value;
    }

    public void setPerms(boolean value) {
        mPerms = value;
    }

    public void setProgress(boolean value) {
        mProgress = value;
    }

    public void setProtectArgs(boolean value) {
        mProtectArgs = value;
    }

    public void setSizeOnly(boolean value) {
        mSizeOnly = value;
    }

    public void setTimes(boolean value) {
        mTimes = value;
    }

    public void setUpdate(boolean value) {
        mUpdate = value;
    }

    public void setVerbose(boolean value) {
        mVerbose = value;
    }
}
