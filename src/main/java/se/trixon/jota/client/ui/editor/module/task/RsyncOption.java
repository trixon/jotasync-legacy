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
public enum RsyncOption implements OptionHandler {

    ACLS("A", "acls"),
    APPEND(null, "append"),
    APPEND_VERIFY(null, "append-verify"),
    ARCHIVE("a", "archive"),
    BACKUP("b", "backup"),
    BLOCKING_IO(null, "blocking-io"),
    CHECKSUM("c", "checksum"),
    COMPRESS("z", "compress"),
    COPY_DIRLINKS("k", "copy-dirlinks"),
    COPY_LINKS("L", "copy-links"),
    COPY_UNSAFE_LINKS(null, "copy-unsafe-links"),
    CVS_EXLUDE("C", "cvs-exclude"),
    D("D", null),
    DEL(null, "del"),
    DELAY_UPDATES(null, "delay-updates"),
    DELETE(null, "delete"),
    DELETE_AFTER(null, "delete-after"),
    DELETE_BEFORE(null, "delete-before"),
    DELETE_DELAY(null, "delete-delay"),
    DELETE_DURING(null, "delete-during"),
    DELETE_EXCLUDED(null, "delete-excluded"),
    DELETE_MISSING_ARGS(null, "delete-missing-args"),
    DEVICES(null, "devices"),
    DIRS("d", "dirs"),
    EXECUTABILITY("E", "executability"),
    EXISTING(null, "existing"),
    FORCE(null, "force"),
    FROM0("0", "from0"),
    FUZZY("y", "fuzzy"),
    HARD_LINKS("H", "hard-links"),
    HUMAN_READABLE("h", "human-readable"),
    IGNORE_ERRORS(null, "ignore-errors"),
    IGNORE_EXISTING(null, "ignore-existing"),
    IGNORE_MISSING_ARGS(null, "ignore-missing-args"),
    IGNORE_TIMES("I", "ignore-times"),
    INPLACE(null, "inplace"),
    IPV4("4", "ipv4"),
    IPV6("6", "ipv6"),
    ITEMIZE_CHANGES("i", "itemize-changes "),
    KEEP_DIRLINKS("K", "keep-dirlinks"),
    LINKS("l", "links"),
    LIST_ONLY(null, "list-only"),
    MSGS2STDERR(null, "msgs2stderr"),
    MUNGE_LINKS(null, "munge-links"),
    NO_IMPLIED_DIRS(null, "no-implied-dirs"),
    NO_MOTD(null, "no-motd"),
    NUMERIC_IDS(null, "numeric-ids"),
    OMIT_DIR_TIMES("O", "omit-dir-times"),
    OMIT_LINK_TIMES("J", "omit-link-times"),
    ONE_FILE_SYSTEM("x", "one-file-system"),
    PARTIAL(null, "partial"),
    PARTIAL_PROGRESS("P", null),
    PREALLOCATE(null, "preallocate"),
    PRESERVE_GROUP("g", "group"),
    PRESERVE_OWNER("o", "owner"),
    PRESERVE_PERMISSION("p", "perms"),
    PRESERVE_TIME("t", "times"),
    PROGRESS(null, "progress"),
    PROTECT_ARGS("s", "protect-args"),
    PRUNE_EMPTY_DIRS("m", "prune-empty-dirs"),
    QUIET("q", "quiet"),
    RECURSIVE("r", "recursive"),
    RELATIVE("R", "relative"),
    REMOVE_SOURCE_FILES(null, "remove-source-files"),
    SAFE_LINKS(null, "safe-links"),
    SIZE_ONLY(null, "size-only"),
    SPARSE("s", "sparse"),
    SPECIALS(null, "specials"),
    STATS(null, "stats"),
    SUPER(null, "super"),
    UPDATE("u", "update"),
    VERBOSE("v", "verbose"),
    WHOLE_FILE("W", "whole-file"),
    _8_BIT_OUTPUT("8", "8-bit-output ");

    private final ResourceBundle mBundle = BundleHelper.getBundle(RsyncOption.class, "RsyncOption");
    private final String mLongArg;
    private final String mShortArg;
    private final String mTitle;

    private RsyncOption(String shortArg, String longArg) {
        mShortArg = shortArg;
        mLongArg = longArg;
        mTitle = mBundle.containsKey(name()) ? mBundle.getString(name()) : "_MISSING DESCRIPTION " + name();
    }

    @Override
    public boolean filter(String filter) {
        return getShortArg().toLowerCase().contains(filter.toLowerCase())
                || getLongArg().toLowerCase().contains(filter.toLowerCase())
                || mTitle.toLowerCase().contains(filter.toLowerCase());
    }

    @Override
    public String getArg() {
        if (mLongArg != null) {
            return getLongArg();
        } else {
            return getShortArg();
        }
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
    public String getTitle() {
        return mTitle;
    }

    @Override
    public String toString() {
        String separator = (mShortArg == null || mLongArg == null) ? "" : ", ";

        return String.format("<html><b>%s</b><br />%s%s%s</html>", mTitle, getShortArg(), separator, getLongArg());
    }
}
