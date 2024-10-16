package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.DEFAULT_CALLBACK_PATH;

public class AzureAuthenticationCredentialBuilder {
    private String routingUrl;

    public AzureAuthenticationCredentialBuilder redirectUrl(RoutingInfo routingInfo) {
        String originalRoutingUrl = routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.routingUrl = originalRoutingUrl.replaceAll("/extension/api/.*", DEFAULT_CALLBACK_PATH);
        return this;
    }

    public AzureAuthenticationCredential build() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("credentials.properties")) {
            properties.load(inputStream);
            String clientId = properties.getProperty("clientId");
            String clientSecret = properties.getProperty("clientSecret");
            String tenantId = properties.getProperty("tenantId");
            String scope = properties.getProperty("scope");
            return new AzureAuthenticationCredential(clientId, clientSecret, tenantId, scope, routingUrl);
        } catch (IOException cause) {
            throw new RuntimeException("Failed to load public configuration file.", cause);
        }
    }
}
