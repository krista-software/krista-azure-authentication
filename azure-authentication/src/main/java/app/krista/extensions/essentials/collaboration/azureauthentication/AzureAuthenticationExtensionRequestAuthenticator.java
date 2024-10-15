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
import app.krista.extensions.essentials.collaboration.azureauthentication.store.AzureAuthenticationTokenStore;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants;
import app.krista.ksdk.accounts.AccountProvider;
import app.krista.ksdk.authentication.SessionManager;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedField;
import com.github.scribejava.core.model.OAuth2AccessToken;
import org.jetbrains.annotations.Nullable;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MultivaluedMap;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.CLIENT_SESSION_ID;

@Service
public class AzureAuthenticationExtensionRequestAuthenticator implements RequestAuthenticator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureAuthenticationExtensionRequestAuthenticator.class);
    private final AuthorizationContext authorizationContext;
    private final UserInfoService userInfoService;
    private final AzureAuthenticationTokenStore tokenStore;
    private final AccountManagerService accountManagerService;
    private final AzureOAuthService azureOAuthService;
    private final SessionManager sessionManager;
    private final AccountProvider accountProvider;

    @Inject
    public AzureAuthenticationExtensionRequestAuthenticator(AuthorizationContext authorizationContext, UserInfoService userInfoService, AzureAuthenticationTokenStore tokenStore, AccountManagerService accountManagerService, AzureOAuthService azureOAuthService, SessionManager sessionManager, AccountProvider accountProvider) {
        this.authorizationContext = authorizationContext;
        this.userInfoService = userInfoService;
        this.tokenStore = tokenStore;
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
            LOGGER.info("path {}", path);
            if (Objects.equals("/callback", path)) {
                MultivaluedMap<String, String> queryParameters = httpRequest.getQueryParameters();
                List<String> code = queryParameters.get("code");
                LOGGER.info("Authorization Context: {}", authorizationContext.getAuthorizedAccount().getAccountId());
                return handleCallback(code.get(0));
            }
            String clientSessionId = getClientSessionId(httpRequest);
            if (!Objects.isNull(clientSessionId)) {
                String authenticatedAccountId = getAuthenticatedAccountId(clientSessionId);
                if (authenticatedAccountId != null) return authenticatedAccountId;
            }
            return null;
        } catch (Exception cause) {
            throw new IllegalStateException("Failed to authenticate : Reason: " + cause.getMessage(), cause);
        }
    }

    private @Nullable String getClientSessionId(HttpRequest httpRequest) {
        String cookie = httpRequest.getHeader(Constants.COOKIE);
        String clientSessionId = getCookie(cookie, CLIENT_SESSION_ID);
        if (Objects.isNull(clientSessionId)) {
            return getClientSessionIdFromHeaderOrQueryParams(httpRequest);
        }
        return clientSessionId;
    }

    private static String getClientSessionIdFromHeaderOrQueryParams(HttpRequest httpRequest) {
        String clientSessionId;
        clientSessionId = httpRequest.getHeader(CLIENT_SESSION_ID);
        LOGGER.info("Client Session Id from query param: {}", clientSessionId);
        if (Objects.isNull(clientSessionId)) {
            MultivaluedMap<String, String> queryParameters = httpRequest.getQueryParameters();
            if (!queryParameters.isEmpty()) {
                List<String> queryParamSessionId = queryParameters.get(CLIENT_SESSION_ID);
                clientSessionId = queryParamSessionId.isEmpty() ? null : queryParamSessionId.get(0);
            }
        }
        return clientSessionId;
    }

    private String getAuthenticatedAccountId(String clientSessionId) {
        String authenticatedAccountId = sessionManager.lookupAccountId(clientSessionId);
        if (!Objects.isNull(authenticatedAccountId)) {
            try {
                accountProvider.getAccount(authenticatedAccountId);
                LOGGER.info("Got Authenticated AccountId: {}", authenticatedAccountId);
                return authenticatedAccountId;
            } catch (NoSuchElementException cause) {
                LOGGER.error("No such account Id exist: {} with exception: {}", authenticatedAccountId, cause);
            }
        }
        LOGGER.info("No account found for clientSessionId: {}", clientSessionId);
        return null;
    }

    private String getCookie(String cookie, String key) {
        if (!Objects.isNull(cookie)) {
            String[] cookies = cookie.split(";");
            for (String c : cookies) {
                if (c.trim().startsWith(key)) {
                    LOGGER.debug("cookie :{}", c);
                    String[] keyValue = c.split("=");
                    return keyValue.length > 1 ? keyValue[1] : null;
                }
            }
        }
        return null;
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
        return new HttpResponse(302,
                Map.of(HttpHeaders.LOCATION, azureOAuthService.getAuthorizationURL(),
                        "Access-Control-Allow-Origin", "*"),
                new ByteArrayInputStream(new byte[0]));
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException e) {
        return new AuthorizationResponse(azureOAuthService.getAuthorizationURL(), List.of());
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException e, ProtoRequest protoRequest) {
        HttpRequest httpRequest = (HttpRequest) protoRequest;
        String routingUrl = httpRequest.getHeader("routingUrl");
        return new HttpResponse(302,
                Map.of(HttpHeaders.LOCATION, azureOAuthService.getAuthorizationURL() + "?caller=" + routingUrl + httpRequest.getUri().toString(),
                        "Access-Control-Allow-Origin", "*"),
                new ByteArrayInputStream(new byte[0]));
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException e) {
        return new AuthorizationResponse(azureOAuthService.getAuthorizationURL(), List.of());
    }

    public String handleCallback(String code) {
        LOGGER.debug("callback called");
        try {
            OAuth2AccessToken accessToken = azureOAuthService.getOAuth20Service().getAccessToken(code);
            String refreshToken = accessToken.getRefreshToken();
            LOGGER.debug("refreshToken= {}", refreshToken);
            CompletableFuture<String> accessTokenFuture = CompletableFuture.completedFuture(accessToken.getAccessToken());
            String email = userInfoService.getEmail(accessTokenFuture);
            tokenStore.saveRefreshToken(email, refreshToken);
            LOGGER.info("{} authenticated successfully", email);
            String accountIdByEmail = accountManagerService.getAccountIdByEmail(email);
            LOGGER.info("accountID {} ", accountIdByEmail);
            if (Objects.isNull(accountIdByEmail)) {
                accountIdByEmail = accountManagerService.createAccount(email);
                String sessionId = sessionManager.createSession(accountIdByEmail);
                tokenStore.saveSessionId(accountIdByEmail, sessionId);
            }
            return accountIdByEmail;
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(cause);
        } catch (IOException | ExecutionException cause) {
            throw new RuntimeException(cause);
        }
    }
}
