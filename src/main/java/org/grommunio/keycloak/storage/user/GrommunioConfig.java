// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2023 grommunio GmbH

package org.grommunio.keycloak.storage.user;

import org.jboss.logging.Logger;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class GrommunioConfig {

    private static final Logger logger = Logger.getLogger(GrommunioConfig.class);

    public static Properties getConfig() throws IOException {

        String grommunioConfigPath = "/etc/grommunio-keycloak/grommunio.properties";
        InputStream propsStream;
        Properties props = new Properties();
        propsStream = new FileInputStream(grommunioConfigPath);

	try {
            if (propsStream != null) {
                props.load(propsStream);
            } else {
                throw new FileNotFoundException("Could not load properties from '" + grommunioConfigPath + "'.");
            }
	} catch (Exception e) {
            logger.error("Could not load grommunio.properties", e);
            e.printStackTrace();
        } finally {
            propsStream.close();
        }
	return props;
    }
}
