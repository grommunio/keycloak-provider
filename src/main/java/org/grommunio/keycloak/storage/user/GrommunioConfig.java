// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2026 grommunio GmbH

package org.grommunio.keycloak.storage.user;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;


public class GrommunioConfig {
    private static final GrommunioLogger logger = (GrommunioLogger) GrommunioLogger.getLogger(GrommunioConfig.class);

    public static Properties getConfig() {
        String grommunioConfigPath = "/etc/grommunio-keycloak/grommunio.properties";
        InputStream propsStream;
        Properties props = new Properties();

        try {
            propsStream = Files.newInputStream(Paths.get(grommunioConfigPath));
            props.load(propsStream);
            propsStream.close();
        } catch (Exception e) {
            logger.error("Could not load grommunio.properties file", e);
        }

        return props;
    }
}
