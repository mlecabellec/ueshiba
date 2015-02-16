package com.booleanworks.ueshiba;

/**
 * Created by vortigern on 17/02/15.
 */
public class DatabaseManager {
    private static DatabaseManager ourInstance = new DatabaseManager();

    public static DatabaseManager getInstance() {
        return ourInstance;
    }

    private DatabaseManager() {
    }
}
