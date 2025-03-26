package app.krista.extensions.essentials.collaboration.azureauthentication.api;

import app.krista.extension.authorization.AuthorizationException;
import app.krista.extension.impl.jaxrs.util.Resources;
import app.krista.extensions.essentials.collaboration.azureauthentication.common.AuthenticationResponse;
import app.krista.extensions.essentials.collaboration.azureauthentication.exception.FileOperationException;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureOAuthService;
import app.krista.extensions.essentials.collaboration.azureauthentication.store.OauthSessionStore;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.accounts.AccountManager;
import app.krista.ksdk.accounts.ModifiableAccount;
import app.krista.ksdk.authorization.Role;
import app.krista.ksdk.authorization.RoleManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RuntimeContext;
import app.krista.omni.sdk.spi.util.GsonJsonMapperImpl;
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
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.*;
import static javax.ws.rs.core.Cookie.DEFAULT_VERSION;
import static javax.ws.rs.core.HttpHeaders.SET_COOKIE;

@Path("/")
public class AzureAuthenticationExtensionApiResource {
    private static final Logger logger = LoggerFactory.getLogger(AzureAuthenticationExtensionApiResource.class);
    private final AuthorizationContext authorizationContext;
    private final OauthSessionStore oauthSessionStore;
    private final AzureOAuthService azureOAuthService;
    private final RuntimeContext runtimeContext;
    private final RoleManager roleManager;
    private final AccountManager accountManager;

    @Inject
    public AzureAuthenticationExtensionApiResource(OauthSessionStore oauthSessionStore, AuthorizationContext authorizationContext, AzureOAuthService azureOAuthService, RuntimeContext runtimeContext, RoleManager roleManager, AccountManager accountManager) {
        this.authorizationContext = authorizationContext;
        this.oauthSessionStore = oauthSessionStore;
        this.azureOAuthService = azureOAuthService;
        this.runtimeContext = runtimeContext;
        this.roleManager = roleManager;
        this.accountManager = accountManager;
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
        return Response.status(Response.Status.NO_CONTENT).build();
    }

    @POST
    @Path("/authorize")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response authorize(@HeaderParam(CLIENT_SESSION_ID) String clientSessionId) {
        return Response.ok(Map.of(Constants.AUTH_URL_KEY, azureOAuthService.getAuthorizationURL())).build();
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@HeaderParam(CLIENT_SESSION_ID) String clientSessionId) {
        logger.info("authorization context id:{} and clientSessionId: {}", authorizationContext.getAuthorizedAccount().getAccountId(), clientSessionId);
        String accountId = oauthSessionStore.getSessionId(clientSessionId);
        logger.info("Account Id is fetched from Auth Session Store at Login");
        ModifiableAccount account = accountManager.getAccount(accountId);
        logger.info("Account is fetched from From Account Manager at Login");
        AuthenticationResponse authenticationResponse = createAuthenticationResponse(account, clientSessionId);
        logger.info("AuthenticationResponse is Generated");
        int maxAgeInSeconds = getMaxAgeInSeconds();
        Date expiry = new Date(System.currentTimeMillis() + maxAgeInSeconds * 1000L);
        NewCookie sessionCookie = new NewCookie(Constants.CHATBOT_SESSION_ID,
                clientSessionId, "/", "*", DEFAULT_VERSION, Constants.COMMENT, maxAgeInSeconds, expiry, true, true);
        return Response.ok(GsonJsonMapperImpl.getInstance().toString(authenticationResponse))
                .cookie(sessionCookie)
                .header(SET_COOKIE, sessionCookie)
                .header(Constants.CHATBOT_SESSION_ID, clientSessionId)
                .build();
    }

    @GET
    @Path("/callback")
    public Response getCallbackResponse(@QueryParam(CODE) String code, @QueryParam(STATE) String state, @CookieParam(Constants.CHATBOT_SESSION_ID) Cookie sessionCookieInRequest) {
        if (authorizationContext.getAuthorizedAccount() == null) {
            logger.info("UnAuthorized Account At getCallbackResponse");
            return Response.status(Response.Status.UNAUTHORIZED).build();
        }
        String sessionId = oauthSessionStore.getSessionId(authorizationContext.getAuthorizedAccount().getAccountId());
        logger.info("Session Id is fetched from Auth Session Store At getCallbackResponse");
        try {
            Response response = Resources.getTranslatedResource(getClass(),
                    "index.html", Map.of("_clientSessionId_", sessionId));
            logger.info("Client Session Id Response Created At getCallbackResponse");
            if (!Objects.isNull(sessionCookieInRequest) && !sessionCookieInRequest.getValue().isBlank()) {
                return Response.ok(response.getEntity())
                        .header(Constants.CHATBOT_SESSION_ID, sessionCookieInRequest.getValue())
                        .build();
            }

            int maxAgeInSeconds = getMaxAgeInSeconds();
            Date expiry = new Date(System.currentTimeMillis() + maxAgeInSeconds * 1000L);
            NewCookie sessionCookie = new NewCookie(Constants.CHATBOT_SESSION_ID,
                    sessionId, "/", "*", DEFAULT_VERSION, Constants.COMMENT, maxAgeInSeconds, expiry, true, true);
            NewCookie xKristaContextCookie = new NewCookie(Constants.X_KRISTA_CONTEXT,
                    getEncodedClientSessionId(sessionId), "/", "*", DEFAULT_VERSION, Constants.COMMENT, maxAgeInSeconds, expiry, true, true);
            return Response.ok(response.getEntity())
                    .cookie(sessionCookie)
                    .header(SET_COOKIE, xKristaContextCookie)
                    .header(Constants.CHATBOT_SESSION_ID, sessionId)
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


    private int getMaxAgeInSeconds() {
        return 24 * 60 * 60;
    }

    private AuthenticationResponse createAuthenticationResponse(Account account, String sessionId)
            throws AuthorizationException {
        return new AuthenticationResponse(sessionId, account.getPerson().getPersonName(),
                account.getPerson().getAvatarUrl(), account.getAccountId(),
                runtimeContext.getKristaAccount().getAccountId(), account.getPerson().getPersonId(),
                getListOfRoles(account.getRoles()), account.getInboxId(),
                roleManager.isWorkspaceAdmin(account),
                false,
                Map.of("email", account.getPrimaryEmailAddress()),
                Map.of("creationTime", getCurrentDateTime(), "newSession", true));
    }

    private String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    private List<String> getListOfRoles(Set<Role> roles) {
        List<String> allRoles = new ArrayList<>();
        for (Role role : roles) {
            allRoles.add(role.getRoleId());
        }
        return allRoles;
    }

}
