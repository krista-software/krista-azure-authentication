package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.azureauthentication.store.OauthSessionStore;
import app.krista.model.field.NamedValuedField;
import app.krista.model.field.util.GsonJsonMapper;
import app.krista.omni.sdk.spi.util.GsonJsonMapperImpl;
import com.google.gson.JsonObject;
import com.microsoft.aad.msal4j.*;
import com.microsoft.graph.http.GraphServiceException;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;


import javax.inject.Inject;

import java.lang.reflect.MalformedParametersException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.*;
import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.TENANT_ID;

@Service
public class AzureOAuthService {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AzureOAuthService.class);
    private final Invoker invoker;
    private Map<String, String> attributes;
    private final GsonJsonMapper gsonJsonMapper;
    private final OauthSessionStore oauthSessionStore;

    @Inject
    public AzureOAuthService(Invoker invoker, OauthSessionStore oauthSessionStore) {
        this.invoker = invoker;
        this.oauthSessionStore = oauthSessionStore;
        this.gsonJsonMapper = GsonJsonMapperImpl.getInstance();
    }

    private AzureAuthenticationCredential getAzureAuthenticationCredential() {
        String routingURL = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        return new AzureAuthenticationCredentialBuilder()
                .routingUrl(routingURL).build(attributes);
    }

    public String getAuthorizationURL() {
        this.attributes = invoker.getAttributes()
                .entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, entry -> (String) entry.getValue()));
        AzureAuthenticationCredential azureAuthenticationCredential = getAzureAuthenticationCredential();
        ConfidentialClientApplication clientApplication = buildClientApplication(azureAuthenticationCredential);
        return getAuthorizationUrlForCredential(azureAuthenticationCredential, clientApplication);
    }

    public String getAuthorizationUrlForFields(Map<String, String> fields) {
        this.attributes = fields;
        String routingURL = invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        AzureAuthenticationCredential authenticationCredential = new AzureAuthenticationCredentialBuilder()
                .routingUrl(routingURL)
                .build(fields);
        ConfidentialClientApplication confidentialClientApplication = buildClientApplication(authenticationCredential);
        return getAuthorizationUrlForCredential(authenticationCredential, confidentialClientApplication);
    }

    private String getAuthorizationUrlForCredential(AzureAuthenticationCredential azureAuthenticationCredential, ConfidentialClientApplication clientApplication) {
        URL authorizationRequestUrl = new AzureAuthorizationUrlBuilder(azureAuthenticationCredential, clientApplication)
                .build();
        String urlString = authorizationRequestUrl.toString();
        logger.debug("called auth url: {} and routingUrl: {}", urlString, azureAuthenticationCredential.getRoutingUrl());
        return urlString;
    }

    private ConfidentialClientApplication buildClientApplication(AzureAuthenticationCredential azureAuthenticationCredential) {
        try {
            IClientCredential credential = ClientCredentialFactory.createFromSecret(azureAuthenticationCredential.getClientSecret());
            return ConfidentialClientApplication
                    .builder(azureAuthenticationCredential.getClientId(), credential)
                    .authority(azureAuthenticationCredential.getAuthority())
                    .build();
        } catch (MalformedURLException cause) {
            throw new MalformedParametersException("error while construction authorization url: " + cause.getMessage());
        }
    }

    public CompletableFuture<String> getAccessTokenFuture(String code) {
        AzureAuthenticationCredential azureAuthenticationCredential = getAzureAuthenticationCredential();
        ConfidentialClientApplication clientApplication = buildClientApplication(azureAuthenticationCredential);

        String redirectUrl = azureAuthenticationCredential.getRedirectUrl();

        AuthorizationCodeParameters authorizationCodeParameters = AuthorizationCodeParameters
                .builder(code, URI.create(redirectUrl))
                .build();

        CompletableFuture<IAuthenticationResult> iAuthCompletableFuture = clientApplication.acquireToken(authorizationCodeParameters);
        IAuthenticationResult iAuthenticationResult = iAuthCompletableFuture.join();

        storeRefreshToken(clientApplication, iAuthenticationResult);

        oauthSessionStore.saveInvokerAttributes(attributes);

        return CompletableFuture.completedFuture(iAuthenticationResult.accessToken());
    }

    private void storeRefreshToken(ConfidentialClientApplication clientApplication, IAuthenticationResult iAuthenticationResult) {
        final String refreshToken = extractRefreshToken(clientApplication.tokenCache().serialize());
        String username = iAuthenticationResult.account().username();
        oauthSessionStore.saveRefreshToken(username, refreshToken);
    }


    public String updateTokens(CompletableFuture<String> refreshToken) {
            try {
                this.attributes = oauthSessionStore.getInvokerAttributes();

                String token = refreshToken.join();
                logger.info("Refresh Token in Update Token");

                String scope = "User.Read";
                Set<String> scopeSet = Arrays.stream(scope.split(BLANK_SPACE)).collect(Collectors.toSet());
                // Build RefreshTokenParameters
                RefreshTokenParameters refreshTokenParameters = RefreshTokenParameters.builder(scopeSet, token).build();

                // Authenticate and acquire new token
                AzureAuthenticationCredential azureAuthenticationCredential = getAzureAuthenticationCredential();
                ConfidentialClientApplication clientApplication = buildClientApplication(azureAuthenticationCredential);

                CompletableFuture<IAuthenticationResult> iAuthCompletableFuture  = clientApplication.acquireToken(refreshTokenParameters);
                IAuthenticationResult iAuthenticationResult = iAuthCompletableFuture.join();

                storeRefreshToken(clientApplication, iAuthenticationResult);
                return iAuthenticationResult.accessToken(); // Return the new access token

            } catch (GraphServiceException cause) {
                logger.error("Error refreshing token: {}", cause.getMessage(), cause);

                NamedValuedField clientIdField = new NamedValuedField(CLIENT_ID, TEXT_FIELD_TYPE, attributes.get(CLIENT_ID), Map.of(), Map.of());
                NamedValuedField clientSecretField = new NamedValuedField(CLIENT_SECRET, TEXT_FIELD_TYPE, attributes.get(CLIENT_SECRET), Map.of(), Map.of());
                NamedValuedField tenantIdField = new NamedValuedField(TENANT_ID, TEXT_FIELD_TYPE, attributes.get(TENANT_ID), Map.of(), Map.of());
                String authContextId = getAuthContextId(attributes.get(CLIENT_ID), attributes.get(CLIENT_SECRET), attributes.get(TENANT_ID));

                logger.info("AT Refresh Token Auth contextID : {}", authContextId);
                NamedValuedField authContextField = new NamedValuedField(AUTH_CONTEXT_ID, TEXT_FIELD_TYPE, authContextId, Map.of(), Map.of());

                throw new MustAuthorizeException("Unauthorized user, please authorize yourself", List.of(clientIdField, clientSecretField, tenantIdField, authContextField));
            } catch (Exception cause) {

                logger.error("Invalid Token: {}", cause.getMessage(), cause);
                throw new RuntimeException(cause);
            }
    }

    public String getAuthContextId(String clientId, String clientSecret, String tenantId) {
        String authContextId;
        authContextId = oauthSessionStore.storeAuthCredentials(Map.of(
                CLIENT_ID, clientId,
                CLIENT_SECRET, clientSecret,
                TENANT_ID, tenantId
        ));
        return authContextId;
    }



    public String extractRefreshToken(String tokenCache) {
        JsonObject jsonObject = gsonJsonMapper.fromString(tokenCache, JsonObject.class);
        JsonObject refreshToken = jsonObject.get("RefreshToken").getAsJsonObject();

        String firstKey = refreshToken.keySet().iterator().next();

        JsonObject tokenData = refreshToken.getAsJsonObject(firstKey);
        String secret = tokenData.get("secret").getAsString();
        logger.info("Updated Refresh Token.");
        return secret;
    }
}
