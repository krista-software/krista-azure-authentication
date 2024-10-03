package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

public class AzureAuthenticationCredential {
    private final String clientId;
    private final String clientSecret;
    private final String tenantId;
    private final String scope;

    public AzureAuthenticationCredential(String clientId, String clientSecret, String tenantId, String scope) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tenantId = tenantId;
        this.scope = scope;
    }

    public String getClientId() {
        return clientId;
    }

    public String getClientSecret() {
        return clientSecret;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getScope() {
        return scope;
    }
}
