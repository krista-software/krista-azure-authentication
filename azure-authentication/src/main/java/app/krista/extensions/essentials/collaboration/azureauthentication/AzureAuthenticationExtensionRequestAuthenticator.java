package app.krista.extensions.essentials.collaboration.azureauthentication;

import app.krista.extension.authorization.MustAuthenticateException;
import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.request.ProtoRequest;
import app.krista.extension.request.ProtoResponse;
import app.krista.extension.request.protos.http.HttpRequest;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.AccountManagerService;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.UserInfoService;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureOAuthService;
import app.krista.extensions.essentials.collaboration.azureauthentication.store.AzureAuthenticationTokenStore;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedField;
import com.github.scribejava.core.model.OAuth2AccessToken;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class AzureAuthenticationExtensionRequestAuthenticator implements RequestAuthenticator {
    private static final Logger LOGGER = LoggerFactory.getLogger(AzureAuthenticationExtensionRequestAuthenticator.class);
    private final AuthorizationContext authorizationContext;
    private final UserInfoService userInfoService;
    private final AzureAuthenticationTokenStore tokenStore;
    private final AccountManagerService accountManagerService;
    private final AzureOAuthService azureOAuthService;

    @Inject
    public AzureAuthenticationExtensionRequestAuthenticator(AuthorizationContext authorizationContext, UserInfoService userInfoService, AzureAuthenticationTokenStore tokenStore, AccountManagerService accountManagerService, AzureOAuthService azureOAuthService) {
        this.authorizationContext = authorizationContext;
        this.userInfoService = userInfoService;
        this.tokenStore = tokenStore;
        this.accountManagerService = accountManagerService;
        this.azureOAuthService = azureOAuthService;
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
            if (Objects.equals("/login", path)) {
                LOGGER.info("Authorization Context: {}", authorizationContext.getAuthorizedAccount().getAccountId());
                return authorizationContext.getAuthorizedAccount().getAccountId();
            }
            if (Objects.equals("/callback", path)) {
                MultivaluedMap<String, String> queryParameters = httpRequest.getQueryParameters();
                List<String> code = queryParameters.get("code");
                LOGGER.info("Authorization Context: {}", authorizationContext.getAuthorizedAccount().getAccountId());
                return handleCallback(code.get(0));
            }
        } catch (Exception cause) {
            throw new IllegalStateException("Failed to authenticate : Reason: " + cause.getMessage(), cause);
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
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthenticateResponse(MustAuthenticateException e) {
        return null;
    }

    @Override
    public ProtoResponse getMustAuthorizeResponse(MustAuthorizeException e, ProtoRequest protoRequest) {
        return null;
    }

    @Override
    public AuthorizationResponse getMustAuthorizeResponse(MustAuthorizeException e) {
        return null;
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
            return accountIdByEmail != null ? accountIdByEmail : accountManagerService.createAccount(email);
        } catch (InterruptedException cause) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(cause);
        } catch (IOException | ExecutionException cause) {
            throw new RuntimeException(cause);
        }
    }
}
