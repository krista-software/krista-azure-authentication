/*
 * Azure Authentication Extension for Krista
 * Copyright (C) 2025 Krista Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package app.krista.extensions.essentials.collaboration.azureauthentication.api;

import app.krista.extension.authorization.AuthorizationException;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.jaxrs.util.Resources;
import app.krista.extension.request.RoutingInfo;
import app.krista.extension.request.protos.http.HttpProtocol;
import app.krista.extensions.essentials.collaboration.azureauthentication.common.AuthenticationResponse;
import app.krista.extensions.essentials.collaboration.azureauthentication.exception.FileOperationException;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureOAuthService;
import app.krista.extensions.essentials.collaboration.azureauthentication.store.OauthSessionStore;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.CommonUtils;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.accounts.AccountManager;
import app.krista.ksdk.accounts.ModifiableAccount;
import app.krista.ksdk.authentication.JwtSessionManager;
import app.krista.ksdk.authorization.Role;
import app.krista.ksdk.authorization.RoleManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.ksdk.context.RuntimeContext;
import app.krista.model.field.util.GsonJsonMapper;
import app.krista.omni.sdk.spi.util.GsonJsonMapperImpl;
import com.google.gson.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
    private final Invoker invoker;
    private final JwtSessionManager jwtSessionManager;

    @Inject
    public AzureAuthenticationExtensionApiResource(OauthSessionStore oauthSessionStore, AuthorizationContext authorizationContext, AzureOAuthService azureOAuthService, RuntimeContext runtimeContext, RoleManager roleManager, AccountManager accountManager, Invoker invoker, JwtSessionManager jwtSessionManager) {
        this.authorizationContext = authorizationContext;
        this.oauthSessionStore = oauthSessionStore;
        this.azureOAuthService = azureOAuthService;
        this.runtimeContext = runtimeContext;
        this.roleManager = roleManager;
        this.accountManager = accountManager;
        this.invoker = invoker;
        this.jwtSessionManager = jwtSessionManager;
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
    public Response login(@HeaderParam(CLIENT_SESSION_ID) String clientSessionId, @HeaderParam(Constants.CALLER_URI) String callerUri) throws URISyntaxException {
        logger.info("authorization context id:{} and clientSessionId: {}", authorizationContext.getAuthorizedAccount().getAccountId(), clientSessionId);
        String accountId = oauthSessionStore.getSessionId(clientSessionId);
        logger.info("Account Id is fetched from Auth Session Store at Login");
        ModifiableAccount account = accountManager.getAccount(accountId);
        logger.info("Account is fetched from From Account Manager at Login");
        String accessToken = jwtSessionManager.createAccessTokenFromRefreshToken(clientSessionId);
        logger.info("Access Token Generated at Login");
        AuthenticationResponse authenticationResponse = createAuthenticationResponse(account, clientSessionId, accessToken);
        logger.info("AuthenticationResponse is Generated");

        int maxAgeInSeconds = getMaxAgeInSeconds();
        Date expiry = new Date(System.currentTimeMillis() + maxAgeInSeconds * 1000L);
        String routingURL =
                invoker.getRoutingInfo().getRoutingURL(HttpProtocol.PROTOCOL_NAME, RoutingInfo.Type.APPLIANCE);
        URI httpURI = new URI((!Objects.isNull(callerUri) && !callerUri.isBlank()) ? callerUri : routingURL);

        String chatbotSessionCookie = getChatbotSessionCookie(clientSessionId, httpURI, maxAgeInSeconds, expiry);
        String kristaContextCookie = getKristaContextCookie(clientSessionId, httpURI, maxAgeInSeconds, expiry);

        return Response.ok(GsonJsonMapperImpl.getInstance().toString(authenticationResponse))
                .header(SET_COOKIE, chatbotSessionCookie)
                .header(SET_COOKIE, kristaContextCookie)
                .header(Constants.CHATBOT_SESSION_ID, clientSessionId)
                .entity(GsonJsonMapper.create().toString(authenticationResponse))
                .build();
    }

    @NotNull
    private static String getChatbotSessionCookie(String clientSessionId, URI httpURI, int maxAgeInSeconds, Date expiry) {
        return CommonUtils.buildSecureCookie(
                Constants.CHATBOT_SESSION_ID,
                clientSessionId,
                httpURI.getHost(),
                maxAgeInSeconds,
                expiry
        );
    }

    @NotNull
    private String getKristaContextCookie(String clientSessionId, URI httpURI, int maxAgeInSeconds, Date expiry) {
        return CommonUtils.buildSecureCookie(
                Constants.X_KRISTA_CONTEXT,
                getEncodedClientSessionId(clientSessionId),
                httpURI.getHost(),
                maxAgeInSeconds,
                expiry
        );
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
            throw new FileOperationException(
                "Unable to load the authentication page. This may be due to network issues or server problems. " +
                "Please try again in a few moments.", cause);
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

    private AuthenticationResponse createAuthenticationResponse(Account account, String sessionId, String accessToken)
            throws AuthorizationException {
        return new AuthenticationResponse(sessionId, account.getPerson().getPersonName(),
                account.getPerson().getAvatarUrl(), account.getAccountId(),
                runtimeContext.getKristaAccount().getAccountId(), account.getPerson().getPersonId(),
                getListOfRoles(account.getRoles()), account.getInboxId(),
                roleManager.isWorkspaceAdmin(account),
                false,
                Map.of("email", account.getPrimaryEmailAddress()),
                Map.of("creationTime", getCurrentDateTime(), "newSession", true, "accessToken", accessToken));
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
