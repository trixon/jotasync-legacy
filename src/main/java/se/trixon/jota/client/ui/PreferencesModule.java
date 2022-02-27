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
package se.trixon.jota.client.ui;

import com.dlsc.workbenchfx.view.controls.ToolbarItem;
import java.util.ResourceBundle;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.ButtonType;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.icons.material.MaterialIcon;
import se.trixon.jota.client.Preferences;
import static se.trixon.jota.client.ui.MainApp.*;

/**
 *
 * @author Patrik Karlström
 */
public class PreferencesModule extends BaseModule {

    private final ResourceBundle mBundle = SystemHelper.getBundle(MainApp.class, "Bundle");

    public PreferencesModule(Scene scene) {
        super(scene, Dict.OPTIONS.toString(), MaterialIcon._Action.SETTINGS.getImageView(ICON_SIZE_MODULE).getImage());

        createUI();
    }

    @Override
    public Node activate() {
        return Preferences.getInstance().getPreferencesFxView();
    }

    @Override
    public boolean destroy() {
        Preferences.getInstance().save();
        return true;
    }

    private void createUI() {
        ToolbarItem saveToolbarItem = new ToolbarItem(MaterialIcon._Content.SAVE.getImageView(ICON_SIZE_MODULE_TOOLBAR),
                event -> Preferences.getInstance().save());

        ToolbarItem discardToolbarItem = new ToolbarItem(MaterialIcon._Action.DELETE.getImageView(ICON_SIZE_MODULE_TOOLBAR),
                event -> getWorkbench().showConfirmationDialog(mBundle.getString("prefs.ui.discard_title"),
                        mBundle.getString("prefs.ui.discard_message"),
                        buttonType -> {
                            if (ButtonType.YES.equals(buttonType)) {
                                Preferences.getInstance().discardChanges();
                            }
                        })
        );

        getToolbarControlsLeft().addAll(saveToolbarItem, discardToolbarItem);
    }
}
