package app.krista.extensions.essentials.collaboration.azureauthentication.api;

import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureOAuthService;
import com.github.scribejava.core.oauth.OAuth20Service;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Map;

@Path("/")
public class AzureAuthenticationExtensionApiResource {
    private final AzureOAuthService oAuth20Service;

    @Inject
    public AzureAuthenticationExtensionApiResource(AzureOAuthService oAuth20Service) {
        this.oAuth20Service = oAuth20Service;
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String login() {
        System.out.println("login hit");
        return oAuth20Service.getAuthorizationURL();
    }
}
