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
package se.trixon.jota.client.ui.editor.module.task;

import java.util.ResourceBundle;
import se.trixon.util.BundleHelper;

/**
 *
 * @author Patrik Karlsson
 */
public enum RsyncOption {

    SAFE_LINKS(null, "safe-links"),
    PRESERVE_TIME("t", "times"),
    PRESERVE_OWNER("o", "owner"),
    PRESERVE_GROUP("g", "group"),
    PRESERVE_PERMISSION("p", "perms"),
    PARTIAL_PROGRESS("P", null);
    private final ResourceBundle mBundle = BundleHelper.getBundle(RsyncOption.class, "RsyncOption");
    private final String mDescription;
    private final String mLongArg;
    private final String mShortArg;

    private RsyncOption(String shortArg, String longArg) {
        mShortArg = shortArg;
        mLongArg = longArg;
        mDescription = mBundle.getString(name());
    }

    public boolean filter(String filter) {
        return getShortArg().toLowerCase().contains(filter.toLowerCase())
                || getLongArg().toLowerCase().contains(filter.toLowerCase())
                || mDescription.toLowerCase().contains(filter.toLowerCase());
    }

    public String getArg() {
        if (mLongArg != null) {
            return getLongArg();
        } else {
            return getShortArg();
        }
    }

    public String getDescription() {
        return mDescription;
    }

    public String getLongArg() {
        if (mLongArg != null) {
            return "--" + mLongArg;
        } else {
            return "";
        }
    }

    public String getShortArg() {
        if (mShortArg != null) {
            return "-" + mShortArg;
        } else {
            return "";
        }
    }

    @Override
    public String toString() {
        String separator = (mShortArg == null || mLongArg == null) ? "" : ", ";

        return String.format("<html><b>%s</b><br />%s%s%s</html>", mDescription, getShortArg(), separator, getLongArg());
    }
}
