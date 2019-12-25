package org.uigl.ut2004.serverlink;

import org.uigl.ut2004.serverlink.database.SQLiteDatabaseRepositoryImpl;
import org.uigl.ut2004.serverlink.exception.AuthenticationException;

import java.util.Scanner;

public class Wizard {
    public void questionnaire() throws AuthenticationException {
        Scanner scanner = new Scanner(System.in);
        System.out.println("Welcome to the serverlink admin account wizard.");
        System.out.println("Please enter the database admin's username:");
        String username = scanner.nextLine();
        System.out.println("Please enter the database admin's password:");
        String password = scanner.nextLine();
        System.out.println("You entered username of: " + username + " and password of: " + password);

        SQLiteDatabaseRepositoryImpl dbWizard = new SQLiteDatabaseRepositoryImpl();
        dbWizard.genAdminCredentials(username, password);
        System.out.println("SUCCESS:  Now put that into your db file, you're now ready to start serverlink.");

        scanner.close();
    }
}
