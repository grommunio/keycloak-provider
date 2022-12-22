/*
 * Copyright 2016 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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

    public static final String PROVIDER_NAME = "grommunio-user-storage";
    private static final Logger logger = Logger.getLogger(GrommunioUserStorageProviderFactory.class);

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
        return "grommunio User Storage Provider";
    }

    @Override
    public void close() {
        logger.info("<<<<<< Closing factory");
    }

    protected GrommunioPAMAuthenticator createGrommunioPAMAuthenticator(String username, String... factors) {
        return new GrommunioPAMAuthenticator(username, factors);
    }
}
