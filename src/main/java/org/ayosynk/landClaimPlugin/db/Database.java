package org.ayosynk.landClaimPlugin.db;

import java.sql.Connection;
import java.sql.SQLException;

public interface Database {
    void connect() throws SQLException;

    void disconnect();

    Connection getConnection() throws SQLException;

    void createTables();
}
