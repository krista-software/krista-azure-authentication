package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

public interface OAuthCredential {
    String getClientId();

    String getClientSecret();

    String getTenantId();
}
