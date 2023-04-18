// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2023 grommunio GmbH

package org.grommunio.keycloak.storage.user;

import org.jboss.logging.Logger;
import org.keycloak.Config;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.KeycloakSessionFactory;
import org.keycloak.storage.UserStorageProviderFactory;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class GrommunioUserStorageProviderFactory implements UserStorageProviderFactory<GrommunioUserStorageProvider> {

    public static final String PROVIDER_NAME = "grommunio";
    private static final GrommunioLogger logger = (GrommunioLogger) GrommunioLogger.getLogger(GrommunioUserStorageProviderFactory.class);

    @Override
    public String getId() {
        return PROVIDER_NAME;
    }

    @Override
    public GrommunioUserStorageProvider create(KeycloakSession session, ComponentModel model) {
        return new GrommunioUserStorageProvider(session, model, this);
    }

    @Override
    public void init(Config.Scope config) {
    }

    @Override
    public void postInit(KeycloakSessionFactory factory) {
    }

    @Override
    public String getHelpText() {
        return "Storage Provider for grommunio user database";
    }

    @Override
    public void close() {
    }

    protected GrommunioPAMAuthenticator createGrommunioPAMAuthenticator(String username, String... factors) {
        return new GrommunioPAMAuthenticator(username, factors);
    }
}
