package app.krista.extensions.essentials.collaboration.azureauthentication.service;

import app.krista.extension.authorization.AuthorizationException;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.CommonUtils;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.EmailAddresses;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.accounts.AccountManager;
import app.krista.ksdk.accounts.ModifiableAccount;
import app.krista.ksdk.authentication.AuthenticationSettings;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class AccountManagerService {
    private final AccountProvisioner accountProvisioner;
    private final AccountManager accountManager;
    private final AuthenticationSettings authenticationSettings;

    @Inject
    public AccountManagerService(AccountProvisioner accountProvisioner, AccountManager accountManager, AuthenticationSettings authenticationSettings) {
        this.accountProvisioner = accountProvisioner;
        this.accountManager = accountManager;
        this.authenticationSettings = authenticationSettings;
    }

    public String getAccountIdByEmail(String email) {
        ModifiableAccount modifiableAccount = accountManager.lookupAccount(email);
        return modifiableAccount != null ? modifiableAccount.getAccountId() : null;
    }

    public String createAccount(String email) {
        addDomainToWorkspaceIfNotPresent(email);
        Account account = accountProvisioner.provisionAccount(email);
        return account != null ? account.getAccountId() : null;
    }

    private void addDomainToWorkspaceIfNotPresent(String email) throws AuthorizationException {
        List<String> supportedDomainsForWorkspace = authenticationSettings.getSupportedDomains();// List.of("omni.com", "krista.ai", "kristasoft.com");
        System.out.println("Supported Domains : "+ supportedDomainsForWorkspace.toString());
        String supportedDomain = String.join(",", supportedDomainsForWorkspace);
        String invokerDomains = EmailAddresses.getDomainName(email);
        CommonUtils.validateIfSupportedDomain(email, supportedDomain, invokerDomains);
        CommonUtils.addSupportedDomainsToWorkspace(email, supportedDomainsForWorkspace, authenticationSettings);
    }
}
