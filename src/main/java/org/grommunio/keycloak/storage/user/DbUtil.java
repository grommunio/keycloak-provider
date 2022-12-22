package org.grommunio.keycloak.storage.user;

import org.jboss.logging.Logger;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import static org.keycloak.quarkus.runtime.configuration.Configuration.getConfig;

import org.keycloak.component.ComponentModel;

public class DbUtil {

    public static Connection getConnection(ComponentModel config) throws SQLException{
        String driverClass = getConfig().getRawValue("quarkus.grommunio.user-db.db-kind");
        try {
            Class.forName(driverClass);
        }
        catch(ClassNotFoundException nfe) {
            throw new RuntimeException("Invalid JDBC driver: " + driverClass + ". Please check if your driver if properly installed");
        }
        
        return DriverManager.getConnection(getConfig().getRawValue("quarkus.grommunio.user-db.jdbc-url"),
          getConfig().getRawValue("quarkus.grommunio.user-db.username"),
          getConfig().getRawValue("quarkus.grommunio.user-db.password"));
    }
}

