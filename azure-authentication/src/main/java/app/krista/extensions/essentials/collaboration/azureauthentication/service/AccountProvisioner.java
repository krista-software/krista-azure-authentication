package app.krista.extensions.essentials.collaboration.azureauthentication.service;

import app.krista.extension.authorization.AuthorizationException;
import app.krista.extension.executor.Invoker;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.EmailAddresses;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.accounts.AccountManager;
import app.krista.ksdk.accounts.ModifiableAccount;
import app.krista.ksdk.authorization.ModifiableRole;
import app.krista.ksdk.authorization.Role;
import app.krista.ksdk.authorization.RoleManager;
import app.krista.omni.sdk.spi.impl.services.AccountService;
import app.krista.omni.sdk.spi.impl.util.Strings;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Create or fetch workspace account with given IdentificationToken.
 */
@Service
public final class AccountProvisioner {
    public static final String DEFAULT_ROLE_FOR_NEW_ACCOUNT = "Krista Guest User";
    public static final String DEFAULT_ROLE_KEY = "default_role";

    private final AccountManager accountManager;
    private final RoleManager roleManager;
    private final Invoker invoker;

    @Inject
    public AccountProvisioner(AccountManager accountManager, RoleManager roleManager, Invoker invoker) {
        this.accountManager = accountManager;
        this.roleManager = roleManager;
        this.invoker = invoker;

    }

    public Account provisionAccount(String emailAddress) {
        String assignedRole = createRoleIfNotPresent();
        ModifiableAccount modifiableAccount = accountManager.lookupAccount(emailAddress);
        List<String> roleNames = ensureHasAllRoles(modifiableAccount, assignedRole);
        if (modifiableAccount == null) {
            modifiableAccount = accountManager.createAccount(EmailAddresses.getLocalPart(emailAddress),
                    EmailAddresses.normalizeEmailAddress(emailAddress),
                    new LinkedHashSet<>(roleNames),
                    emailAddress.endsWith(Strings.AT + AccountService.SUPPORTED_DOMAIN) ? Map.of("ORG", "KristaSoft",
                            "KRISTA_SOURCE", "Omni Chatbot",
                            "KRISTA_LAST_LOGIN", getCurrentDateTime(),
                            "SOURCE", "OMNI") : Map.of());
        }
        return modifiableAccount;
    }

    private List<String> ensureHasAllRoles(ModifiableAccount modifiableAccount, String roles)
            throws AuthorizationException {
        List<String> allRoles = new ArrayList<>();
        Iterable<ModifiableRole> workspaceRoles = roleManager.getRoles();
        boolean roleExit = false;
        for (ModifiableRole workspaceRole : workspaceRoles) {
            if (Objects.equals(roles, workspaceRole.getName())) {
                allRoles.add(workspaceRole.getRoleId());
                roleExit = true;
                break;
            }
        }
        if (!roleExit) {
            ModifiableRole role = roleManager.createRole(roles);
            allRoles.add(role.getRoleId());
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
        Object defaultRoleObject = invoker.getAttributes().get(DEFAULT_ROLE_KEY);
        if (defaultRoleObject instanceof String && !((String) defaultRoleObject).trim().isEmpty()) {
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