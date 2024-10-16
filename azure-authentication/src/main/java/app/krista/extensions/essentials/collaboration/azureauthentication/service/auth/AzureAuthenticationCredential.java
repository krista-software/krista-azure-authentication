package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

import java.util.Set;

public class AzureAuthenticationCredential {
    private final String clientId;
    private final String clientSecret;
    private final String tenantId;
    private final Set<String> scopes;
    private final String redirectUrl;

    public AzureAuthenticationCredential(String clientId, String clientSecret, String tenantId, Set<String> scopes, String redirectUrl) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tenantId = tenantId;
        this.scopes = scopes;
        this.redirectUrl = redirectUrl;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public String getAuthority() {
        return "https://login.microsoftonline.com/" + tenantId;
    }

    public String getRedirectUrl() {
        return redirectUrl;
    }
}
