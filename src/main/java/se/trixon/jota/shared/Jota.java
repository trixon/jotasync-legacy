/* 
 * Copyright 2022 Patrik Karlström.
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
package se.trixon.jota.shared;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ResourceBundle;
import se.trixon.almond.util.SystemHelper;

/**
 *
 * @author Patrik Karlström
 */
public class Jota {

    public static final int DEFAULT_PORT_CLIENT = 1199;
    public static final int DEFAULT_PORT_HOST = 1099;
    public static final String TASK_SEPARATOR = ",";
    private static final ResourceBundle sBundle = SystemHelper.getBundle(Jota.class, "Bundle");

    public static void exit() {
        exit(0);
    }

    public static void exit(int status) {
        System.exit(status);
    }

    public static ResourceBundle getBundle() {
        return sBundle;
    }

    public static String millisToDateTime(long timestamp) {
        Date date = new Date(timestamp);
        return new SimpleDateFormat("yyyy-MM-dd HH.mm.ss").format(date);
    }

    public static String nowToDateTime() {
        return millisToDateTime(System.currentTimeMillis());
    }
}
