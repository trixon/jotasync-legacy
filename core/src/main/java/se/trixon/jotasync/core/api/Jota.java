/*
 * Copyright 2021 Patrik Karlström.
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
package se.trixon.jotasync.core.api;

import se.trixon.almond.util.Log;

/**
 *
 * @author Patrik Karlström
 */
public class Jota {

    private static final Log sLog = new Log();

    public static void err(String line) {
        sLog.timedErr(line);
    }

    public static void err(String category, String item) {
        sLog.timedErr(String.format("%s: %s ", category, item));
    }

    public static Log getLog() {
        return sLog;
    }

    public static void log(String category, String item) {
        sLog.timedOut(String.format("%s: %s ", category, item));
    }

    public static void log(String line) {
        sLog.timedOut(line);
    }
}
