/*
 * Copyright 2020 Patrik Karlström.
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
package se.trixon.jotasync.core.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.NbBundle.Messages;
import se.trixon.jota.client.Client;
import se.trixon.jota.client.Manager;

@ActionID(
        category = "File",
        id = "se.trixon.jotasync.core.actions.ServerShutdownAction"
)
@ActionRegistration(
        displayName = "#CTL_ServerShutdownAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/File/Server", position = 20),
    @ActionReference(path = "Shortcuts", name = "DS-D")
})
@Messages("CTL_ServerShutdownAction=Shut&down")
public final class ServerShutdownAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent actionEvent) {
        Manager.getInstance().getClient().execute(Client.Command.SHUTDOWN);
    }
}
