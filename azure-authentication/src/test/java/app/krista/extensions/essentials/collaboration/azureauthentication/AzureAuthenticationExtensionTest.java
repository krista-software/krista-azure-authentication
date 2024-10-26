package app.krista.extensions.essentials.collaboration.azureauthentication;

import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureAuthenticationCredential;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureAuthenticationCredentialBuilder;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureAuthorizationUrlBuilder;
import com.microsoft.aad.msal4j.*;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class AzureAuthenticationExtensionTest {
    @Test
    public void getAuthorizationUrl() throws MalformedURLException {
        AzureAuthenticationCredential azureAuthenticationCredential = new AzureAuthenticationCredentialBuilder()
                .routingUrl("http://localhost:8765/extension/api/FwoDdIazmBVeU4DRo0F_sdQ_e_e").buildCredentialFromProperties();
        IClientCredential credential = ClientCredentialFactory.createFromSecret(azureAuthenticationCredential.getClientSecret());
        ConfidentialClientApplication build = ConfidentialClientApplication
                .builder(azureAuthenticationCredential.getClientId(), credential)
                .authority(azureAuthenticationCredential.getAuthority())
                .build();
        AzureAuthorizationUrlBuilder azureAuthorizationUrlBuilder=new AzureAuthorizationUrlBuilder(azureAuthenticationCredential,build);
        URL authorizationRequestUrl = azureAuthorizationUrlBuilder.build();
        System.out.println("url:" + authorizationRequestUrl.toString());
        Assert.assertNotNull(authorizationRequestUrl);
    }
}
