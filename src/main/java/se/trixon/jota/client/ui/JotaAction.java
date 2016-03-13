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
package se.trixon.jota.client.ui;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import se.trixon.jota.client.ClientOptions;
import static se.trixon.jota.client.ui.MainFrame.ActionManager.JOTA_SMALL_ICON_KEY;
import se.trixon.util.icon.Pict;
import se.trixon.util.icons.IconColor;
import se.trixon.util.icons.material.MaterialIcon;

/**
 *
 * @author Patrik Karlsson
 */
public abstract class JotaAction extends AbstractAction {

    private Enum mIconEnum = null;

    public JotaAction() {
        super();
    }

    public JotaAction(String name) {
        super(name);
    }

    public JotaAction(String name, Icon icon) {
        super(name, icon);
    }

    public Enum getIconEnum() {
        return mIconEnum;
    }

    public void setIconEnum(Enum iconEnum) {
        mIconEnum = iconEnum;
    }

    public void updateIcon() {
        IconColor iconColor = ClientOptions.INSTANCE.getIconTheme() == 0 ? IconColor.BLACK : IconColor.WHITE;

        if (mIconEnum != null) {
            if (mIconEnum instanceof Pict.Actions) {
                Pict.Actions icon = (Pict.Actions) mIconEnum;
                putValue(Action.LARGE_ICON_KEY, icon.get(UI.ICON_SIZE_LARGE));
                putValue(JOTA_SMALL_ICON_KEY, icon.get(UI.ICON_SIZE_SMALL));
            } else if (mIconEnum instanceof Pict.Apps) {
                Pict.Apps icon = (Pict.Apps) mIconEnum;
                putValue(Action.LARGE_ICON_KEY, icon.get(UI.ICON_SIZE_LARGE));
                putValue(JOTA_SMALL_ICON_KEY, icon.get(UI.ICON_SIZE_SMALL));
            } else if (mIconEnum instanceof Pict.Places) {
                Pict.Places icon = (Pict.Places) mIconEnum;
                putValue(Action.LARGE_ICON_KEY, icon.get(UI.ICON_SIZE_LARGE));
                putValue(JOTA_SMALL_ICON_KEY, icon.get(UI.ICON_SIZE_SMALL));
            } else if (mIconEnum instanceof MaterialIcon.Action) {
                MaterialIcon.Action icon = (MaterialIcon.Action) mIconEnum;
                putValue(Action.LARGE_ICON_KEY, icon.get(UI.ICON_SIZE_LARGE, iconColor));
                putValue(JOTA_SMALL_ICON_KEY, icon.get(UI.ICON_SIZE_SMALL, iconColor));
            } else if (mIconEnum instanceof MaterialIcon.Av) {
                MaterialIcon.Av icon = (MaterialIcon.Av) mIconEnum;
                putValue(Action.LARGE_ICON_KEY, icon.get(UI.ICON_SIZE_LARGE, iconColor));
                putValue(JOTA_SMALL_ICON_KEY, icon.get(UI.ICON_SIZE_SMALL, iconColor));
            } else if (mIconEnum instanceof MaterialIcon.Communication) {
                MaterialIcon.Communication icon = (MaterialIcon.Communication) mIconEnum;
                putValue(Action.LARGE_ICON_KEY, icon.get(UI.ICON_SIZE_LARGE, iconColor));
                putValue(JOTA_SMALL_ICON_KEY, icon.get(UI.ICON_SIZE_SMALL, iconColor));
            } else if (mIconEnum instanceof MaterialIcon.Content) {
                MaterialIcon.Content icon = (MaterialIcon.Content) mIconEnum;
                putValue(Action.LARGE_ICON_KEY, icon.get(UI.ICON_SIZE_LARGE, iconColor));
                putValue(JOTA_SMALL_ICON_KEY, icon.get(UI.ICON_SIZE_SMALL, iconColor));
            } else if (mIconEnum instanceof MaterialIcon.Editor) {
                MaterialIcon.Editor icon = (MaterialIcon.Editor) mIconEnum;
                putValue(Action.LARGE_ICON_KEY, icon.get(UI.ICON_SIZE_LARGE, iconColor));
                putValue(JOTA_SMALL_ICON_KEY, icon.get(UI.ICON_SIZE_SMALL, iconColor));
            } else if (mIconEnum instanceof MaterialIcon.Navigation) {
                MaterialIcon.Navigation icon = (MaterialIcon.Navigation) mIconEnum;
                putValue(Action.LARGE_ICON_KEY, icon.get(UI.ICON_SIZE_LARGE, iconColor));
                putValue(JOTA_SMALL_ICON_KEY, icon.get(UI.ICON_SIZE_SMALL, iconColor));
            }
        }
    }
}
