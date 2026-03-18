package org.ayosynk.landClaimPlugin.db;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Abstraction over database backends (SQLite, MySQL).
 * Implementations handle connection lifecycle and dialect-specific table creation.
 */
public interface Database {

    /**
     * Establish the database connection and initialize the connection pool.
     *
     * @throws SQLException if the connection fails
     */
    void connect() throws SQLException;

    /**
     * Close all connections and release resources.
     */
    void disconnect();

    /**
     * Obtain a connection from the pool.
     *
     * @return an active JDBC connection
     * @throws SQLException if no connection is available
     */
    Connection getConnection() throws SQLException;

    /**
     * Create core database tables shared across all DAOs.
     */
    void createTables();
}
