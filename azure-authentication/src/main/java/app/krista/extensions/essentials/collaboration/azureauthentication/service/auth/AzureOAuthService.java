package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import com.microsoft.aad.msal4j.AuthorizationRequestUrlParameters;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4j.ResponseMode;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.HashSet;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.DEFAULT_CALLBACK_PATH;

@Service
public class AzureOAuthService {
    private OAuth20Service oAuth20Service;
    private final RoutingInfo routingInfo;

    @Inject
    public AzureOAuthService(Invoker invoker) {
        this.routingInfo = invoker.getRoutingInfo();
        this.oAuth20Service = getOAuth20Service();
    }

    public synchronized OAuth20Service getOAuth20Service() {
        if (oAuth20Service == null) {
            AzureAuthenticationCredential credential = new AzureAuthenticationCredentialBuilder().build();
            String routingUrl = routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
            routingUrl = routingUrl.replaceAll("/extension/api/.*", DEFAULT_CALLBACK_PATH);
            oAuth20Service = new ServiceBuilder(credential.getClientId())
                    .defaultScope(credential.getScope())
                    .callback(routingUrl)
                    .apiSecret(credential.getClientSecret())
                    .build(MicrosoftAzureActiveDirectory20Api.custom(credential.getTenantId()));
        }
        return oAuth20Service;
    }

    public String getAuthorizationURL() {
        System.out.println("called auth url");
        String redirectURLState = routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE) + "/authn/callback";
        redirectURLState += "#&access_type=offline&approval_prompt=force&prompt=select_account&forward=true";
        return oAuth20Service.getAuthorizationUrl(redirectURLState);
    }
}
