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
package se.trixon.jotaclient;

import java.rmi.RemoteException;
import java.util.ResourceBundle;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import se.trixon.jota.Jota;
import se.trixon.util.SystemHelper;
import se.trixon.util.Xlog;

/**
 *
 * @author Patrik Karlsson <patrik@trixon.se>
 */
public class Main {

    private final ResourceBundle mJotaBundle = Jota.getBundle();
    static final String OPT_CLIENT_PORT = "client-port";
    static final String OPT_CRON = "cron";
    static final String OPT_HELP = "help";
    static final String OPT_HOST = "host";
    static final String OPT_LIST_JOBS = "list-jobs";
    static final String OPT_LIST_TASKS = "list-tasks";
    static final String OPT_PORT = "port";
    static final String OPT_SHUTDOWN = "shutdown";
    static final String OPT_STATUS = "status";
    static final String OPT_VERSION = "version";
    /**
     * @param args the command line arguments
     * @throws java.rmi.RemoteException
     */
    public static void main(String[] args) throws RemoteException {
        System.setProperty("java.rmi.server.hostname", SystemHelper.getHostname());
        Options options = initOptions();
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            if (cmd.hasOption(Main.OPT_HELP)) {
                displayHelp(options);
            } else if (cmd.hasOption(OPT_VERSION)) {
                displayVersion();
            } else {
                Client client = new Client(cmd);
            }
        } catch (ParseException ex) {
            Xlog.timedErr(ex.getMessage());
            System.out.println("Try 'jotaclient --help' for more information.");
        }
    }

    private static void displayHelp(Options options) {
        String header = "rsync front end with built in cron\n\n";
        String footer = "\nPlease report issues to patrik@trixon.se";

        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        formatter.printHelp("jotaclient", header, options, footer, true);
    }

    private static void displayVersion() {
        System.out.println(Jota.getVersionInfo("jotaclient"));
    }

    private static Options initOptions() {
        Option help = new Option("?", OPT_HELP, false, "print this message");
        Option version = new Option("v", OPT_VERSION, false, "print the version information and exit\n");
        Option host = Option.builder("h").longOpt(OPT_HOST).argName("host").hasArg(true).desc("connect to server at host [hostname]").build();
        Option portHost = Option.builder("p").longOpt(OPT_PORT).argName("port").hasArg(true).desc("connect to server at port [1099]").build();
        Option portClient = Option.builder("q").longOpt(OPT_CLIENT_PORT).argName("port").hasArg(true).desc("client callback port [1199]\n").build();
        Option cron = Option.builder("c").longOpt(OPT_CRON).argName("on|off").hasArg(true).desc("turn internal cron on or off").build();
        Option listJobs = new Option("lj", OPT_LIST_JOBS, false, "list jobs");
        Option listTasks = new Option("lt", OPT_LIST_TASKS, false, "list tasks");
        Option shutdown = new Option("s", OPT_SHUTDOWN, false, "shutdown jotasync");
        Option status = new Option("u", OPT_STATUS, false, "print status information");

        Options options = new Options();
        options.addOption(help);
        options.addOption(version);
        options.addOption(host);
        options.addOption(portHost);
        options.addOption(portClient);
        options.addOption(listJobs);
        options.addOption(listTasks);
        options.addOption(cron);
        options.addOption(shutdown);
        options.addOption(status);

        return options;
    }
}
