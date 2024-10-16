package app.krista.extensions.essentials.collaboration.azureauthentication;

import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureAuthenticationCredential;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureAuthenticationCredentialBuilder;
import com.microsoft.aad.msal4j.*;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.BLANK_SPACE;

public class AzureAuthenticationExtensionTest {
    @Test
    public void getAuthorizationUrl() throws MalformedURLException {
        AzureAuthenticationCredential azureAuthenticationCredential = new AzureAuthenticationCredentialBuilder().build();
        Set<String> scopes = Arrays.stream(azureAuthenticationCredential.getScope().split(BLANK_SPACE)).collect(Collectors.toSet());
        String routingUrl = "http://localhost:8765/extension/api/FwoDdIazmBVeU4DRo0F_sdQ_e_e";
        String redirectURLState = routingUrl + "/authn/callback";
        redirectURLState += "#&access_type=offline&forward=true";
        AuthorizationRequestUrlParameters authorizationRequestUrlParameters = AuthorizationRequestUrlParameters
                .builder("http://localhost:8765/extension/api/authn/callback", scopes)
                .responseMode(ResponseMode.QUERY)
                .state(redirectURLState)
                .prompt(Prompt.SELECT_ACCOUNT)
                .build();
        IClientCredential credential = ClientCredentialFactory.createFromSecret(azureAuthenticationCredential.getClientSecret());
        ConfidentialClientApplication build = ConfidentialClientApplication
                .builder(azureAuthenticationCredential.getClientId(), credential)
                .authority(azureAuthenticationCredential.getAuthority())
                .build();
        URL authorizationRequestUrl = build.getAuthorizationRequestUrl(authorizationRequestUrlParameters);
        System.out.println("url:"+authorizationRequestUrl.toString());
        Assert.assertNotNull(authorizationRequestUrl);
    }
}
