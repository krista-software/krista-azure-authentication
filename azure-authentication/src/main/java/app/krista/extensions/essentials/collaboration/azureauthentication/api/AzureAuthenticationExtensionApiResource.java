package app.krista.extensions.essentials.collaboration.azureauthentication.api;

import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureOAuthService;
import app.krista.ksdk.context.AuthorizationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import java.util.Map;

import static com.github.scribejava.core.model.OAuthConstants.CODE;
import static com.github.scribejava.core.model.OAuthConstants.STATE;

@Path("/")
public class AzureAuthenticationExtensionApiResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureAuthenticationExtensionApiResource.class);
    private final AzureOAuthService oAuth20Service;
    private final AuthorizationContext authorizationContext;

    @Inject
    public AzureAuthenticationExtensionApiResource(AzureOAuthService oAuth20Service, AuthorizationContext authorizationContext) {
        this.oAuth20Service = oAuth20Service;
        this.authorizationContext = authorizationContext;
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String login() {
        return oAuth20Service.getAuthorizationURL();
    }

    @GET
    @Path("/callback")
    public Response getCallbackResponse(@QueryParam(CODE) String code, @QueryParam(STATE) String state) {
        if (authorizationContext.getAuthorizedAccount() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        return Response.ok(Map.of("accountId", authorizationContext.getAuthorizedAccount().getAccountId())).build();
    }
}
