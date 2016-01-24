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

    PRESERVE_TIME("t", "times"),
    PRESERVE_OWNER("o", "owner"),
    PRESERVE_GROUP("g", "group"),
    PRESERVE_PERMISSION("p", "perms");
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
        return mShortArg.toLowerCase().contains(filter.toLowerCase())
                || mLongArg.toLowerCase().contains(filter.toLowerCase())
                || ("-" + mShortArg).toLowerCase().contains(filter.toLowerCase())
                || ("--" + mLongArg).toLowerCase().contains(filter.toLowerCase())
                || mDescription.toLowerCase().contains(filter.toLowerCase());
    }

    public String getDescription() {
        return mDescription;
    }

    @Override
    public String toString() {
        return String.format("<html><b>%s</b><br />-%s, --%s</html>", mDescription, mShortArg, mLongArg);
    }
}
