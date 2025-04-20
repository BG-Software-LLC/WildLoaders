package com.bgsoftware.wildloaders.api.config;

import java.util.List;

public interface SettingsManager {

    /**
     * Global config section.
     * Config path: hologram-lines, infinite-hologram-lines
     */
    Global getGlobal();

    /**
     * All settings related to the database of the plugin.
     * Config path: database
     */
    Database getDatabase();

    interface Global {

        /**
         * Lines displayed in holograms for regular loaders.
         * Config path: hologram-lines
         */
        List<String> getHologramLines();

        /**
         * Lines displayed in holograms for infinite loaders.
         * Config path: infinite-hologram-lines
         */
        List<String> getInfiniteHologramLines();
    }

    interface Database {

        /**
         * Get the database-type to use (SQLite or MySQL).
         * Config-path: database.type
         */
        String getType();

        /**
         * The address used to connect to the database.
         * Used for MySQL only.
         * Config-path: database.address
         */
        String getAddress();

        /**
         * The port used to connect to the database.
         * Used for MySQL only.
         * Config-path: database.port
         */
        int getPort();

        /**
         * Get the name of the database.
         * Used for MySQL only.
         * Config-path: database.db-name
         */
        String getDBName();

        /**
         * The username used to connect to the database.
         * Used for MySQL only.
         * Config-path: database.user-name
         */
        String getUsername();

        /**
         * The password used to connect to the database.
         * Used for MySQL only.
         * Config-path: database.password
         */
        String getPassword();

        /**
         * The prefix used for tables in the database.
         * Used for MySQL only.
         * Config-path: database.prefix
         */
        String getPrefix();

        /**
         * Whether the database uses SSL or not.
         * Used for MySQL only.
         * Config-path: database.useSSL
         */
        boolean hasSSL();

        /**
         * Whether public-key-retrieval is allowed in the database or not.
         * Used for MySQL only.
         * Config-path: database.allowPublicKeyRetrieval
         */
        boolean hasPublicKeyRetrieval();

        /**
         * The wait-timeout of the database, in milliseconds.
         * Used for MySQL only.
         * Config-path: database.waitTimeout
         */
        long getWaitTimeout();

        /**
         * The max-lifetime of the database, in milliseconds.
         * Used for MySQL only.
         * Config-path: database.maxLifetime
         */
        long getMaxLifetime();
    }
}
