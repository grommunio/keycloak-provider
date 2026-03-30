// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2026 grommunio GmbH

package org.grommunio.keycloak.storage.user;

import java.util.List;
import java.util.Map;

import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.component.ComponentModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.adapter.AbstractUserAdapterFederatedStorage;

class GrommunioUser extends AbstractUserAdapterFederatedStorage {
    private final String username;
    private final int id;
    private final String email;
    private final String firstName;
    private final String lastName;
    private final String chatPerm;
    private final String meetPerm;
    private final String filesPerm;
    private final String webPerm;
    private static final GrommunioLogger logger = (GrommunioLogger) GrommunioLogger.getLogger(GrommunioUser.class);

    GrommunioUser(KeycloakSession session,
                  RealmModel realm,
                  ComponentModel storageProviderModel,
                  String username,
                  int id,
                  String email,
                  String firstName,
                  String lastName,
                  String chatPerm,
                  String meetPerm,
                  String filesPerm,
                  String webPerm) {
        super(session, realm, storageProviderModel);
        this.username = username;
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.chatPerm = chatPerm;
        this.meetPerm = meetPerm;
        this.filesPerm = filesPerm;
        this.webPerm = webPerm;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getId() {
        return new StorageId(storageProviderModel.getId(), String.valueOf(this.id)).getId();
    }

    // TODO: necessary?
    @Override
    public void setUsername(String username) {
        logger.debugf("setUsername(%s)", username);
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

    public String getChatPerm() {
        return this.chatPerm;
    }

    public String getMeetPerm() {
        return this.meetPerm;
    }

    public String getFilesPerm() {
        return this.filesPerm;
    }

    public String getWebPerm() {
        return this.webPerm;
    }

    @Override
    public Map<String, List<String>> getAttributes() {
        MultivaluedHashMap<String, String> attributes = new MultivaluedHashMap<>();
        attributes.add(UserModel.USERNAME, getUsername());
        attributes.add(UserModel.EMAIL, getEmail());
        attributes.add(UserModel.EMAIL_VERIFIED, Boolean.toString(true));
        attributes.add(UserModel.FIRST_NAME, getFirstName());
        attributes.add(UserModel.LAST_NAME, getLastName());
        attributes.add("chatPerm", getChatPerm());
        attributes.add("meetPerm", getMeetPerm());
        attributes.add("filesPerm", getFilesPerm());
        attributes.add("webPerm", getWebPerm());
        return attributes;
    }


    // No credentialManager() override! We rely on AbstractUserAdapterFederatedStorage's default behavior.

    static class Builder {
        private static final int CHAT_PERM_BITS = 1 << 4;
        private static final int MEET_PERM_BITS = 1 << 5;
        private static final int FILES_PERM_BITS = 1 << 6;
        private static final int WEB_PERM_BITS = 1 << 9;
        private final KeycloakSession session;
        private final RealmModel realm;
        private final ComponentModel storageProviderModel;
        private final String username;
        private final int id;
        private String email;
        private String firstName;
        private String lastName;
        private int privilegeBits;

        Builder(KeycloakSession session, RealmModel realm, ComponentModel storageProviderModel, String username, int id) {
            this.session = session;
            this.realm = realm;
            this.storageProviderModel = storageProviderModel;
            this.username = username;
            this.id = id;
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

        GrommunioUser.Builder privilegeBits(int privilegeBits) {
            this.privilegeBits = privilegeBits;
            return this;
        }

        GrommunioUser build() {
            String chatPerm = String.valueOf((privilegeBits & CHAT_PERM_BITS) > 0);
            String meetPerm = String.valueOf((privilegeBits & MEET_PERM_BITS) > 0);
            String filesPerm = String.valueOf((privilegeBits & FILES_PERM_BITS) > 0);
            String webPerm = String.valueOf((privilegeBits & WEB_PERM_BITS) > 0);
            return new GrommunioUser(
                    session,
                    realm,
                    storageProviderModel,
                    username,
                    id,
                    email,
                    firstName,
                    lastName,
                    chatPerm,
                    meetPerm,
                    filesPerm,
                    webPerm
            );
        }
    }
}
