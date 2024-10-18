package app.krista.extensions.essentials.collaboration.azureauthentication.api;

import app.krista.extension.impl.jaxrs.util.Resources;
import app.krista.extensions.essentials.collaboration.azureauthentication.exception.FileOperationException;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureOAuthService;
import app.krista.extensions.essentials.collaboration.azureauthentication.store.OauthSessionStore;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants;
import app.krista.ksdk.context.AuthorizationContext;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;

import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Map;
import java.util.Objects;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.*;
import static javax.ws.rs.core.Cookie.DEFAULT_VERSION;
import static javax.ws.rs.core.HttpHeaders.SET_COOKIE;

@Path("/")
public class AzureAuthenticationExtensionApiResource {
    private static final Logger logger = LoggerFactory.getLogger(AzureAuthenticationExtensionApiResource.class);
    private final AuthorizationContext authorizationContext;
    private final OauthSessionStore oauthSessionStore;
    private final AzureOAuthService azureOAuthService;

    @Inject
    public AzureAuthenticationExtensionApiResource(OauthSessionStore oauthSessionStore, AuthorizationContext authorizationContext, AzureOAuthService azureOAuthService) {
        this.authorizationContext = authorizationContext;
        this.oauthSessionStore = oauthSessionStore;
        this.azureOAuthService = azureOAuthService;
    }

    @GET
    @Path("authenticator.js")
    public InputStream getAuthenticator() {
        return getClass().getClassLoader().getResourceAsStream("authenticator.js");
    }

    @GET
    @Path("/type")
    public String getAuthType() {
        return AUTHENTICATION_TYPE;
    }

    @OPTIONS
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response loginOptions(@Context java.net.http.HttpHeaders headers, String email) {
        return getResponseBuilderWithCORSHeaders().build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response login(@Context java.net.http.HttpHeaders headers, String email) {
        return getResponseBuilderWithCORSHeaders().entity(Map.of("authUrl", azureOAuthService.getAuthorizationURL())).build();

    }

    @GET
    @Path("/callback")
    public Response getCallbackResponse(@QueryParam(CODE) String code, @QueryParam(STATE) String state, @CookieParam(Constants.CHATBOT_SESSION_ID) Cookie sessionCookieInRequest) {
        if (authorizationContext.getAuthorizedAccount() == null) {
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream("index.html");
        if (!Objects.isNull(sessionCookieInRequest) && !sessionCookieInRequest.getValue().isBlank()) {
            return getResponseBuilderWithCORSHeaders()
                    .header(Constants.CHATBOT_SESSION_ID, sessionCookieInRequest.getValue())
                    .entity(resourceAsStream)
                    .build();
        }

        int maxAgeInSeconds = getMaxAgeInSeconds();
        Date expiry = new Date(System.currentTimeMillis() + maxAgeInSeconds * 1000L);
        String sessionId = oauthSessionStore.getSessionId(authorizationContext.getAuthorizedAccount().getAccountId());
        try {
            Response response = Resources.getTranslatedResource(getClass(),
                    "index.html", Map.of("_clientSessionId_", sessionId));
            NewCookie sessionCookie = new NewCookie(Constants.CHATBOT_SESSION_ID,
                    sessionId, "/", "*", DEFAULT_VERSION, Constants.COMMENT, maxAgeInSeconds, expiry, true, true);
            NewCookie xKristaContextCookie = new NewCookie(Constants.X_KRISTA_CONTEXT,
                    getEncodedClientSessionId(sessionId), "/", "*", DEFAULT_VERSION, Constants.COMMENT, maxAgeInSeconds, expiry, true, true);
            return getResponseBuilderWithCORSHeaders()
                    .cookie(sessionCookie)
                    .header(SET_COOKIE, xKristaContextCookie)
                    .header(Constants.CHATBOT_SESSION_ID, sessionId)
                    .entity(response.getEntity())
                    .build();
        } catch (IOException cause) {
            throw new FileOperationException("error occurred while loading page", cause);
        }
    }

    private String getEncodedClientSessionId(String sessionId) {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("clientSessionId", sessionId);
        return URLEncoder.encode(jsonObject.toString(), StandardCharsets.UTF_8);
    }

    private Response.ResponseBuilder getResponseBuilderWithCORSHeaders() {
        return Response.status(200)
                .header("Access-Control-Allow-Origin", "*")
                .header("Access-Control-Allow-Methods", "POST, GET, OPTIONS")
                .header("Access-Control-Allow-Headers", "Content-Type");
    }

    private int getMaxAgeInSeconds() {
        return 24 * 60 * 60;
    }
}
