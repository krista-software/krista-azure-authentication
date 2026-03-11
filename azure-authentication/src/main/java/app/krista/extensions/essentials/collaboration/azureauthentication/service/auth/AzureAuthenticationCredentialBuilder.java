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

import app.krista.extensions.essentials.collaboration.azureauthentication.exception.FileOperationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

import static app.krista.extensions.essentials.collaboration.azureauthentication.util.Constants.*;
import static app.krista.extensions.essentials.collaboration.azureauthentication.util.ErrorMessages.*;

public class AzureAuthenticationCredentialBuilder {
    private String routingUrl;
    private final Set<String> scopes = new HashSet<>();

    public AzureAuthenticationCredentialBuilder routingUrl(String routingUrl) {
        this.routingUrl = routingUrl;
        return this;
    }

    public AzureAuthenticationCredential buildCredentialFromProperties() {
        Properties properties = new Properties();
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream("credentials.properties")) {
            properties.load(inputStream);
            String cId = properties.getProperty("clientId");
            String cSecret = properties.getProperty("clientSecret");
            String tId = properties.getProperty("tenantId");
            String scope = properties.getProperty("scope");
            Set<String> scps = Arrays.stream(scope.split(BLANK_SPACE)).collect(Collectors.toSet());
            return new AzureAuthenticationCredential(new OAuthCredentialImpl(cId, cSecret, tId), scps, routingUrl);
        } catch (IOException cause) {
            throw new FileOperationException(CONFIGURATION_FILE_LOAD_FAILED, cause);
        }
    }

    public AzureAuthenticationCredential build(Map<String, String> attributes) {
        String clientIdAttribute = attributes.get(CLIENT_ID);
        String clientSecretAttribute = attributes.get(CLIENT_SECRET);
        String tenantIdAttribute = attributes.get(TENANT_ID);
        scopes.add("User.Read");
        return new AzureAuthenticationCredential(new OAuthCredentialImpl(clientIdAttribute, clientSecretAttribute, tenantIdAttribute), scopes, routingUrl);
    }
}
