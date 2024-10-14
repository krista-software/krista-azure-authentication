package app.krista.extensions.essentials.collaboration.azureauthentication.api;

import app.krista.extensions.essentials.collaboration.azureauthentication.store.AzureAuthenticationTokenStore;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants;
import app.krista.ksdk.authentication.SessionManager;
import app.krista.ksdk.context.AuthorizationContext;
import com.google.gson.JsonObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.Cookie;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Objects;

import static com.github.scribejava.core.model.OAuthConstants.CODE;
import static com.github.scribejava.core.model.OAuthConstants.STATE;
import static javax.ws.rs.core.Cookie.DEFAULT_VERSION;
import static javax.ws.rs.core.HttpHeaders.SET_COOKIE;

@Path("/")
public class AzureAuthenticationExtensionApiResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureAuthenticationExtensionApiResource.class);
    private final AuthorizationContext authorizationContext;
    private final AzureAuthenticationTokenStore tokenStore;

    @Inject
    public AzureAuthenticationExtensionApiResource(AzureAuthenticationTokenStore tokenStore, AuthorizationContext authorizationContext) {
        this.authorizationContext = authorizationContext;
        this.tokenStore = tokenStore;
    }

    @GET
    @Path("/callback")
    public Response getCallbackResponse(@QueryParam(CODE) String code, @QueryParam(STATE) String state, @CookieParam(Constants.CHATBOT_SESSION_ID) Cookie sessionCookieInRequest) {
        LOGGER.info("Received code: {} ", code);
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
        String sessionId = tokenStore.getSessionId(authorizationContext.getAuthorizedAccount().getAccountId());
       /* String chatbotSessionCookie = Constants.CHATBOT_SESSION_ID + Constants.EQUAL_TO + sessionId +
                "; Version=" + DEFAULT_VERSION +
                "; Comment=" + Constants.COMMENT +
                "; Max-Age=" + maxAgeInSeconds +
                "; Expires=" + expiry +
                ";Secure;HttpOnly;SameSite=None;";*/

       /* String kristaContextCookie =
                Constants.X_KRISTA_CONTEXT + Constants.EQUAL_TO + getEncodedClientSessionId(sessionId) +
                        "; Version=" + DEFAULT_VERSION +
                        "; Comment=" + Constants.COMMENT +
                        "; Max-Age=" + maxAgeInSeconds +
                        "; Expires=" + expiry +
                        ";Secure;HttpOnly;SameSite=None;";*/
        // Building the response with CORS headers and cookie
        NewCookie sessionCookie = new NewCookie(Constants.CHATBOT_SESSION_ID,
                sessionId, "/", "*", DEFAULT_VERSION, Constants.COMMENT, maxAgeInSeconds, expiry, true, true);
        NewCookie xKristaContextCookie = new NewCookie(Constants.X_KRISTA_CONTEXT,
                getEncodedClientSessionId(sessionId), "/", "*", DEFAULT_VERSION, Constants.COMMENT, maxAgeInSeconds, expiry, true, true);
        return getResponseBuilderWithCORSHeaders()
                .cookie(sessionCookie)
                .header(SET_COOKIE, xKristaContextCookie)
                .header(Constants.CHATBOT_SESSION_ID, sessionId)
                .entity(resourceAsStream)
                .build();
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
