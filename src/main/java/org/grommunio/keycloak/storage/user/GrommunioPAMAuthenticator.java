// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2023 grommunio GmbH

package org.grommunio.keycloak.storage.user;

import org.jboss.logging.Logger;
import org.grommunio.libpam.PAM;
import org.grommunio.libpam.PAMException;

/**
 * PAMAuthenticator for Unix users
 *
 * @author <a href="mailto:bruno@abstractj.org">Bruno Oliveira</a>
 * @version $Revision: 1 $
 */
public class GrommunioPAMAuthenticator {

    private static final String PAM_SERVICE = "grommunioauth";
    private static final Logger logger = Logger.getLogger(GrommunioPAMAuthenticator.class);
    private final String username;
    private final String[] factors;

    public GrommunioPAMAuthenticator(String username, String... factors) {
        this.username = username;
        this.factors = factors;
    }

    /**
     * Returns true if user was successfully authenticated against PAM
     *
     * @return boolean if user was successfully authenticated
     */
    public boolean authenticate() {
        logger.info("authenticate()");
        PAM pam = null;
        boolean authenticated = false;

        try {
            pam = new PAM(PAM_SERVICE);
            authenticated = pam.authenticate(username, factors);
        } catch (PAMException e) {
            logger.error("PAM Authentication failed", e);
            e.printStackTrace();
        } finally {
            pam.dispose();
        }

        return authenticated;
    }
}
