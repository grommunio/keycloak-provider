// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2023 grommunio GmbH

package org.grommunio.keycloak.storage.user;

import org.grommunio.libpam.PAM;

/**
 * PAMAuthenticator for Unix users
 *
 * @author <a href="mailto:bruno@abstractj.org">Bruno Oliveira</a>
 * @version $Revision: 1 $
 */
public class GrommunioPAMAuthenticator {

    private static final String PAM_SERVICE = "grommunioauth";
    private static final GrommunioLogger logger = (GrommunioLogger) GrommunioLogger.getLogger(GrommunioPAMAuthenticator.class);
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

        try {
            pam = new PAM(PAM_SERVICE);
            return pam.authenticate(username, factors);
        } catch (Throwable t) {
            logger.warn("PAM init/auth failed: ", t.toString(), t);
            return false;
        } finally {
            if (pam != null) {
                try {
                    pam.dispose();
                } catch (Throwable ignore) {
                }
            }
        }
    }
}
