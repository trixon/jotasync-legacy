/* 
 * Copyright 2017 Patrik Karlsson.
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
package se.trixon.jota.client;

import java.rmi.RemoteException;
import java.util.ResourceBundle;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import se.trixon.almond.util.BundleHelper;
import se.trixon.almond.util.Dict;
import se.trixon.almond.util.PomInfo;
import se.trixon.almond.util.SystemHelper;
import se.trixon.almond.util.Xlog;
import se.trixon.jota.shared.Jota;

/**
 *
 * @author Patrik Karlsson
 */
public class Main {

    private static final ResourceBundle sBundle = BundleHelper.getBundle(Jota.class, "Bundle");

    static final String OPT_CLIENT_PORT = "client-port";
    static final String OPT_CRON = "cron";
    static final String OPT_HELP = "help";
    static final String OPT_HOST = "host";
    static final String OPT_LIST_JOBS = "list-jobs";
    static final String OPT_LIST_TASKS = "list-tasks";
    static final String OPT_PORT = "port";
    static final String OPT_SHUTDOWN = "shutdown";
    static final String OPT_START = "start";
    static final String OPT_STATUS = "status";
    static final String OPT_STOP = "stop";
    static final String OPT_VERSION = "version";

    /**
     * @param args the command line arguments
     * @throws java.rmi.RemoteException
     */
    public static void main(String[] args) throws RemoteException {
        SystemHelper.enableRmiServer();
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
            System.out.println(sBundle.getString("parse_help_client"));
        }
    }

    private static void displayHelp(Options options) {
        String header = sBundle.getString("help_header");
        String footer = sBundle.getString("help_footer");

        HelpFormatter formatter = new HelpFormatter();
        formatter.setOptionComparator(null);
        formatter.printHelp("jotaclient", header, options, footer, true);
    }

    private static void displayVersion() {
        PomInfo pomInfo = new PomInfo(Main.class, "se.trixon", "jotasync");
        System.out.println(String.format(sBundle.getString("version_info"), pomInfo.getVersion()));
    }

    private static Options initOptions() {
        String hostString = Dict.HOST.toString().toLowerCase();
        String portString = Dict.PORT.toString().toLowerCase();
        String jobString = Dict.JOB.toString().toLowerCase();

        Option help = new Option("?", OPT_HELP, false, sBundle.getString("opt_help_desc"));
        Option version = new Option("v", OPT_VERSION, false, sBundle.getString("opt_version_desc"));
        Option host = Option.builder("h").longOpt(OPT_HOST).argName(hostString).hasArg(true).desc(sBundle.getString("opt_host_desc")).build();
        Option portHost = Option.builder("p").longOpt(OPT_PORT).argName(portString).hasArg(true).desc(sBundle.getString("opt_port_host_desc")).build();
        Option portClient = Option.builder("q").longOpt(OPT_CLIENT_PORT).argName(portString).hasArg(true).desc(sBundle.getString("opt_port_client_desc")).build();
        Option cron = Option.builder("c").longOpt(OPT_CRON).argName("on|off").hasArg(true).desc(sBundle.getString("opt_cron_desc")).build();
        Option listJobs = new Option("lj", OPT_LIST_JOBS, false, sBundle.getString("opt_list_jobs_desc"));
        Option listTasks = new Option("lt", OPT_LIST_TASKS, false, sBundle.getString("opt_list_tasks_desc"));
        Option start = Option.builder(null).longOpt(OPT_START).argName(jobString).hasArg(true).desc(sBundle.getString("opt_start_desc")).build();
        Option stop = Option.builder(null).longOpt(OPT_STOP).argName(jobString).hasArg(true).desc(sBundle.getString("opt_stop_desc")).build();
        Option shutdown = new Option("s", OPT_SHUTDOWN, false, sBundle.getString("opt_shutdown_desc"));
        Option status = new Option("u", OPT_STATUS, false, sBundle.getString("opt_status_desc"));

        Options options = new Options();
        options.addOption(help);
        options.addOption(version);
        options.addOption(host);
        options.addOption(portHost);
        options.addOption(portClient);
        options.addOption(listJobs);
        options.addOption(listTasks);
        options.addOption(start);
        options.addOption(stop);
        options.addOption(cron);
        options.addOption(shutdown);
        options.addOption(status);

        return options;
    }
}
