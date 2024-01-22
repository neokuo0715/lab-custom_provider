package com.example.customproviderlab.factory;

import com.example.customproviderlab.provider.CustomUserStorageProvider;
import com.example.customproviderlab.util.DbUtil;
import org.keycloak.component.ComponentModel;
import org.keycloak.component.ComponentValidationException;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.provider.ProviderConfigProperty;
import org.keycloak.provider.ProviderConfigurationBuilder;
import org.keycloak.storage.UserStorageProviderFactory;

import java.sql.Connection;
import java.util.List;

import static com.example.customproviderlab.constants.DBConnection.*;

public class CustomUserStorageProviderFactory implements UserStorageProviderFactory<CustomUserStorageProvider> {

    protected final List<ProviderConfigProperty> configMetadata;

    /**
     * 用於作provider 跟DB的溝通設定
     */
    public CustomUserStorageProviderFactory() {
        configMetadata = ProviderConfigurationBuilder.create()
                .property()
                    .name(CONFIG_KEY_JDBC_DRIVER)
                    .label("JDBC Driver Class")
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .defaultValue(POSTGRES_CONFIG_VAL_JDBC_DRIVER)
                    .helpText("Fully qualified class name of the JDBC driver")
                    .add()
                .property()
                    .name(CONFIG_KEY_JDBC_URL)
                    .label("JDBC URL")
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .defaultValue("jdbc:postgresql://"+POSTGRES_CONFIG_VAL_JDBC_IP+":"+POSTGRES_CONFIG_VAL_JDBC_PORT+"/"+POSTGRES_CONFIG_VAL_DB_DATABASE)
                    .helpText("JDBC URL used to connect to the user database")
                    .add()
                .property()
                    .name(CONFIG_KEY_DB_USERNAME)
                    .label("Database User")
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .defaultValue(POSTGRES_CONFIG_VAL_DB_USERNAME)
                    .helpText("Username used to connect to the database")
                    .add()
                .property()
                    .name(CONFIG_KEY_DB_PASSWORD)
                    .label("Database User")
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .defaultValue(POSTGRES_CONFIG_VAL_DB_PASSWORD)
                    .helpText("Password used to connect to the database")
                    .add()
                .property()
                    .name(CONFIG_KEY_VALIDATION_QUERY)
                    .label("SQL Validation Query")
                    .type(ProviderConfigProperty.STRING_TYPE)
                    .defaultValue("select 1")
                    .helpText("SQL query used to validate a connection")
                    .add()

                // ... repeat this for every property (omitted)
                .build();
    }

    @Override
    public void validateConfiguration(KeycloakSession session, RealmModel realm, ComponentModel config)
            throws ComponentValidationException {
        try (Connection c = DbUtil.getConnection(config)) {
            c.createStatement().execute(config.get(CONFIG_KEY_VALIDATION_QUERY));
        }
        catch(Exception ex) {
            throw new ComponentValidationException("Unable to validate database connection",ex);
        }
    }
    /**
     * 回傳你實作的custom provider
     * @param keycloakSession
     * @param componentModel
     * @return provider_instance
     */
    @Override
    public CustomUserStorageProvider create(KeycloakSession keycloakSession, ComponentModel componentModel) {
        return new CustomUserStorageProvider(keycloakSession,componentModel);
    }

    //為你的custom provider 命名
    @Override
    public String getId() {
        return "lab-custom-provider";
    }

    /**
     * 需要這個才能在 admin console 顯示出來
     * @return
     */
    @Override
    public List<ProviderConfigProperty> getConfigProperties() {
        return configMetadata;
    }


}
