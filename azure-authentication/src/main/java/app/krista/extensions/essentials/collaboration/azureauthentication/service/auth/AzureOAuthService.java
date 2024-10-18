package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import com.microsoft.aad.msal4j.*;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;

import javax.inject.Inject;

import java.lang.reflect.MalformedParametersException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class AzureOAuthService {
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(AzureOAuthService.class);
    private final Invoker invoker;
    private Map<String, String> attributes;

    @Inject
    public AzureOAuthService(Invoker invoker) {
        this.invoker = invoker;
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
        AuthorizationCodeParameters authorizationCodeParameters = AuthorizationCodeParameters.builder(code, URI.create(redirectUrl)).build();
        CompletableFuture<IAuthenticationResult> iAuthCompletableFuture = clientApplication.acquireToken(authorizationCodeParameters);
        IAuthenticationResult iAuthenticationResult = iAuthCompletableFuture.join();
        return CompletableFuture.completedFuture(iAuthenticationResult.accessToken());
    }
}
