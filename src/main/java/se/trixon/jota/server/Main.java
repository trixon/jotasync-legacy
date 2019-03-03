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
package se.trixon.jota.server;

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.ResourceBundle;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.Xlog;
import se.trixon.jota.shared.Jota;

/**
 *
 * @author Patrik Karlström
 */
public class Main {

    private static final ResourceBundle sBundle = SystemHelper.getBundle(Jota.class, "Bundle");

    /**
     * @param args the command line arguments
     * @throws java.rmi.RemoteException
     */
    public static void main(String[] args) throws RemoteException, IOException {
        SystemHelper.enableRmiServer();
        Options options = initOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption("help")) {
                displayHelp(options);
            } else if (cmd.hasOption("version")) {
                displayVersion();
            } else {
                Server server = new Server(cmd);
            }
        } catch (ParseException ex) {
            Xlog.timedErr(ex.getMessage());
            System.out.println(sBundle.getString("parse_help_server"));
        }
    }

    private static void displayHelp(Options options) {
        String header = sBundle.getString("help_header");
        String footer = sBundle.getString("help_footer");

        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        formatter.printHelp("jotaserver", header, options, footer, true);
    }

    private static void displayVersion() {
        PomInfo pomInfo = new PomInfo(se.trixon.jota.client.Main.class, "se.trixon", "jotasync");
        System.out.println(String.format(sBundle.getString("version_info"), pomInfo.getVersion()));
    }

    private static Options initOptions() {
        String portString = Dict.PORT.toString().toLowerCase();
        Option help = new Option("?", "help", false, sBundle.getString("opt_help_desc"));
        Option version = new Option("v", "version", false, sBundle.getString("opt_version_desc"));
        Option port = Option.builder("p").longOpt("port").argName(portString).hasArg(true).desc(sBundle.getString("opt_port_server_desc")).build();

        Options options = new Options();
        options.addOption(help);
        options.addOption(version);
        options.addOption(port);

        return options;
    }
}
