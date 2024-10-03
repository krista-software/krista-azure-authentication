package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AzureAuthenticationCredentialBuilder {
    public AzureAuthenticationCredential build() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("credentials.properties")) {
            properties.load(inputStream);
            String clientId = properties.getProperty("clientId");
            String clientSecret = properties.getProperty("clientSecret");
            String tenantId = properties.getProperty("tenantId");
            String scope = properties.getProperty("scope");
            return new AzureAuthenticationCredential(clientId, clientSecret, tenantId, scope);
        } catch (IOException cause) {
            throw new RuntimeException("Failed to load public configuration file.", cause);
        }
    }
}
