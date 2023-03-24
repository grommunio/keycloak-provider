// SPDX-License-Identifier: AGPL-3.0-or-later
// SPDX-FileCopyrightText: 2023 grommunio GmbH

package org.grommunio.keycloak.storage.user;

//import org.jboss.logging.Logger;
import org.grommunio.keycloak.storage.user.GrommunioLogger;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.credential.CredentialModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.cache.CachedUserModel;
import org.keycloak.models.cache.OnUserCache;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Stream;
import java.util.Set;
import java.util.UUID;

/**
 * @author <a href="mailto:bill@burkecentral.com">Bill Burke</a>
 * @version $Revision: 1 $
 */
public class GrommunioUserStorageProvider implements
        UserStorageProvider,
        UserLookupProvider,
        CredentialInputValidator,
	UserQueryProvider
{
    private static final GrommunioLogger logger = (GrommunioLogger) GrommunioLogger.getLogger(GrommunioUserStorageProvider.class);

    protected KeycloakSession session;
    protected ComponentModel model;

    protected static final Set<String> supportedCredentialTypes = new HashSet<>();
    private final GrommunioUserStorageProviderFactory factory;

    public GrommunioUserStorageProvider(KeycloakSession session, ComponentModel model, GrommunioUserStorageProviderFactory grommunioUserStorageProviderFactory) {
        this.session = session;
        this.model = model;
        this.factory = grommunioUserStorageProviderFactory;
    }

    static {
        supportedCredentialTypes.add(PasswordCredentialModel.TYPE);
    }

    @Override
    public void close() {
        logger.info("close()");
    }

    @Override
    public UserModel getUserById(RealmModel realm, String id) {
        logger.infof("getUserById(%s)", id);
        StorageId sid = new StorageId(id);
        return getUserByUsername(realm,sid.getExternalId());
    }

    @Override
    public UserModel getUserByUsername(RealmModel realm, String username) {
        logger.infof("getUserByUsername(%s)", username);
        try ( Connection c = DbUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement(
                "select " + 
                "u.username, p1.propval_str as firstName, p2.propval_str as lastName, u.primary_email as email " +
                "from users as u " +
                "join user_properties as p1 on u.id = p1.user_id and p1.proptag=973471775 " +
	       	"join user_properties as p2 on u.id = p2.user_id and p2.proptag=974192671 " +
	        "where u.username = ?");
            st.setString(1, username);
            st.execute();
            ResultSet rs = st.getResultSet();
            if ( rs.next()) {
                return mapUser(realm,rs);
            }
            else {
                return null;
            }
        }
        catch(SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(),ex);
        }
    }

    @Override
    public UserModel getUserByEmail(RealmModel realm, String email) {
        logger.infof("getUserByEmail(%s)", email);
        try ( Connection c = DbUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement(
                "select " + 
                "u.username, p1.propval_str as firstName, p2.propval_str as lastName, u.primary_email as email " +
                "from users as u " +
                "join user_properties as p1 on u.id = p1.user_id and p1.proptag=973471775 " +
	       	"join user_properties as p2 on u.id = p2.user_id and p2.proptag=974192671 " +
	        "where u.primary_email = ?");
            st.setString(1, email);
            st.execute();
            ResultSet rs = st.getResultSet();
            if ( rs.next()) {
                return mapUser(realm,rs);
            }
            else {
                return null;
            }
        }
        catch(SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(),ex);
        }
    }

    @Override
    public boolean supportsCredentialType(String credentialType) {
        logger.infof("supportsCredentialType(%s)", credentialType);
        return PasswordCredentialModel.TYPE.equals(credentialType);
    }

    @Override
    public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
        logger.infof("isConfiguredFor(realm=%s,user=%s,credentialType=%s)",realm.getName(), user.getUsername(), credentialType);
	return supportsCredentialType(credentialType);
    }

    @Override
    public boolean isValid(RealmModel realm, UserModel user, CredentialInput input) {
        logger.infof("isValid(realm=%s,user=%s,credentialInput.type=%s)",realm.getName(), user.getUsername(), input.getType());
        if (!supportsCredentialType(input.getType()) || !(input instanceof UserCredentialModel)) return false;

        logger.infof("isValid() Credentialtype %s is supported.", input.getType());
	StorageId sid = new StorageId(user.getId());
	String username = sid.getExternalId();
	logger.infof("isValid() Username: %s.", username);

        UserCredentialModel cred = (UserCredentialModel)input;
        GrommunioPAMAuthenticator pam = factory.createGrommunioPAMAuthenticator(username, cred.getChallengeResponse());
        return pam.authenticate();
    }

    @Override
    public int getUsersCount(RealmModel realm) {
        logger.infof("getUsersCount: realm=%s", realm.getName() );
        try ( Connection c = DbUtil.getConnection(this.model)) {
            Statement st = c.createStatement();
            st.execute("select count(*) from users");
            ResultSet rs = st.getResultSet();
            rs.next();
            return rs.getInt(1);
        }
        catch(SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(),ex);
        }
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
        Map<String, String> attributes = new HashMap<>(1);
        attributes.put(UserModel.SEARCH, search);
	return searchForUserStream(realm, attributes, firstResult, maxResults);
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, String search) {
        return searchForUserStream(realm,search,0,5000);
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String,String> params, Integer firstResult, Integer maxResults) {
        String search = params.get(UserModel.SEARCH);
        logger.infof("searchForUserStream: realm=%s, search=%s, firstResult=%d, maxResults=%d", realm.getName(), search, firstResult, maxResults);

        try ( Connection c = DbUtil.getConnection(this.model)) {
            PreparedStatement st = c.prepareStatement(
                "select " +
                "u.username, p1.propval_str as firstName, p2.propval_str as lastName, u.primary_email as email " +
                "from users as u " +
                "join user_properties as p1 on u.id = p1.user_id and p1.proptag=973471775 " +
                "join user_properties as p2 on u.id = p2.user_id and p2.proptag=974192671 " +
                "where u.username like ? order by u.username limit ? offset ?");
            st.setString(1, "%" + search + "%");
            st.setInt(2, maxResults);
            st.setInt(3, firstResult);
            st.execute();
            ResultSet rs = st.getResultSet();
            List<UserModel> users = new ArrayList<>();
            while(rs.next()) {
                users.add(mapUser(realm,rs));
            }
            return users.stream();
        }
        catch(SQLException ex) {
            throw new RuntimeException("Database error:" + ex.getMessage(),ex);
        }
    }

    @Override
    public Stream<UserModel> searchForUserStream(RealmModel realm, Map<String,String> params) {
        return searchForUserStream(realm,params,0,5000);
    }


    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> getGroupMembersStream(RealmModel realm, GroupModel group, Integer firstResult, Integer maxResults) {
        return Stream.empty();
    }

    @Override
    public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realm, String attrName, String attrValue) {
        return Stream.empty();
    }

    private UserModel mapUser(RealmModel realm, ResultSet rs) throws SQLException {

        GrommunioUser user = new GrommunioUser.Builder(session, realm, model, rs.getString("username"))
          .email(rs.getString("email"))
          .firstName(rs.getString("firstName"))
          .lastName(rs.getString("lastName"))
          .build();

        return user;
    }

}
