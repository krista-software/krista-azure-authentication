package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;


import java.util.Objects;
import java.util.Set;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.DEFAULT_CALLBACK_PATH;

public class AzureAuthenticationCredential {
    private final OAuthCredential oAuthCredential;
    private final Set<String> scopes;
    private final String routingUrl;

    public AzureAuthenticationCredential(OAuthCredential oAuthCredential, Set<String> scopes, String routingUrl) {
        this.oAuthCredential = oAuthCredential;
        this.scopes = scopes;
        this.routingUrl = routingUrl;
    }

    public String getClientId() {
        return oAuthCredential.getClientId();
    }

    public String getClientSecret() {
        return oAuthCredential.getClientSecret();
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public String getAuthority(String... tenantId) {
        String baseAuthorityUrl = "https://login.microsoftonline.com/";
        if (Objects.isNull(tenantId) || tenantId.length == 0) {
            return baseAuthorityUrl + this.oAuthCredential.getTenantId();
        }
        return baseAuthorityUrl + tenantId[0];
    }

    public String getRoutingUrl() {
        return routingUrl;
    }

    public String getRedirectUrl() {
        return routingUrl.replaceAll("/extension/api/.*", DEFAULT_CALLBACK_PATH);
    }
}
