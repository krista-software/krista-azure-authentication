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

package app.krista.extensions.essentials.collaboration.azureauthentication.service.auth;


import java.util.Objects;
import java.util.Set;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.DEFAULT_CALLBACK_PATH;

public class AzureAuthenticationCredential {
    private final OAuthCredential oAuthCredential;
    private final Set<String> scopes;
    private final String routingUrl;

    public AzureAuthenticationCredential(OAuthCredential oAuthCredential, Set<String> scopes, String routingUrl) {
        this.oAuthCredential = oAuthCredential;
        this.scopes = scopes;
        this.routingUrl = routingUrl;
    }

    public String getClientId() {
        return oAuthCredential.getClientId();
    }

    public String getClientSecret() {
        return oAuthCredential.getClientSecret();
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public String getAuthority(String... tenantId) {
        String baseAuthorityUrl = "https://login.microsoftonline.com/";
        if (Objects.isNull(tenantId) || tenantId.length == 0) {
            return baseAuthorityUrl + this.oAuthCredential.getTenantId();
        }
        return baseAuthorityUrl + tenantId[0];
    }

    public String getRoutingUrl() {
        return routingUrl;
    }

    public String getRedirectUrl() {
        return routingUrl.replaceAll("/extension/api/.*", DEFAULT_CALLBACK_PATH);
    }
}
