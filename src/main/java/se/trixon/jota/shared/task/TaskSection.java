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
package se.trixon.jota.shared.task;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Patrik Karlström
 */
public abstract class TaskSection {

    public static final String OPT_SEPARATOR = ":::";
    protected transient final List<String> mCommand = new ArrayList<>();

    public abstract List<String> getCommand();

    protected void add(String command) {
        if (!mCommand.contains(command)) {
            mCommand.add(command);
        }
    }
}
