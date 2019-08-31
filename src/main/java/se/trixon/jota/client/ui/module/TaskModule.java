/*
 * Copyright 2019 Patrik Karlström.
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
package se.trixon.jota.client.ui.module;

import com.dlsc.workbenchfx.model.WorkbenchModule;
import javafx.scene.Node;
import javafx.scene.control.Label;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jota.client.ui.MainApp;

/**
 *
 * @author Patrik Karlström
 */
public class TaskModule extends WorkbenchModule {

    public TaskModule() {
        super(Dict.TASK.toString(), MaterialIcon._Maps.DIRECTIONS_RUN.getImageView(MainApp.MODULE_ICON_SIZE).getImage());
    }

    @Override
    public Node activate() {
        return new Label("task");
    }

}
