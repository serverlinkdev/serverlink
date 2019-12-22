package org.uigl.ut2004.serverlink.database;

public class DatabaseRepositoryProvider {

    private static DatabaseRepository instance;

    private DatabaseRepositoryProvider() {}

    public static void setDatabaseInstance(DatabaseRepository databaseRepository) {
        instance = databaseRepository;
    }

    public static DatabaseRepository getDatabaseInstance() {
        return instance;
    }

}
