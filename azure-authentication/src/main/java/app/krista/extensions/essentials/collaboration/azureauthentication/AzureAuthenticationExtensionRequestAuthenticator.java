package app.krista.extensions.essentials.collaboration.azureauthentication;

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.extension.request.protos.http.HttpResponse;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.AccountManagerService;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.UserInfoService;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureOAuthService;
import app.krista.extensions.essentials.collaboration.azureauthentication.store.OauthSessionStore;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.CommonUtils;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants;
import app.krista.ksdk.accounts.AccountProvider;
import app.krista.ksdk.authentication.SessionManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedField;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.util.*;
import java.util.concurrent.CompletableFuture;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.*;

@Service
public class AzureAuthenticationExtensionRequestAuthenticator implements RequestAuthenticator {
    private static final Logger logger = LoggerFactory.getLogger(AzureAuthenticationExtensionRequestAuthenticator.class);
    private final AuthorizationContext authorizationContext;
    private final UserInfoService userInfoService;
    private final OauthSessionStore oauthSessionStore;
    private final AccountManagerService accountManagerService;
    private final AzureOAuthService azureOAuthService;
    private final SessionManager sessionManager;
    private final AccountProvider accountProvider;

    @Inject
    public AzureAuthenticationExtensionRequestAuthenticator(AuthorizationContext authorizationContext, UserInfoService userInfoService, OauthSessionStore oauthSessionStore, AccountManagerService accountManagerService, AzureOAuthService azureOAuthService, SessionManager sessionManager, AccountProvider accountProvider) {
        this.authorizationContext = authorizationContext;
        this.userInfoService = userInfoService;
        this.oauthSessionStore = oauthSessionStore;
        this.accountManagerService = accountManagerService;
        this.azureOAuthService = azureOAuthService;
        this.sessionManager = sessionManager;
        this.accountProvider = accountProvider;
    }

    @Override
    public String getScheme() {
        return "";
    }

    @Override
    public Set<String> getSupportedProtocols() {
        return Set.of();
    }

    @Override
    public String getAuthenticatedAccountId(ProtoRequest protoRequest) {
        HttpRequest httpRequest = (HttpRequest) protoRequest;
        try {
            httpRequest.bufferBody();
            String path = httpRequest.getUri().getPath();
            logger.info("AZURE AUTH PATH: {}", path);
            if (Objects.equals("/login", path)) {
                logger.info("Preparing for Login at getAuthenticatedAccountId");
                return authorizationContext.getAuthorizedAccount().getAccountId();
            }

            final boolean isWebFormRequests = path.endsWith("/omni-form.css") || path.endsWith("/omni-form.js");
            if (isWebFormRequests) {
                // In case of web form requests, there is no any user before submitting form.
                // We need X-Krista-CSF to prepare for media upload.
                // To create X-Krista-CSF, we need to return any valid account id.
                return authorizationContext.getAuthorizedAccount().getAccountId();
            }

            if (Objects.equals("/callback", path)) {
                MultivaluedMap<String, String> queryParameters = httpRequest.getQueryParameters();
                List<String> code = queryParameters.get("code");
                logger.info("Authorization Context: {}", authorizationContext.getAuthorizedAccount().getAccountId());
                return handleCallback(code.get(0));
            }
            String clientSessionId = getClientSessionId(httpRequest);
            logger.info("Client Session Id is Received : {}", clientSessionId);
            if (!Objects.isNull(clientSessionId)) {
                return getAuthenticatedAccountId(clientSessionId);
            }
            return null;
        } catch (Exception cause) {
            throw new IllegalStateException("Failed to authenticate : Reason: " + cause.getMessage(), cause);
        }
    }

    @Override
    public boolean setServiceAuthorization(String s) {
        return false;
    }

    @Override
    public Map<String, NamedField> getAttributeFields() {
        return Map.of();
    }

    @Override
    public ProtoResponse getMustAuthenticateResponse(MustAuthenticateException e, ProtoRequest protoRequest) {
        logger.info("Preparing for ProtoResponse of getMustAuthenticateResponse");
        return new HttpResponse(302,
                Map.of(HttpHeaders.LOCATION, azureOAuthService.getAuthorizationURL(),
                        "Access-Control-Allow-Origin", "*"),
                new ByteArrayInputStream(new byte[0]));
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException e) {
        logger.info("Preparing for callback");
        return new AuthorizationResponse(azureOAuthService.getAuthorizationURL(), List.of());
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException e, ProtoRequest protoRequest) {
        logger.info("Preparing for ProtoResponse of getMustAuthorizeResponse");
        HttpRequest httpRequest = (HttpRequest) protoRequest;
        String routingUrl = httpRequest.getHeader("routingUrl");
        return new HttpResponse(302,
                Map.of(HttpHeaders.LOCATION, azureOAuthService.getAuthorizationURL() + "?caller=" + routingUrl + httpRequest.getUri().toString(),
                        "Access-Control-Allow-Origin", "*"),
                new ByteArrayInputStream(new byte[0]));
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException cause) {
        String authContextId = (String)cause.getDetails().get(3).getValue();
        logger.info("Auth Context ID found : {}", authContextId);
        Map<String, String> invokerAttributes = oauthSessionStore.getOAuthAttributes(authContextId);
        String authorizationUrlForFields = azureOAuthService.getAuthorizationUrlForFields(invokerAttributes);

        oauthSessionStore.removeOAuthAttributes(authContextId);

        return new AuthorizationResponse(authorizationUrlForFields, List.of());
    }

    private String getClientSessionId(HttpRequest httpRequest) {
        String cookie = httpRequest.getHeader(Constants.COOKIE);
        String clientSessionId = getClientSessionIdFromCookie(cookie);
        logger.info("Fetching Client Session Id from Cookie : {}", clientSessionId);
        String verifiedClientSessionId = isValidClientSessionId(clientSessionId) ? clientSessionId : null;
        logger.info("Verifying Client Session Id from Cookie : {}", verifiedClientSessionId);
        if (Objects.isNull(verifiedClientSessionId)) {
            return getClientSessionIdFromHeaderOrQueryParams(httpRequest);
        }
        return verifiedClientSessionId;
    }

    private String getClientSessionIdFromHeaderOrQueryParams(HttpRequest httpRequest) {
        String clientSessionId = httpRequest.getHeader(CLIENT_SESSION_ID);
        logger.info("Client Session Id from query param: {}", clientSessionId);

        String verifiedClientSessionId = isValidClientSessionId(clientSessionId) ? clientSessionId : null;

        if (Objects.isNull(verifiedClientSessionId)) {
            MultivaluedMap<String, String> queryParameters = httpRequest.getQueryParameters();
            logger.info("Client Session Id is not available in header, now fetching from query parameter");
            if (!queryParameters.isEmpty()) {
                List<String> queryParamSessionId = queryParameters.get(CLIENT_SESSION_ID);
                String queryClientSessionId = !Objects.isNull(queryParamSessionId) && !queryParamSessionId.isEmpty() ? queryParamSessionId.get(0) : null;

                logger.info("Client Session Id Fetched from query parameter before validation : {}", queryClientSessionId);
                verifiedClientSessionId = isValidClientSessionId(queryClientSessionId) ? queryClientSessionId : null;
                logger.info("Client Session Id Fetched from query parameter : {}", verifiedClientSessionId);
            }
        }
        return verifiedClientSessionId;
    }

    private boolean isValidClientSessionId(String clientSessionId){
        if(Objects.isNull(clientSessionId)){
            return false;
        }
        return getAuthenticatedAccountIdFromSession(clientSessionId) != null;
    }

    private String getAuthenticatedAccountIdFromSession(String clientSessionId){
        if(Objects.isNull(clientSessionId)){
            return null;
        }
        String authenticatedAccountId = sessionManager.lookupAccountId(clientSessionId);
        logger.info("Received authenticatedAccountId from Session Manager : {}", authenticatedAccountId);
        if(authenticatedAccountId != null){
            return isAccountVerifiedInCurrentInvoker(authenticatedAccountId) ? authenticatedAccountId : null;
        }
        return null;
    }

    private String getAuthenticatedAccountId(String clientSessionId) {
        String authenticatedAccountId = sessionManager.lookupAccountId(clientSessionId);
        logger.info("Received authenticatedAccountId from Session Manager At getAuthenticatedAccountId: {}", authenticatedAccountId);
        if (!Objects.isNull(authenticatedAccountId)) {
            try {
                accountProvider.getAccount(authenticatedAccountId);
                logger.info("Got Authenticated AccountId: {}", authenticatedAccountId);
                return authenticatedAccountId;
            } catch (NoSuchElementException cause) {
                logger.error("No such account Id exist: {} with exception: {}", authenticatedAccountId, cause);
            }
        }
        logger.info("No account found for clientSessionId: {}", clientSessionId);
        return null;
    }

    private boolean isAccountVerifiedInCurrentInvoker(String authenticatedAccountId) {
        return oauthSessionStore.getSessionId(authenticatedAccountId) != null;
    }

    private String getClientSessionIdFromCookie(String cookie) {
        if (!Objects.isNull(cookie)) {
            String[] cookies = cookie.split(Constants.SEMI_COLON);
            for (String c : cookies) {
                logger.info("cookie :{}", c);
                if (c.trim().startsWith(CLIENT_SESSION_ID)) {
                    return CommonUtils.findValue(c);
                }
                if (c.trim().startsWith(CHATBOT_SESSION_ID)) {
                    return CommonUtils.findValue(c);
                }
            }
        }
        return null;
    }

    public String handleCallback(String code) {
        CompletableFuture<String> accessTokenFuture = azureOAuthService.getAccessTokenFuture(code);
        String email = userInfoService.getEmail(accessTokenFuture);

        logger.info("{} authenticated successfully", email);

        oauthSessionStore.saveAccessToken(email, accessTokenFuture.join());

        String accountIdByEmail = accountManagerService.createAccount(email);
        String sessionId = sessionManager.createSession(accountIdByEmail);

        logger.info("session id:{} for user: {}", sessionId, email);
        oauthSessionStore.saveSessionId(accountIdByEmail, sessionId);
        return accountIdByEmail;
    }
}
