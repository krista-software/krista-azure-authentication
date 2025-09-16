package app.krista.extensions.essentials.collaboration.azureauthentication;

import app.krista.extension.authorization.MustAuthorizeException;
import app.krista.extension.authorization.RequestAuthenticator;
import app.krista.extension.executor.Invoker;
import app.krista.extension.impl.anno.*;
import app.krista.extension.request.ExecutionMode;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.AccountProvisioner;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.UserInfoService;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureOAuthService;
import app.krista.extensions.essentials.collaboration.azureauthentication.store.OauthSessionStore;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.context.AuthorizationContext;
import app.krista.model.field.NamedValuedField;
import com.nimbusds.oauth2.sdk.util.StringUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.*;

@Domain(id = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7",
        name = "Authentication",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista",
        ecosystemVersion = "3e7e09ed-688f-41fa-ab7c-ff879e750011")
@Extension(implementingDomainIds = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7", jaxrsId = "authn", requireWorkspaceAdminRights = true, version = "1.0.3",name = "Azure Authentication", supportingModes = ExecutionMode.LIVE)
@Field.Text(name = AzureAuthenticationExtension.DEFAULT_ROLE_KEY, required = false)
@Field.Text(name = CLIENT_ID)
@Field.Text(name = Constants.CLIENT_SECRET, isSecured = true)
@Field.Text(name = Constants.TENANT_ID)
@StaticResource(path = "docs", file = "docs")
@Java(version = Java.Version.JAVA_21)
public class AzureAuthenticationExtension {
    public static final String DEFAULT_ROLE_KEY = "default_role";
    private final Logger logger = LoggerFactory.getLogger(AzureAuthenticationExtension.class);
    private final RequestAuthenticator requestAuthenticator;
    private final Invoker invoker;
    private final AuthorizationContext authorizationContext;
    private final OauthSessionStore oauthSessionStore;
    private final UserInfoService userInfoService;
    private final AzureOAuthService azureOAuthService;

    @Inject
    public AzureAuthenticationExtension(AzureAuthenticationExtensionRequestAuthenticator requestAuthenticator, Invoker invoker, AuthorizationContext authorizationContext, OauthSessionStore oauthSessionStore, UserInfoService userInfoService, AzureOAuthService azureOAuthService) {
        this.requestAuthenticator = requestAuthenticator;
        this.invoker = invoker;
        this.authorizationContext = authorizationContext;
        this.oauthSessionStore = oauthSessionStore;
        this.userInfoService = userInfoService;
        this.azureOAuthService = azureOAuthService;
    }

    @InvokerRequest(InvokerRequest.Type.AUTHENTICATOR)
    public RequestAuthenticator getAuthenticatedAccountId() {
        logger.info("RequestAuthenticator called");
        return requestAuthenticator;
    }

    @InvokerRequest(InvokerRequest.Type.VALIDATE_ATTRIBUTES)
    public void validateAttributes(Map<String, String> attributes) {
        String defaultRole = attributes.get(DEFAULT_ROLE_KEY);

        if (StringUtils.isBlank(defaultRole)) {
            System.out.println("#####-ADDING ROLE TO ATTRIBUTES...");
            attributes.put(DEFAULT_ROLE_KEY, AccountProvisioner.DEFAULT_ROLE_FOR_NEW_ACCOUNT);
        }

        if (!authorizationContext.isWorkspaceAdmin() && isSetupRoleChanged(defaultRole)) {
            logger.info("User is not workspace admin, skipping validation on role change.");
            return;
        }

        String refreshToken = getRefreshToken(attributes);
        if (Objects.isNull(refreshToken)) {
            throwMustAuthorizationException(attributes);
        }

        logger.info("Processed After Auth");
        String accessToken = getAccessToken(refreshToken);

        logger.info("Access Token Received");
        String email = userInfoService.getEmail(CompletableFuture.completedFuture(accessToken));
        logger.info("user: {} authorized", email);
    }

    @Nullable
    private String getRefreshToken(Map<String, String> attributes) {
        Account authorizedAccount = authorizationContext.getAuthorizedAccount();
        logger.info("Who Is Authorized Account Holder : {}", authorizedAccount.getPrimaryEmailAddress());
        logger.info("Is workSpace Admin : {}", authorizationContext.isWorkspaceAdmin());
        return !areAttributesUpdated(attributes) ? oauthSessionStore.getRefreshToken(authorizedAccount.getPrimaryEmailAddress()) : null;
    }

    private void throwMustAuthorizationException(Map<String, String> attributes) {

        String clientId = attributes.get(CLIENT_ID);
        String clientSecret = attributes.get(CLIENT_SECRET);
        String tenantId = attributes.get(TENANT_ID);
        String defaultRole = attributes.get(DEFAULT_ROLE_KEY);

        logger.info("Refresh Token is Null");
        NamedValuedField clientIdField = new NamedValuedField(CLIENT_ID, TEXT_FIELD_TYPE, clientId, Map.of(), Map.of());
        NamedValuedField clientSecretField = new NamedValuedField(CLIENT_SECRET, TEXT_FIELD_TYPE, clientSecret, Map.of(), Map.of());
        NamedValuedField tenantIdField = new NamedValuedField(TENANT_ID, TEXT_FIELD_TYPE, tenantId, Map.of(), Map.of());
        String authContextId = getAuthContextId(clientId, clientSecret, tenantId, defaultRole);
        logger.info("Auth contextID : {}", authContextId);
        NamedValuedField authContextField = new NamedValuedField(AUTH_CONTEXT_ID, TEXT_FIELD_TYPE, authContextId, Map.of(), Map.of());

        throw new MustAuthorizeException("Unauthorized user, please authorize yourself", List.of(clientIdField, clientSecretField, tenantIdField, authContextField));
    }

    private boolean isSetupRoleChanged(String incomingDefaultRole) {
        if (incomingDefaultRole == null || incomingDefaultRole.isBlank()) {
            return false;
        }
        Map<String, Object> attributes = invoker.getAttributes();
        String roleStoredInInvoker = (String) attributes.getOrDefault(DEFAULT_ROLE_KEY, "");

        if (!roleStoredInInvoker.isBlank() && roleStoredInInvoker.equals(incomingDefaultRole)) {
            return false;
        }
        return true;
    }

    private String getAccessToken(String refreshToken) {
        return azureOAuthService.updateTokens(refreshToken);
    }


    private String getAuthContextId(String clientId, String clientSecret, String tenantId, String defaultRole) {
        String authContextId;
        authContextId = oauthSessionStore.storeAuthCredentials(Map.of(
                CLIENT_ID, clientId,
                CLIENT_SECRET, clientSecret,
                TENANT_ID, tenantId,
                DEFAULT_ROLE_KEY, defaultRole
        ));
        return authContextId;
    }

    private boolean areAttributesUpdated(Map<String, String> attributes) {
        try {
            Map<String, String> invokerDetails = oauthSessionStore.getInvokerAttributes();
            return !invokerDetails.equals(attributes);
        } catch (RuntimeException e) {
            logger.error("Invoker Credential Details are not Available: {}", e.getMessage(), e);
            return true;
        }
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
