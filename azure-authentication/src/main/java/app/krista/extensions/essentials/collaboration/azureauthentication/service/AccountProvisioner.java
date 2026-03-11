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

package app.krista.extensions.essentials.collaboration.azureauthentication.service;

import app.krista.extension.authorization.AuthorizationException;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.azureauthentication.store.OauthSessionStore;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.EmailAddresses;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.accounts.AccountManager;
import app.krista.ksdk.accounts.AttributeManager;
import app.krista.ksdk.accounts.ModifiableAccount;
import app.krista.ksdk.authorization.ModifiableRole;
import app.krista.ksdk.authorization.Role;
import app.krista.ksdk.authorization.RoleManager;
import app.krista.model.field.Field;
import app.krista.omni.sdk.spi.impl.services.AccountService;
import app.krista.omni.sdk.spi.impl.util.Strings;
import org.jetbrains.annotations.NotNull;
import org.jvnet.hk2.annotations.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import static app.krista.extensions.essentials.collaboration.azureauthentication.AzureAuthenticationExtension.DEFAULT_ROLE_KEY;

/**
 * Create or fetch workspace account with given IdentificationToken.
 */
@Service
public final class AccountProvisioner {
    public static final String DEFAULT_ROLE_FOR_NEW_ACCOUNT = "Krista Guest User";
    public static final String ORG = "ORG";
    public static final String KRISTA_SOURCE = "KRISTA_SOURCE";
    public static final String KRISTA_LAST_LOGIN = "KRISTA_LAST_LOGIN";
    public static final String SOURCE = "SOURCE";


    private final Logger logger = LoggerFactory.getLogger(AccountProvisioner.class);
    private final AccountManager accountManager;
    private final RoleManager roleManager;
    private final Invoker invoker;
    private final AttributeManager attributeManager;
    private final OauthSessionStore oauthSessionStore;

    @Inject
    public AccountProvisioner(AccountManager accountManager, RoleManager roleManager, Invoker invoker, AttributeManager attributeManager, OauthSessionStore oauthSessionStore) {
        this.accountManager = accountManager;
        this.roleManager = roleManager;
        this.invoker = invoker;
        this.attributeManager = attributeManager;
        this.oauthSessionStore = oauthSessionStore;
    }

    public Account provisionAccount(String emailAddress) {
        String roleName = createRoleIfNotPresent();

        ModifiableAccount modifiableAccount = accountManager.lookupAccount(emailAddress);
        List<String> roleNames = ensureHasAllRoles(modifiableAccount, roleName);
        if (modifiableAccount == null) {
            modifiableAccount = accountManager.createAccount(EmailAddresses.getLocalPart(emailAddress),
                    EmailAddresses.normalizeEmailAddress(emailAddress),
                    new LinkedHashSet<>(roleNames),
                    getAttributes(emailAddress));
        }
        return modifiableAccount;
    }

    @NotNull
    private Map<String, Object> getAttributes(String emailAddress) {
        createAttributesIfNotExist();

        return emailAddress.endsWith(Strings.AT + AccountService.SUPPORTED_DOMAIN)
                ? Map.of(ORG, "KristaSoft",
                KRISTA_SOURCE, "Omni Chatbot",
                KRISTA_LAST_LOGIN, getCurrentDateTime(),
                SOURCE, "OMNI")
                :
                Map.of(ORG, "KristaSoft",
                        KRISTA_SOURCE, "EXTENSION_AZURE_AUTHENTICATION");
    }

    private void createAttributesIfNotExist() {
        if (verifyAttributeExists(ORG)) {
            createAttribute(ORG);
        }
        if (verifyAttributeExists(KRISTA_SOURCE)) {
            createAttribute(KRISTA_SOURCE);
        }
        if (verifyAttributeExists(KRISTA_LAST_LOGIN)) {
            createAttribute(KRISTA_LAST_LOGIN);
        }
        if (verifyAttributeExists(SOURCE)) {
            createAttribute(SOURCE);
        }
    }

    private boolean verifyAttributeExists(String attribute) {
        try {
            return attributeManager.lookupAttribute(attribute) == null;
        } catch (RuntimeException cause) {
            logger.error("Attribute Does not Exists : Attribute Name  {}   : {}", attribute, cause.getMessage(), cause);
        }
        return true;
    }

    private void createAttribute(String attributeName) {
        attributeManager.createAttribute(attributeName, new Field("Text", Map.of(), Map.of()));
    }

    private List<String> ensureHasAllRoles(ModifiableAccount modifiableAccount, String roleName)
            throws AuthorizationException {
        List<String> allRoles = new ArrayList<>();
        ModifiableRole workspaceRole = roleManager.lookupRole(roleName);
        if (workspaceRole != null) {
            allRoles.add(workspaceRole.getRoleId());
        }
        if (modifiableAccount != null) {
            for (Role role : modifiableAccount.getRoles()) {
                allRoles.add(role.getRoleId());
            }
            modifiableAccount.addRole(allRoles.toArray(String[]::new));
        }
        return allRoles;
    }

    private String createRoleIfNotPresent() throws AuthorizationException {
        String defaultRole = getDefaultRole();
        boolean isRolePresent = isDefaultRolePresentInWorkspace(defaultRole);
        if (!isRolePresent) {
            roleManager.createRole(defaultRole);
        }
        return defaultRole;
    }

    private boolean isDefaultRolePresentInWorkspace(String defaultRole) throws AuthorizationException {
        Iterable<ModifiableRole> roles = roleManager.getRoles();
        for (ModifiableRole role : roles) {
            if (Objects.equals(defaultRole, role.getName())) {
                return true;
            }
        }
        return false;
    }

    private String getDefaultRole() {
        String defaultRole = DEFAULT_ROLE_FOR_NEW_ACCOUNT;

        Map<String, String> invokerAttributes = oauthSessionStore.getInvokerAttributes();
        String defaultRoleObject = invokerAttributes.getOrDefault(DEFAULT_ROLE_KEY, (String) invoker.getAttributes().get(DEFAULT_ROLE_KEY));
        if (defaultRoleObject != null && !((String) defaultRoleObject).trim().isEmpty()) {
            defaultRole = (String) defaultRoleObject;
        }
        return defaultRole;
    }

    private String getCurrentDateTime() {
        DateFormat dateFormat = new SimpleDateFormat(AccountService.DATE_FORMAT);
        dateFormat.setTimeZone(TimeZone.getTimeZone(AccountService.GMT));
        return dateFormat.format(Calendar.getInstance().getTime());
    }

}