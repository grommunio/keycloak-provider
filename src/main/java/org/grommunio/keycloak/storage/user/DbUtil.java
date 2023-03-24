// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2023 grommunio GmbH

package org.grommunio.keycloak.storage.user;

import org.jboss.logging.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

import org.keycloak.component.ComponentModel;

public class DbUtil {

    private static final GrommunioLogger logger = (GrommunioLogger) GrommunioLogger.getLogger(GrommunioConfig.class);
    public static Connection getConnection(ComponentModel config) throws SQLException{
        Properties conf;
        try {
            conf = GrommunioConfig.getConfig();
	} catch(Exception e) {
            throw new RuntimeException("Could not load grommunio.properties file.");
        }
        String driverClass = conf.getProperty("grommunio.db-kind", "org.mariadb.jdbc.Driver");
        try {
            Class.forName(driverClass);
        }
        catch(ClassNotFoundException nfe) {
            throw new RuntimeException("Invalid JDBC driver: " + driverClass + ". Please check if your driver if properly installed");
        }
        
        return DriverManager.getConnection(conf.getProperty("grommunio.jdbc-url", "jdbc:mariadb://localhost:3306/grommunio"),
          conf.getProperty("grommunio.username", "groauth"),
          conf.getProperty("grommunio.password"));
    }
}

