package app.krista.extensions.essentials.collaboration.azureauthentication;

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class AzureAuthenticationExtensionRequestAuthenticator implements RequestAuthenticator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureAuthenticationExtensionRequestAuthenticator.class);
    private final AuthorizationContext authorizationContext;

    public AzureAuthenticationExtensionRequestAuthenticator(AuthorizationContext authorizationContext) {
        this.authorizationContext = authorizationContext;
    }

    @Override
    public String getScheme() {
        return "";
    }

    @Override
    public Set<String> getSupportedProtocols() {
        return Set.of();
    }

    @Override
    public String getAuthenticatedAccountId(ProtoRequest protoRequest) {
        HttpRequest httpRequest = (HttpRequest) protoRequest;
        try {
            httpRequest.bufferBody();
            if (Objects.equals("/login", httpRequest.getUri().getPath())) {
                LOGGER.info("Authorization Context: {}", authorizationContext.getAuthorizedAccount().getAccountId());
                return authorizationContext.getAuthorizedAccount().getAccountId();
            }
        } catch (Exception cause) {
            throw new IllegalStateException("Failed to authenticate : Reason: " + cause.getMessage(), cause);
        }
        return null;
    }

    @Override
    public boolean setServiceAuthorization(String s) {
        return false;
    }

    @Override
    public Map<String, NamedField> getAttributeFields() {
        return Map.of();
    }

    @Override
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException e, ProtoRequest protoRequest) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException e) {
        return null;
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException e, ProtoRequest protoRequest) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException e) {
        return null;
    }
}
