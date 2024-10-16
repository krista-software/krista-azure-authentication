package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import com.microsoft.aad.msal4j.*;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;

import javax.inject.Inject;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.concurrent.CompletableFuture;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.*;

@Service
public class AzureOAuthService {
    private static final Logger LOGGER = org.slf4j.LoggerFactory.getLogger(AzureOAuthService.class);
    private final ConfidentialClientApplication clientApplication;
    private final AzureAuthenticationCredential azureAuthenticationCredential;
    private final RoutingInfo routingInfo;

    @Inject
    public AzureOAuthService(Invoker invoker) {
        this.azureAuthenticationCredential = new AzureAuthenticationCredentialBuilder()
                .clientId((String) invoker.getAttributes().get(CLIENT_ID))
                .clientSecret((String) invoker.getAttributes().get(CLIENT_SECRET))
                .tenantId((String) invoker.getAttributes().get(TENANT_ID))
                .redirectUrl(invoker.getRoutingInfo()).build();
        this.clientApplication = buildClientApplication();
        this.routingInfo = invoker.getRoutingInfo();
    }

    private ConfidentialClientApplication buildClientApplication() {
        try {
            IClientCredential credential = ClientCredentialFactory.createFromSecret(azureAuthenticationCredential.getClientSecret());
            return ConfidentialClientApplication
                    .builder(azureAuthenticationCredential.getClientId(), credential)
                    .authority(azureAuthenticationCredential.getAuthority())
                    .build();
        } catch (MalformedURLException cause) {
            LOGGER.error("Error while creating client application", cause);
        }
        return null;
    }

    public String getAuthorizationURL() {
        String redirectURLState = routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE) + "/authn/callback";
        redirectURLState += "#&access_type=offline&forward=true";
        AuthorizationRequestUrlParameters authorizationRequestUrlParameters = AuthorizationRequestUrlParameters
                .builder(azureAuthenticationCredential.getRedirectUrl(), azureAuthenticationCredential.getScopes())
                .responseMode(ResponseMode.QUERY)
                .state(redirectURLState)
                .prompt(Prompt.SELECT_ACCOUNT)
                .build();
        URL authorizationRequestUrl = clientApplication.getAuthorizationRequestUrl(authorizationRequestUrlParameters);
        String urlString = authorizationRequestUrl.toString();
        LOGGER.debug("called auth url: {} and routingUrl: {}", urlString, azureAuthenticationCredential.getRedirectUrl());
        return urlString;
    }

    public CompletableFuture<String> getAccessTokenFuture(String code) {
        String redirectUrl = azureAuthenticationCredential.getRedirectUrl();
        AuthorizationCodeParameters authorizationCodeParameters = AuthorizationCodeParameters.builder(code, URI.create(redirectUrl)).build();
        CompletableFuture<IAuthenticationResult> iAuthCompletableFuture = clientApplication.acquireToken(authorizationCodeParameters);
        IAuthenticationResult iAuthenticationResult = iAuthCompletableFuture.join();
        return CompletableFuture.completedFuture(iAuthenticationResult.accessToken());
    }
}
