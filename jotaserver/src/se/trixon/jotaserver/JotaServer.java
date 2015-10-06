/*
 * Copyright 2015 Patrik Karlsson.
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
package se.trixon.jotaserver;

import java.util.ResourceBundle;
import org.apache.commons.cli.CommandLine;
import se.trixon.util.BundleHelper;
import se.trixon.util.Xlog;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class JotaServer {

    public static final int DEFAULT_PORT = 1099;

    private final ResourceBundle mBundle = BundleHelper.getBundle(Main.class, "Bundle");

    public JotaServer(CommandLine cmd) {
        if (cmd.hasOption("port")) {
            String port = cmd.getOptionValue("port");
            try {
                //mConnectionManager.setPort(Integer.valueOf(port));
            } catch (NumberFormatException e) {
                Xlog.timedErr(String.format(mBundle.getString("invalid_port"), port, DEFAULT_PORT));
            }
        }

        //mConnectionManager.startServer();
    }

}
