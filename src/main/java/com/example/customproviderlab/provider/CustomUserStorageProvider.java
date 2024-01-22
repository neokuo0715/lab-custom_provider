package com.example.customproviderlab.provider;

import com.example.customproviderlab.entity.CustomUser;
import com.example.customproviderlab.util.DbUtil;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.GroupModel;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.StorageId;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.keycloak.storage.user.UserQueryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import static com.example.customproviderlab.constants.DBFieldsConstants.*;

// UserStorageProvider needs for factory
// UserLookupProvider needs for keycloak auto import user to its database when required
// CredentialInputValidator for validating a credential
// UserQueryProvider for showing the user list on admin console
public class CustomUserStorageProvider implements UserStorageProvider,
        UserLookupProvider,
        CredentialInputValidator,
        UserQueryProvider
    {
        private static final Logger log = LoggerFactory.getLogger(CustomUserStorageProvider.class);
        private KeycloakSession keycloakSession;
        private ComponentModel componentModel;
        public CustomUserStorageProvider(KeycloakSession keycloakSession, ComponentModel componentModel) {
            this.keycloakSession=keycloakSession;
            this.componentModel=componentModel;
        }

        @Override
        public void close() {

        }

        @Override
        public UserModel getUserById(RealmModel realmModel, String s) {
            return null;
        }

        /**
         *
         * @param realmModel
         * @param username
         * @return
         */
        @Override
        public UserModel getUserByUsername(RealmModel realmModel, String username) {
            String querySql = String.format("select " +
                    "  %s, %s, %s, %s, %s " +
                    "from %s " +
                    "where %s = ?",DB_USER_NAME,DB_USER_FIRST_NAME,DB_USER_LAST_NAME,DB_USER_EMAIL,DB_USER_BIRTHDATE,
                    DB_USER_TABLE,
                    DB_USER_NAME
                    );
            try ( Connection c = DbUtil.getConnection(this.componentModel)) {
                PreparedStatement st = c.prepareStatement(querySql);
                st.setString(1, username);
                st.execute();
                ResultSet rs = st.getResultSet();
                if ( rs.next()) {
                    return mapUser(realmModel,rs);
                }
                else {
                    return null;
                }
            }
            catch(SQLException ex) {
                throw new RuntimeException("Database error:" + ex.getMessage(),ex);
            }
        }

        /**
         * 把Data 轉換成 keyCloak看得懂的userInfo(UserModel)
         * @param realmModel
         * @param rs
         * @return
         * @throws SQLException
         */
        private UserModel mapUser(RealmModel realmModel, ResultSet rs) throws SQLException{
            CustomUser user = new CustomUser.Builder(keycloakSession, realmModel, componentModel,
                    rs.getString("username"))
                    .email(rs.getString("email"))
                    .firstName(rs.getString("firstName"))
                    .lastName(rs.getString("lastName"))
                    .birthDate(rs.getDate("birthDate"))
                    .build();
            log.info("user model : user name ={} ",user.getUsername());
            return user;
        }


        @Override
        public boolean supportsCredentialType(String credentialType) {
            return PasswordCredentialModel.TYPE.endsWith(credentialType);
        }

        /**
         * 當前realm 是否有支援
         * @param realm
         * @param user
         * @param credentialType
         * @return
         */
        @Override
        public boolean isConfiguredFor(RealmModel realm, UserModel user, String credentialType) {
            return supportsCredentialType(credentialType);
        }


        @Override
        public UserModel getUserByEmail(RealmModel realmModel, String s) {
            return null;
        }


        /**
         * 使用者登入的驗證
         * @param realm
         * @param user
         * @param credentialInput
         * @return
         */
        @Override
        public boolean isValid(RealmModel realm, UserModel user, CredentialInput credentialInput) {
            log.info("[I57] isValid(realm={},user={},credentialInput.type={})",realm.getName(), user.getUsername(), credentialInput.getType());
            if( !this.supportsCredentialType(credentialInput.getType())) {
                return false;
            }
            StorageId sid = new StorageId(user.getId());
            String username = sid.getExternalId();

            try ( Connection c = DbUtil.getConnection(this.componentModel)) {
                String sql = String.format("select %s from users where %s = ?",DB_USER_PWD,DB_USER_NAME);
                PreparedStatement st = c.prepareStatement(sql);
                st.setString(1, username);
                st.execute();
                ResultSet rs = st.getResultSet();
                if ( rs.next()) {
                    String pwd = rs.getString(1);
                    return pwd.equals(credentialInput.getChallengeResponse());
                }
                else {
                    return false;
                }
            }
            catch(SQLException ex) {
                throw new RuntimeException("Database error:" + ex.getMessage(),ex);
            }
        }

        @Override
        public Stream<UserModel> searchForUserStream(RealmModel realm, String search, Integer firstResult, Integer maxResults) {
            log.info("[I139] searchForUser: realm={}", realm.getName());

            try (Connection c = DbUtil.getConnection(this.componentModel)) {
                String sql = String.format("select %s, %s from %s where %s like ? order by %s limit ? offset ?",
                                            DB_USER_NAME,DB_USER_EMAIL,
                                            DB_USER_TABLE,
                                            DB_USER_NAME,DB_USER_NAME
                                            );
                PreparedStatement st = c.prepareStatement(sql);
                st.setString(1, search);
                st.setInt(2, maxResults);
                st.setInt(3, firstResult);
                st.execute();
                ResultSet rs = st.getResultSet();
                List<UserModel> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(mapUser(realm, rs));
                }
                return users.stream();
            } catch (SQLException ex) {
                throw new RuntimeException("Database error:" + ex.getMessage(), ex);
            }
        }

        @Override
        public Stream<UserModel> searchForUserStream(RealmModel realmModel, Map<String, String> map, Integer integer, Integer integer1) {
            log.info("[I140] searchForUser: realm={}", realmModel.getName());
            return null;
        }

        @Override
        public Stream<UserModel> getGroupMembersStream(RealmModel realmModel, GroupModel groupModel, Integer integer, Integer integer1) {
            return null;
        }

        @Override
        public Stream<UserModel> searchForUserByUserAttributeStream(RealmModel realmModel, String s, String s1) {
            return null;
        }
    }
