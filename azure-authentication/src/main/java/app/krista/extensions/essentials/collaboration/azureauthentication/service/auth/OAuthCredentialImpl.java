package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

public class OAuthCredentialImpl implements OAuthCredential {
    private final String clientId;
    private final String clientSecret;
    private final String tenantId;

    public OAuthCredentialImpl(String clientId, String clientSecret, String tenantId) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.tenantId = tenantId;
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
}
