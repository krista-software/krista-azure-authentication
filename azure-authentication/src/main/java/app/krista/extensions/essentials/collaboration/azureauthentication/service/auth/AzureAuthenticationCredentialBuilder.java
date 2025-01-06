package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

import app.krista.extensions.essentials.collaboration.azureauthentication.exception.FileOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.*;

public class AzureAuthenticationCredentialBuilder {
    private String defaultRole;
    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String routingUrl;
    private final Set<String> scopes = new HashSet<>();

    public AzureAuthenticationCredentialBuilder defaultRole(String clientId) {
        this.defaultRole = defaultRole;
        return this;
    }

    public AzureAuthenticationCredentialBuilder clientId(String clientId) {
        this.clientId = clientId;
        return this;
    }

    public AzureAuthenticationCredentialBuilder clientSecret(String clientSecret) {
        this.clientSecret = clientSecret;
        return this;
    }

    public AzureAuthenticationCredentialBuilder tenantId(String tenantId) {
        this.tenantId = tenantId;
        return this;
    }

    public AzureAuthenticationCredentialBuilder routingUrl(String routingUrl) {
        this.routingUrl = routingUrl;
        return this;
    }

    public AzureAuthenticationCredential buildCredentialFromProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("credentials.properties")) {
            properties.load(inputStream);
            String cId = properties.getProperty("clientId");
            String cSecret = properties.getProperty("clientSecret");
            String tId = properties.getProperty("tenantId");
            String scope = properties.getProperty("scope");
            Set<String> scps = Arrays.stream(scope.split(BLANK_SPACE)).collect(Collectors.toSet());
            return new AzureAuthenticationCredential(new OAuthCredentialImpl(cId, cSecret, tId), scps, routingUrl);
        } catch (IOException cause) {
            throw new FileOperationException("Failed to load public configuration file.", cause);
        }
    }

    public AzureAuthenticationCredential build() {
        scopes.add("User.Read");
        return new AzureAuthenticationCredential(new OAuthCredentialImpl(clientId, clientSecret, tenantId), scopes, routingUrl);
    }

    public AzureAuthenticationCredential build(Map<String, String> attributes) {
        String clientIdAttribute = attributes.get(CLIENT_ID);
        String clientSecretAttribute = attributes.get(CLIENT_SECRET);
        String tenantIdAttribute = attributes.get(TENANT_ID);
        scopes.add("User.Read");
        return new AzureAuthenticationCredential(new OAuthCredentialImpl(clientIdAttribute, clientSecretAttribute, tenantIdAttribute), scopes, routingUrl);
    }
}
