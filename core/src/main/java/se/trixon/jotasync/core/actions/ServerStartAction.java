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
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.rmi.NotBoundException;
import java.util.ArrayList;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle.Messages;
import se.trixon.almond.util.Dict;
import se.trixon.jota.client.ClientOptions;
import se.trixon.jotasync.core.api.Jota;

@ActionID(
        category = "File",
        id = "se.trixon.jotasync.core.actions.ServerStartAction"
)
@ActionRegistration(
        displayName = "#CTL_ServerStartAction"
)
@ActionReferences({
    @ActionReference(path = "Menu/File/Server", position = 10),
    @ActionReference(path = "Shortcuts", name = "DS-O")
})
@Messages("CTL_ServerStartAction=&Start")
public final class ServerStartAction implements ActionListener {

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            serverStart();
        } catch (URISyntaxException | IOException | NotBoundException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public void serverStart() throws URISyntaxException, IOException, NotBoundException {
        Jota.log(Dict.SERVER_START.toString());

        var sb = new StringBuilder();
        sb.append(System.getProperty("java.home")).append(File.separator)
                .append("bin").append(File.separator)
                .append("java");

        if (SystemUtils.IS_OS_WINDOWS) {
            sb.append(".exe");
        }

        var nbHome = new File(System.getProperty("netbeans.home"));
        var extDir = new File(nbHome.getParentFile(), "jotasync/modules/ext/se.trixon.jotasync.core");
        String engineName = "se-trixon-jotasync/engine.jar";
        var jarFile = new File(extDir, engineName);

        ArrayList<String> cp = new ArrayList<>();
        cp.add(jarFile.getAbsolutePath());
        cp.add(nbHome.getAbsolutePath() + "/lib/org-openide-util.jar");
        cp.add(nbHome.getAbsolutePath() + "/lib/org-openide-util-lookup.jar");

        for (var file : FileUtils.listFiles(extDir, new String[]{"jar"}, true)) {
            if (!file.getAbsolutePath().equalsIgnoreCase(engineName)) {
                cp.add(file.getAbsolutePath());
            }
        }

        var options = ClientOptions.getInstance();
        ArrayList<String> command = new ArrayList<>();
        command.add(sb.toString());
        command.add("-cp");
        command.add(String.join(":", cp));
        command.add("Server");
        command.add("--port");
        command.add(String.valueOf(options.getAutostartServerPort()));

        Jota.log(StringUtils.join(command, " "));
        new ProcessBuilder(command).inheritIO().start();
        try {
            Thread.sleep(options.getAutostartServerConnectDelay());
        } catch (InterruptedException ex) {
            Jota.err(ex.getLocalizedMessage());
        }
    }
}
