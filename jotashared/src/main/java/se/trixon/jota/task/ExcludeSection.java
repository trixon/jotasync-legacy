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

import java.util.List;
import org.json.simple.JSONObject;

/**
 *
 * @author Patrik Karlsson
 */
public class ExcludeSection extends TaskSection {

    public static final String KEY = "exclude";
    public static final String KEY_TEMPLATE_BACKUP = "templateBackup";
    public static final String KEY_TEMPLATE_CACHE = "templateCahce";
    public static final String KEY_TEMPLATE_GVFS = "templateGvfs";
    public static final String KEY_TEMPLATE_LOST_FOUND = "templateLostFound";
    public static final String KEY_TEMPLATE_SYSTEM_DIRS = "templateSystemDirs";
    public static final String KEY_TEMPLATE_SYSTEM_MOUNT_DIRS = "templateSystemMountDirs";
    public static final String KEY_TEMPLATE_TEMP = "templateTemp";
    public static final String KEY_TEMPLATE_TRASH = "templateTrash";

    private boolean mExcludeTemplateBackup;
    private boolean mExcludeTemplateCache;
    private boolean mExcludeTemplateGvfs;
    private boolean mExcludeTemplateLostFound;
    private boolean mExcludeTemplateSystemDirs;
    private boolean mExcludeTemplateSystemMountDirs;
    private boolean mExcludeTemplateTemp;
    private boolean mExcludeTemplateTrash;

    @Override
    public List<String> getCommand() {
        mCommand.clear();

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

        return mCommand;
    }

    @Override
    public JSONObject getJson() {
        JSONObject jsonObject = new JSONObject();

        jsonObject.put(KEY_TEMPLATE_BACKUP, isExcludeTemplateBackup());
        jsonObject.put(KEY_TEMPLATE_CACHE, isExcludeTemplateCache());
        jsonObject.put(KEY_TEMPLATE_GVFS, isExcludeTemplateGvfs());
        jsonObject.put(KEY_TEMPLATE_LOST_FOUND, isExcludeTemplateLostFound());
        jsonObject.put(KEY_TEMPLATE_SYSTEM_DIRS, isExcludeTemplateSystemDirs());
        jsonObject.put(KEY_TEMPLATE_SYSTEM_MOUNT_DIRS, isExcludeTemplateSystemMountDirs());
        jsonObject.put(KEY_TEMPLATE_TEMP, isExcludeTemplateTemp());
        jsonObject.put(KEY_TEMPLATE_TRASH, isExcludeTemplateTrash());

        return jsonObject;
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

    public void setExcludeTemplateBackup(boolean value) {
        mExcludeTemplateBackup = value;
    }

    public void setExcludeTemplateCache(boolean value) {
        mExcludeTemplateCache = value;
    }

    public void setExcludeTemplateGvfs(boolean value) {
        mExcludeTemplateGvfs = value;
    }

    public void setExcludeTemplateLostFound(boolean value) {
        mExcludeTemplateLostFound = value;
    }

    public void setExcludeTemplateSystemDirs(boolean value) {
        mExcludeTemplateSystemDirs = value;
    }

    public void setExcludeTemplateSystemMountDirs(boolean value) {
        mExcludeTemplateSystemMountDirs = value;
    }

    public void setExcludeTemplateTemp(boolean value) {
        mExcludeTemplateTemp = value;
    }

    public void setExcludeTemplateTrash(boolean value) {
        mExcludeTemplateTrash = value;
    }

    @Override
    public void setJson(JSONObject jsonObject) {
        mExcludeTemplateBackup = (boolean) jsonObject.get(KEY_TEMPLATE_BACKUP);
        mExcludeTemplateCache = (boolean) jsonObject.get(KEY_TEMPLATE_CACHE);
        mExcludeTemplateGvfs = (boolean) jsonObject.get(KEY_TEMPLATE_GVFS);
        mExcludeTemplateLostFound = (boolean) jsonObject.get(KEY_TEMPLATE_LOST_FOUND);
        mExcludeTemplateSystemDirs = (boolean) jsonObject.get(KEY_TEMPLATE_SYSTEM_DIRS);
        mExcludeTemplateSystemMountDirs = (boolean) jsonObject.get(KEY_TEMPLATE_SYSTEM_MOUNT_DIRS);
        mExcludeTemplateTemp = (boolean) jsonObject.get(KEY_TEMPLATE_TEMP);
        mExcludeTemplateTrash = (boolean) jsonObject.get(KEY_TEMPLATE_TRASH);
    }
}
