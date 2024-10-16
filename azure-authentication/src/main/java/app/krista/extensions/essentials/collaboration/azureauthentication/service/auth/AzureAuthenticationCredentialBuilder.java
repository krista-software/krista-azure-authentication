package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.azureauthentication.exception.FileOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.BLANK_SPACE;
import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.DEFAULT_CALLBACK_PATH;

public class AzureAuthenticationCredentialBuilder {
    private String clientId;
    private String clientSecret;
    private String tenantId;
    private String routingUrl;
    private final Set<String> scopes = new HashSet<>();

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

    public AzureAuthenticationCredentialBuilder redirectUrl(RoutingInfo routingInfo) {
        String originalRoutingUrl = routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        this.routingUrl = originalRoutingUrl.replaceAll("/extension/api/.*", DEFAULT_CALLBACK_PATH);
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
            return new AzureAuthenticationCredential(cId, cSecret, tId, scps, routingUrl);
        } catch (IOException cause) {
            throw new FileOperationException("Failed to load public configuration file.", cause);
        }
    }

    public AzureAuthenticationCredential build() {
        scopes.add("User.Read");
        return new AzureAuthenticationCredential(clientId, clientSecret, tenantId, scopes, routingUrl);
    }
}
