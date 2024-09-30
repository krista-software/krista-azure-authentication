package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;

import app.krista.extension.executor.Invoker;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import com.github.scribejava.apis.MicrosoftAzureActiveDirectory20Api;
import com.github.scribejava.core.builder.ServiceBuilder;
import com.github.scribejava.core.oauth.OAuth20Service;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;

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
            oAuth20Service = new ServiceBuilder("df4cbf46-4775-4bf2-b1aa-5e08b44c8cfc")
                    .apiSecret("REDACTED_SECRET_0")
                    .defaultScope("user.read openid profile offline_access")
                    .callback(routingInfo.getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE) + "/azureauth/callback")
                    .build(MicrosoftAzureActiveDirectory20Api.custom("c4d5be5b-6f10-488d-b7db-c0676c352975"));
        }
        return oAuth20Service;
    }

    public String getAuthorizationURL() {
        return oAuth20Service.getAuthorizationUrl();
    }

}
