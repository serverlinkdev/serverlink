package org.uigl.ut2004.serverlink;

import org.ini4j.Wini;
import org.uigl.ut2004.serverlink.database.DatabaseRepositoryProvider;
import org.uigl.ut2004.serverlink.database.SQLiteDatabaseRepositoryImpl;
import org.uigl.ut2004.serverlink.exception.AuthenticationException;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.URISyntaxException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ServerLinkApplication {

    private static final int DEFAULT_PORT = 9090;
    private static final int DEFAULT_THREADS = 16;

   public static void main(String[] argv) throws IOException, URISyntaxException, AuthenticationException {
       System.out.println("    _____                                _       _         _    ");
       System.out.println("   / ____|                              | |     (_)       | |   ");
       System.out.println("  | (___    ___  _ __ __   __ ___  _ __ | |      _  _ __  | | __");
       System.out.println("   \\___ \\  / _ \\| '__|\\ \\ / // _ \\| '__|| |     | || '_ \\ | |/ /");
       System.out.println("   ____) ||  __/| |    \\ V /|  __/| |   | |____ | || | | ||   < ");
       System.out.println("  |_____/  \\___||_|     \\_/  \\___||_|   |______||_||_| |_||_|\\_\\");
       System.out.println("                                                                ");
       System.out.println("\tBrought to you by the UT2004/UT99 Community\n\t" +
               "  Visit us at https://discord.gg/tuzcxqW\n");

       String helpInfo="- If this is your first time using serverlink\n" +
                           "run this program again as: \n\n" +
                           "    'java -jar serverlink-1.0.1-win10x64-openjdk1.8.0_152.jar wizard'\n\n" +
                           "- If you are running this on an existing installation \n" +
                           "run this program as: \n\n" +
                           "    'java -jar serverlink.jar ServerLink.ini ServerLink.db'\n\n" +
                           "Your config file must end with '.ini' and your database must end with '.db'\n" +
                           "Your config file and database file must be in same directory as serverlink jar file";

        if (argv.length == 0) {
            System.out.println("\nWelcome to ServerLink!\n\n" + helpInfo);
            System.exit(0);
        } else if ((argv.length == 1) && (argv[0].equals("wizard"))) {
            System.out.println("wizard was requested");
            //TODO add wizard code
            Wizard wizard = new Wizard();
            wizard.questionnaire();
            System.exit(0);
        }  else if ((argv.length == 2) && (!( (argv[0].endsWith(".ini")) && (argv[1].endsWith(".db"))))){
            System.out.println("\nFAIL:  Your command line arguments are wrong.\n\n" + helpInfo);
            System.exit(1);
        } else if ((argv.length == 2) && (( (argv[0].endsWith(".ini")) && (argv[1].endsWith(".db"))))){
            // jarPath = URLDecoder.decode(String.valueOf(jarPathEncoded), "UTF-8"); // use if on non utf-8 plaform

            // Get full folder path to the jar file:
            String jarParent = new File(ServerLinkApplication.class.getProtectionDomain().getCodeSource().
                                       getLocation().toURI()).getParent();

            // Get file path for the users ini file:
            String iniPath = new StringBuilder(jarParent + "\\" + argv[0]).toString();
            File iniFile = new File(iniPath);

            // Get file path for the users db file:
            String dbPath = new StringBuilder(jarParent + "\\" + argv[1]).toString();
            File dbFile = new File(dbPath);

            if (!iniFile.exists()){
                System.out.println("\nFAIL:  Your .ini file was not found!");
                System.out.println("\nYour command line arguments are wrong.\n\n" + helpInfo);
                System.exit(1);
            }
            if (!dbFile.exists()){
                System.out.println("\nFAIL:  Your .db file was not found!");
                System.out.println("\nYour command line arguments are wrong.\n\n" + helpInfo);
                System.exit(1);
            }
            System.out.println("\nSUCCESS:  Your command line arguments are valid! GL HF!\n\n");
        } else {
            System.out.println("\nYour command line arguments are wrong.\n\n" + helpInfo);
            System.exit(1);
        }

//        System.out.println("I would continue");
//            System.exit(0);

        File configFile = new File("ServerLink.ini");
        if (argv.length > 0) {
            configFile = new File(argv[0]);
        }

        if (!configFile.exists()) {
            if (!configFile.createNewFile()) {
                //TODO the config file gets created if one not exist... but it doesnt fill a default db name and hence
                // serverlink crashes.  FIXME
                System.out.println("Unable to create config file.");
                return;
            }
        }
//        System.exit(0);

        Wini config = new Wini(configFile);

        Integer port = config.get("ServerLink", "port", Integer.class);
        if (port == null) {
            System.out.println("ServerLink - port empty. Defaulting to " + DEFAULT_PORT);
            port = DEFAULT_PORT;
            config.put("ServerLink", "port", DEFAULT_PORT);
            config.store();
        }

        Integer threadPool = config.get("ServerLink", "thread_pool", Integer.class);
        if (threadPool == null) {
            System.out.println("ServerLink - thread_pool empty. Defaulting to " + DEFAULT_THREADS);
            threadPool = DEFAULT_THREADS;
            config.put("ServerLink", "thread_pool", DEFAULT_THREADS);
            config.store();
        }

        String databaseOptions = config.get("ServerLink", "database_options", String.class);
        if (databaseOptions == null) {
            System.out.println("ServerLink - database_options empty.");
            databaseOptions = "";
            config.put("ServerLink", "database_options", databaseOptions);
            config.store();
        }

        Boolean allowAnonymous = config.get("ServerLink", "allow_anonymous", Boolean.class);
        if (allowAnonymous == null) {
            System.out.println("ServerLink - allow_anonymous empty.");
            allowAnonymous = false;
            config.put("ServerLink", "allow_anonymous", false);
            config.store();
        }

        DatabaseRepositoryProvider.setDatabaseInstance(new SQLiteDatabaseRepositoryImpl());
        DatabaseRepositoryProvider.getDatabaseInstance().open(databaseOptions);

        System.out.println("ServerLink - Starting on " + port + ".");
        final ServerSocket listener = new ServerSocket(port);

        System.out.println("ServerLink - Starting with " + threadPool + " threads.");
        final ExecutorService executorService = Executors.newFixedThreadPool(threadPool);

        final boolean[] shuttingDown = {false};

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {

                System.out.println("ServerLink - Shutting Down");
                shuttingDown[0] = true;
                try {
                    listener.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("ServerLink - Failed Closing Listener");
                }
                DatabaseRepositoryProvider.getDatabaseInstance().close();

                executorService.shutdown();
            }
        });

        long clientId = 0;
        while (true) {
            try {
                executorService.submit(new ServerLinkHandler(listener.accept(), clientId++, allowAnonymous));
            } catch (Exception e) {
                if (!shuttingDown[0]) {
                    e.printStackTrace();
                    System.out.println("ServerLink - Failed Accept");
                    System.exit(2);
                }
            }
        }
    }

}
