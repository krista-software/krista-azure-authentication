package app.krista.extensions.essentials.collaboration.azureauthentication;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.*;
import app.krista.extension.request.RoutingInfo;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.UserInfoService;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureAuthenticationCredential;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureAuthenticationCredentialBuilder;
import app.krista.extensions.essentials.collaboration.azureauthentication.store.AzureAuthenticationTokenStore;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.context.AuthorizationContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.*;

@Domain(id = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7",
        name = "Authentication",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista",
        ecosystemVersion = "3e7e09ed-688f-41fa-ab7c-ff879e750011")
@Extension(implementingDomainIds = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7", jaxrsId = "authn", requireWorkspaceAdminRights = true)
@StaticResource(path = "docs", file = "docs")
@Field.Text(name = CLIENT_ID)
@Field.Text(name = Constants.CLIENT_SECRET, isSecured = true)
@Field.Text(name = Constants.TENANT_ID)
public class AzureAuthenticationExtension {
    private final Logger LOGGER = LoggerFactory.getLogger(AzureAuthenticationExtension.class);
    private final RequestAuthenticator requestAuthenticator;
    private final RoutingInfo routingInfo;
    private final AuthorizationContext authorizationContext;
    private final AzureAuthenticationTokenStore tokenStore;
    private final UserInfoService userInfoService;

    @Inject
    public AzureAuthenticationExtension(AzureAuthenticationExtensionRequestAuthenticator requestAuthenticator, Invoker invoker, AuthorizationContext authorizationContext, AzureAuthenticationTokenStore tokenStore, UserInfoService userInfoService) {
        this.requestAuthenticator = requestAuthenticator;
        this.routingInfo = invoker.getRoutingInfo();
        this.authorizationContext = authorizationContext;
        this.tokenStore = tokenStore;
        this.userInfoService = userInfoService;
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getAuthenticatedAccountId() {
        return requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateAttributes(Map<String, String> attributes) {
        String clientID = attributes.get(CLIENT_ID);
        String clientSecret = attributes.get(CLIENT_SECRET);
        String tenantId = attributes.get(TENANT_ID);
        Account authorizedAccount = authorizationContext.getAuthorizedAccount();
        String accessToken = tokenStore.getAccessToken(authorizedAccount.getPrimaryEmailAddress());
        if (Objects.isNull(accessToken)) {
            throw new MustAuthorizeException("Unauthorized user, please authorize yourself");
        }
        String email = userInfoService.getEmail(CompletableFuture.completedFuture(accessToken));
        LOGGER.info("user: {} authorized", email);
    }
}
