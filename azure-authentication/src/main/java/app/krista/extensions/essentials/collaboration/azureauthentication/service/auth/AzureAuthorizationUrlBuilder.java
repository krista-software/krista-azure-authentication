package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

import com.microsoft.aad.msal4j.AuthorizationRequestUrlParameters;
import com.microsoft.aad.msal4j.ConfidentialClientApplication;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.ResponseMode;

import java.net.URL;
/**
 * AzureAuthorizationUrlBuilder is responsible for constructing authorization URLs
 * for Azure Active Directory (AAD) OAuth 2.0 authentication flows. It makes use of
 * Azure authentication credentials and a confidential client application to generate
 * a valid authorization URL.
 *
 * <p>This class is useful when integrating Azure's OAuth 2.0 authorization code flow
 * in an application that interacts with Azure resources securely.</p>
 *
 * <p>Example usage:</p>
 * <pre>
 * AzureAuthorizationUrlBuilder builder = new AzureAuthorizationUrlBuilder(
 *      azureAuthenticationCredential, confidentialClientApplication
 * );
 * URL authorizationUrl = builder.build();
 * </pre>
 */
public class AzureAuthorizationUrlBuilder {

    /**
     * Azure authentication credential object, holding the necessary credentials to authenticate
     * against Azure Active Directory.
     */
    private final AzureAuthenticationCredential azureAuthenticationCredential;

    /**
     * Confidential client application representing an application that can maintain confidentiality
     * of its client credentials (such as a web app or daemon app). It is used for OAuth 2.0 token
     * acquisition.
     */
    private final ConfidentialClientApplication confidentialClientApplication;

    /**
     * Constructs an AzureAuthorizationUrlBuilder with the necessary credentials and client application.
     *
     * @param azureAuthenticationCredential the Azure authentication credential needed to authorize the client.
     * @param confidentialClientApplication the confidential client application used for token acquisition.
     */
    public AzureAuthorizationUrlBuilder(AzureAuthenticationCredential azureAuthenticationCredential,
                                        ConfidentialClientApplication confidentialClientApplication) {
        this.azureAuthenticationCredential = azureAuthenticationCredential;
        this.confidentialClientApplication = confidentialClientApplication;
    }

    /**
     * Builds and returns the authorization URL for initiating the Azure OAuth 2.0 authorization code flow.
     *
     * <p>This method creates a URL that the user can navigate to in order to sign in and give consent to
     * the application to access resources on their behalf.</p>
     *
     * @return the constructed authorization URL.
     */
    public URL build() {
        AuthorizationRequestUrlParameters authorizationRequestUrlParameters = getAuthorizationRequestUrlParameters(azureAuthenticationCredential);
        return confidentialClientApplication.getAuthorizationRequestUrl(authorizationRequestUrlParameters);
    }

    private AuthorizationRequestUrlParameters getAuthorizationRequestUrlParameters(AzureAuthenticationCredential azureAuthenticationCredential) {
        String redirectURLState = azureAuthenticationCredential.getRoutingUrl() + "/authn/callback";
        redirectURLState += "#&access_type=offline&forward=true";
        return AuthorizationRequestUrlParameters
                .builder(azureAuthenticationCredential.getRedirectUrl(), azureAuthenticationCredential.getScopes())
                .responseMode(ResponseMode.QUERY)
                .state(redirectURLState)
                .prompt(Prompt.SELECT_ACCOUNT)
                .build();
    }
}
