/*
 * Copyright 2018 Patrik Karlsson.
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
package se.trixon.jota.client.ui.editor.module.task;

import java.util.ResourceBundle;
import org.apache.commons.lang3.SystemUtils;
import se.trixon.almond.util.SystemHelper;
import static se.trixon.jota.shared.task.TaskSection.OPT_SEPARATOR;

/**
 *
 * @author Patrik Karlsson
 */
public enum ExcludeOption implements OptionHandler {
    //_DUMMY_WINDOWS("should only be visible on windows", SystemUtils.IS_OS_WINDOWS),
    TEMP_DIRS("--exclude=**/*tmp*/" + OPT_SEPARATOR + "--exclude=**/*temp*/" + OPT_SEPARATOR + "--exclude=**/*Tmp*/" + OPT_SEPARATOR + "--exclude=**/*Temp*/", true),
    BACKUP_FILES("--exclude=**~", true),
    CACHE_DIRS("--exclude=**/*cache*/" + OPT_SEPARATOR + "--exclude=**/*Cache*/", true),
    TRASH("--exclude=**/*Trash*/" + OPT_SEPARATOR + "--exclude=**/*trash*/", true),
    SYS_MOUNT_DIRS("--exclude=/mnt/*/**" + OPT_SEPARATOR + "--exclude=/media/*/**", SystemUtils.IS_OS_LINUX),
    SYS_DIRS("--exclude=/var/**" + OPT_SEPARATOR + "--exclude=/proc/**" + OPT_SEPARATOR + "--exclude=/dev/**" + OPT_SEPARATOR + "--exclude=/sys/**", SystemUtils.IS_OS_LINUX),
    LOST_FOUND("--exclude=**/lost+found*/", SystemUtils.IS_OS_LINUX),
    GVFS("--exclude=**/.gvfs/", SystemUtils.IS_OS_LINUX);
    private final boolean mActive;
    private final String mArg;
    private final ResourceBundle mBundle = SystemHelper.getBundle(ExcludeOption.class, "ExcludeOption");
    private final String mTitle;

    private ExcludeOption(String arg, boolean active) {
        mArg = arg;
        mActive = active;
        mTitle = mBundle.containsKey(name()) ? mBundle.getString(name()) : "_MISSING DESCRIPTION " + name();
    }

    @Override
    public boolean filter(String filter) {
        return getArg().toLowerCase().contains(filter.toLowerCase())
                || mTitle.toLowerCase().contains(filter.toLowerCase());
    }

    @Override
    public String getArg() {
        return mArg;
    }

    @Override
    public String getDynamicArg() {
        return "";
    }

    @Override
    public String getLongArg() {
        return "";
    }

    @Override
    public String getShortArg() {
        return "";
    }

    @Override
    public String getTitle() {
        return mTitle;
    }

    public boolean isActive() {
        return mActive;
    }

    @Override
    public void setDynamicArg(String dynamicArg) {
    }

    @Override
    public String toString() {
        return String.format("<html><b>%s</b><br />%s</html>", mTitle, mArg.replace(OPT_SEPARATOR, " "));
    }
}
