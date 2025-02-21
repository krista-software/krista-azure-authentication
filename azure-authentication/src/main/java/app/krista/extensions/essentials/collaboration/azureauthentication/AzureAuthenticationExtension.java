package app.krista.extensions.essentials.collaboration.azureauthentication;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.*;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.UserInfoService;
import app.krista.extensions.essentials.collaboration.azureauthentication.store.OauthSessionStore;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedValuedField;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.*;

@Domain(id = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7",
        name = "Authentication",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista",
        ecosystemVersion = "3e7e09ed-688f-41fa-ab7c-ff879e750011")
@Extension(implementingDomainIds = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7", jaxrsId = "authn", requireWorkspaceAdminRights = true, version = "1.0.0-rc3")
@Field.Text(name = AzureAuthenticationExtension.DEFAULT_ROLE_KEY, required = false)
@Field.Text(name = CLIENT_ID)
@Field.Text(name = Constants.CLIENT_SECRET, isSecured = true)
@Field.Text(name = Constants.TENANT_ID)
@StaticResource(path = "docs", file = "docs")
public class AzureAuthenticationExtension {
    public static final String DEFAULT_ROLE_KEY = "default_role";
    private final Logger logger = LoggerFactory.getLogger(AzureAuthenticationExtension.class);
    private final RequestAuthenticator requestAuthenticator;
    private final Invoker invoker;
    private final AuthorizationContext authorizationContext;
    private final OauthSessionStore oauthSessionStore;
    private final UserInfoService userInfoService;

    @Inject
    public AzureAuthenticationExtension(AzureAuthenticationExtensionRequestAuthenticator requestAuthenticator, Invoker invoker, AuthorizationContext authorizationContext, OauthSessionStore oauthSessionStore, UserInfoService userInfoService) {
        this.requestAuthenticator = requestAuthenticator;
        this.invoker = invoker;
        this.authorizationContext = authorizationContext;
        this.oauthSessionStore = oauthSessionStore;
        this.userInfoService = userInfoService;
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getAuthenticatedAccountId() {
        return requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateAttributes(Map<String, String> attributes) {
        if (Objects.isNull(invoker.getAttributes()) || invoker.getAttributes().isEmpty()) {
            return;
        }
        String clientId = attributes.get(CLIENT_ID);
        String clientSecret = attributes.get(CLIENT_SECRET);
        String tenantId = attributes.get(TENANT_ID);
        Account authorizedAccount = authorizationContext.getAuthorizedAccount();
        String accessToken = oauthSessionStore.getAccessToken(authorizedAccount.getPrimaryEmailAddress());
        oauthSessionStore.saveInvokerAttributes(attributes);
        if (Objects.isNull(accessToken)) {
            NamedValuedField clientIdField = new NamedValuedField(CLIENT_ID, "Text", clientId, Map.of(), Map.of());
            NamedValuedField clientSecretField = new NamedValuedField(CLIENT_SECRET, "Text", clientSecret, Map.of(), Map.of());
            NamedValuedField tenantIdField = new NamedValuedField(TENANT_ID, "Text", tenantId, Map.of(), Map.of());
            throw new MustAuthorizeException("Unauthorized user, please authorize yourself", List.of(clientIdField, clientSecretField, tenantIdField));
        }
        String email = userInfoService.getEmail(CompletableFuture.completedFuture(accessToken));
        logger.info("user: {} authorized", email);
    }

    @InvokerRequest(InvokerRequest.Type.TEST_CONNECTION)
    public void testConnection() {
        if (Objects.isNull(invoker.getAttributes()) || invoker.getAttributes().isEmpty()) {
            return;
        }
        Map<String, String> attributesMap = invoker
                .getAttributes()
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, entry -> (String) entry.getValue()));
        validateAttributes(attributesMap);
    }

    @InvokerRequest(InvokerRequest.Type.CUSTOM_TABS)
    public Map<String, String> customTab() {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Documentation", "static/docs");
        return map;
    }
}
