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
import app.krista.extensions.essentials.collaboration.azureauthentication.util.CommonUtils;
import app.krista.extensions.essentials.collaboration.azureauthentication.util.EmailAddresses;
import app.krista.ksdk.accounts.Account;
import app.krista.ksdk.authentication.AuthenticationSettings;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.List;

@Service
public class AccountManagerService {
    private final AccountProvisioner accountProvisioner;
    private final AuthenticationSettings authenticationSettings;

    @Inject
    public AccountManagerService(AccountProvisioner accountProvisioner, AuthenticationSettings authenticationSettings) {
        this.accountProvisioner = accountProvisioner;
        this.authenticationSettings = authenticationSettings;
    }

    public String createAccount(String email) {
        addDomainToWorkspaceIfNotPresent(email);
        Account account = accountProvisioner.provisionAccount(email);
        return account != null ? account.getAccountId() : null;
    }

    private void addDomainToWorkspaceIfNotPresent(String email) throws AuthorizationException {
        List<String> supportedDomainsForWorkspace = authenticationSettings.getSupportedDomains();
        System.out.println("Supported Domains : " + supportedDomainsForWorkspace.toString());
        String supportedDomain = String.join(",", supportedDomainsForWorkspace);
        String invokerDomains = EmailAddresses.getDomainName(email);
        CommonUtils.validateIfSupportedDomain(email, supportedDomain, invokerDomains);
        CommonUtils.addSupportedDomainsToWorkspace(email, supportedDomainsForWorkspace, authenticationSettings);
    }
}
