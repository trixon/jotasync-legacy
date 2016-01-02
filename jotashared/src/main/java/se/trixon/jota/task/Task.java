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
package se.trixon.jota.task;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author Patrik Karlsson
 */
public class Task implements Comparable<Task>, Serializable {

    private String mAdditionalOptions = "";
    private boolean mBackup;
    private boolean mChecksum;
    private final List<String> mCommand = new ArrayList<>();
    private boolean mCompress;
    private boolean mDelete;
    private String mDescription = "";
    private String mDestination = System.getProperty("user.home");
    private boolean mDevices;
    private boolean mDirs;
    private boolean mDryRun = true;
    private String mEnvironment = "";
    private boolean mExcludeTemplateBackup;
    private boolean mExcludeTemplateCache;
    private boolean mExcludeTemplateGvfs;
    private boolean mExcludeTemplateLostFound;
    private boolean mExcludeTemplateSystemDirs;
    private boolean mExcludeTemplateSystemMountDirs;
    private boolean mExcludeTemplateTemp;
    private boolean mExcludeTemplateTrash;
    private boolean mExisting;
    private boolean mGroup;
    private boolean mHardLinks;
    private String mHistory = "";
    private long mId = System.currentTimeMillis();
    private boolean mIgnoreExisting;
    private boolean mItemizeChanges;
    private boolean mLinks;
    private boolean mModifyWindow;
    private String mName = "";
    private boolean mNumericIds;
    private boolean mOneFileSystem;
    private boolean mOwner;
    private boolean mPartialProgress;
    private boolean mPerms;
    private boolean mPostExecute;
    private String mPostExecuteCommand = "";
    private boolean mPostExecuteOnError;
    private boolean mProgress = true;
    private boolean mProtectArgs;
    private boolean mRunBefore;
    private String mRunBeforeCommand = "";
    private boolean mRunBeforeHaltOnError;
    private boolean mSizeOnly;
    private String mSource = System.getProperty("user.home");
    private boolean mTimes;
    private int mType = 0;
    private boolean mUpdate;
    private boolean mVerbose = true;

    public Task() {
    }

    public List<String> build() {
        mCommand.clear();

        String command = "rsync";
//        if (SystemUtils.IS_OS_WINDOWS) {
//            File file = InstalledFileLocator.getDefault().locate("rsync.exe", "se.trixon.toolbox.rsync.windows", false);
//            command = file.getAbsolutePath();
//        }
        add(command);

        if (mDryRun) {
            add("--dry-run");
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

        if (mExcludeTemplateBackup) {
            add("--exclude=**~");
        }
        if (mExcludeTemplateCache) {
            add("--exclude=**/*cache*/");
            add("--exclude=**/*Cache*/");
        }
        if (mExcludeTemplateGvfs) {
            add("--exclude=**/.gvfs/");
        }
        if (mExcludeTemplateLostFound) {
            add("--exclude=**/lost+found*/");
        }
        if (mExcludeTemplateSystemDirs) {
            add("--exclude=/var/**");
            add("--exclude=/proc/**");
            add("--exclude=/dev/**");
            add("--exclude=/sys/**");
        }
        if (mExcludeTemplateSystemMountDirs) {
            add("--exclude=/mnt/*/**");
            add("--exclude=/media/*/**");
        }
        if (mExcludeTemplateTemp) {
            add("--exclude=**/*tmp*/");
        }
        if (mExcludeTemplateTrash) {
            add("--exclude=**/*Trash*/");
            add("--exclude=**/*trash*/");
        }

        add(mSource);
        add(mDestination);

        return mCommand;
    }

    @Override
    public int compareTo(Task o) {
        return mName.compareTo(o.getName());
    }

    public String getAdditionalOptions() {
        return mAdditionalOptions;
    }

    public String getCommandAsString() {
        build();
        return StringUtils.join(mCommand, " ");
    }

    public String getDescription() {
        return mDescription;
    }

    public String getDestination() {
        return mDestination;
    }

    public String getEnvironment() {
        return mEnvironment;
    }

    public String getHistory() {
        return mHistory;
    }

    public long getId() {
        return mId;
    }

    public String getName() {
        return mName;
    }

    public String getPostExecuteCommand() {
        return mPostExecuteCommand;
    }

    public String getRunBeforeCommand() {
        return mRunBeforeCommand;
    }

    public String getSource() {
        return mSource;
    }

    public int getType() {
        return mType;
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

    public boolean isDryRun() {
        return mDryRun;
    }

    public boolean isExcludeTemplateBackup() {
        return mExcludeTemplateBackup;
    }

    public boolean isExcludeTemplateCache() {
        return mExcludeTemplateCache;
    }

    public boolean isExcludeTemplateGvfs() {
        return mExcludeTemplateGvfs;
    }

    public boolean isExcludeTemplateLostFound() {
        return mExcludeTemplateLostFound;
    }

    public boolean isExcludeTemplateSystemDirs() {
        return mExcludeTemplateSystemDirs;
    }

    public boolean isExcludeTemplateSystemMountDirs() {
        return mExcludeTemplateSystemMountDirs;
    }

    public boolean isExcludeTemplateTemp() {
        return mExcludeTemplateTemp;
    }

    public boolean isExcludeTemplateTrash() {
        return mExcludeTemplateTrash;
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

    public boolean isPostExecute() {
        return mPostExecute;
    }

    public boolean isPostOnError() {
        return mPostExecuteOnError;
    }

    public boolean isProgress() {
        return mProgress;
    }

    public boolean isProtectArgs() {
        return mProtectArgs;
    }

    public boolean isRunBefore() {
        return mRunBefore;
    }

    public boolean isRunBeforeHaltOnError() {
        return mRunBeforeHaltOnError;
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

    public boolean isValid() {
        return !getName().isEmpty();
    }

    public boolean isVerbose() {
        return mVerbose;
    }

    public void setAdditionalOptions(String additionalOptions) {
        mAdditionalOptions = additionalOptions;
    }

    public void setBackup(boolean backup) {
        mBackup = backup;
    }

    public void setChecksum(boolean checksum) {
        mChecksum = checksum;
    }

    public void setCompress(boolean compress) {
        mCompress = compress;
    }

    public void setDelete(boolean delete) {
        mDelete = delete;
    }

    public void setDescription(String comment) {
        mDescription = comment;
    }

    public void setDestination(String destination) {
        mDestination = destination;
    }

    public void setDevices(boolean devices) {
        mDevices = devices;
    }

    public void setDirs(boolean dirs) {
        mDirs = dirs;
    }

    public void setDryRun(boolean dryRun) {
        mDryRun = dryRun;
    }

    public void setEnvironment(String environment) {
        mEnvironment = environment;
    }

    public void setExcludeTemplateBackup(boolean excludeTemplateBackup) {
        mExcludeTemplateBackup = excludeTemplateBackup;
    }

    public void setExcludeTemplateCache(boolean excludeTemplateCache) {
        mExcludeTemplateCache = excludeTemplateCache;
    }

    public void setExcludeTemplateGvfs(boolean excludeTemplateGvfs) {
        mExcludeTemplateGvfs = excludeTemplateGvfs;
    }

    public void setExcludeTemplateLostFound(boolean excludeTemplateLostFound) {
        mExcludeTemplateLostFound = excludeTemplateLostFound;
    }

    public void setExcludeTemplateSystemDirs(boolean excludeTemplateSystemDirs) {
        mExcludeTemplateSystemDirs = excludeTemplateSystemDirs;
    }

    public void setExcludeTemplateSystemMountDirs(boolean excludeTemplateSystemMountDirs) {
        mExcludeTemplateSystemMountDirs = excludeTemplateSystemMountDirs;
    }

    public void setExcludeTemplateTemp(boolean excludeTemplateTemp) {
        mExcludeTemplateTemp = excludeTemplateTemp;
    }

    public void setExcludeTemplateTrash(boolean excludeTemplateTrash) {
        mExcludeTemplateTrash = excludeTemplateTrash;
    }

    public void setExisting(boolean existing) {
        mExisting = existing;
    }

    public void setGroup(boolean group) {
        mGroup = group;
    }

    public void setHardLinks(boolean hardLinks) {
        mHardLinks = hardLinks;
    }

    public void setHistory(String history) {
        mHistory = history;
    }

    public void setId(long id) {
        mId = id;
    }

    public void setIgnoreExisting(boolean ignoreExisting) {
        mIgnoreExisting = ignoreExisting;
    }

    public void setItemizeChanges(boolean itemizeChanges) {
        mItemizeChanges = itemizeChanges;
    }

    public void setLinks(boolean links) {
        mLinks = links;
    }

    public void setModifyWindow(boolean modifyWindow) {
        mModifyWindow = modifyWindow;
    }

    public void setName(String name) {
        mName = name;
    }

    public void setNumericIds(boolean numericIds) {
        mNumericIds = numericIds;
    }

    public void setOneFileSystem(boolean oneFileSystem) {
        mOneFileSystem = oneFileSystem;
    }

    public void setOwner(boolean owner) {
        mOwner = owner;
    }

    public void setPartialProgress(boolean partialProgress) {
        mPartialProgress = partialProgress;
    }

    public void setPerms(boolean perms) {
        mPerms = perms;
    }

    public void setPostExecute(boolean postExecute) {
        mPostExecute = postExecute;
    }

    public void setPostExecuteCommand(String postCommand) {
        mPostExecuteCommand = postCommand;
    }

    public void setPostOnError(boolean postOnError) {
        mPostExecuteOnError = postOnError;
    }

    public void setProgress(boolean progress) {
        mProgress = progress;
    }

    public void setProtectArgs(boolean protectArgs) {
        mProtectArgs = protectArgs;
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

    public void setSizeOnly(boolean sizeOnly) {
        mSizeOnly = sizeOnly;
    }

    public void setSource(String source) {
        mSource = source;
    }

    public void setTimes(boolean times) {
        mTimes = times;
    }

    public void setType(int type) {
        mType = type;
    }

    public void setUpdate(boolean update) {
        mUpdate = update;
    }

    public void setVerbose(boolean verbose) {
        mVerbose = verbose;
    }

    @Override
    public String toString() {
        String description = "";

        try {
            description = mDescription.split("\\n")[0];
        } catch (Exception e) {
            // nvm
        }

        return String.format("<html><b>%s</b><br /><i>%s</i></html>", mName, description);
    }

    private void add(String command) {
        if (!mCommand.contains(command)) {
            mCommand.add(command);
        }
    }
}
