// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2023 grommunio GmbH

package org.grommunio.keycloak.storage.user;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.LegacyUserCredentialManager;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;


class GrommunioUser extends AbstractUserAdapterFederatedStorage {
    
    private final String username;
    private final String email;
    private final String firstName;
    private final String lastName;
    private static final GrommunioLogger logger = (GrommunioLogger) GrommunioLogger.getLogger(GrommunioUser.class);

    private GrommunioUser(KeycloakSession session, RealmModel realm,
      ComponentModel storageProviderModel,
      String username,
      String email,
      String firstName,
      String lastName ) {
        super(session, realm, storageProviderModel);
        this.username = username;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public void setUsername(String username) {
        logger.infof("setUsername(%s)", username);
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public String getLastName() {
        return lastName;
    }

    @Override
    public String getEmail() {
        return email;
    }

    @Override
    public boolean isEmailVerified() {
        return true;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();
        attributes.add(UserModel.USERNAME, getUsername());
        attributes.add(UserModel.EMAIL, getEmail());
        attributes.add(UserModel.EMAIL_VERIFIED, Boolean.toString(true));
        attributes.add(UserModel.FIRST_NAME, getFirstName());
        attributes.add(UserModel.LAST_NAME, getLastName());
        return attributes;
    }

    @Override
    public SubjectCredentialManager credentialManager() {
        return new LegacyUserCredentialManager(session, realm, this);
    }

    static class Builder {
        private final KeycloakSession session;
        private final RealmModel realm;
        private final ComponentModel storageProviderModel;
        private String username;
        private String email;
        private String firstName;
        private String lastName;
        
        Builder(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel,String username) {
            this.session = session;
            this.realm = realm;
            this.storageProviderModel = storageProviderModel;
            this.username = username;
        }
        
        GrommunioUser.Builder email(String email) {
            this.email = email;
            return this;
        }
        
        GrommunioUser.Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }
        
        GrommunioUser.Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }
        
        GrommunioUser build() {
            return new GrommunioUser(
              session,
              realm,
              storageProviderModel,
              username,
              email,
              firstName,
              lastName );
            
        }
    }
}
