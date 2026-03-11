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

package app.krista.extensions.essentials.collaboration.azureauthentication;

import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureAuthenticationCredential;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureAuthenticationCredentialBuilder;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureAuthorizationUrlBuilder;
import com.microsoft.aad.msal4j.*;
import org.junit.Assert;
import org.junit.Test;

import java.net.MalformedURLException;
import java.net.URL;

public class AzureAuthenticationExtensionTest {
    @Test
    public void getAuthorizationUrl() throws MalformedURLException {
        AzureAuthenticationCredential azureAuthenticationCredential = new AzureAuthenticationCredentialBuilder()
                .routingUrl("http://localhost:8765/extension/api/FwoDdIazmBVeU4DRo0F_sdQ_e_e").buildCredentialFromProperties();
        IClientCredential credential = ClientCredentialFactory.createFromSecret(azureAuthenticationCredential.getClientSecret());
        ConfidentialClientApplication build = ConfidentialClientApplication
                .builder(azureAuthenticationCredential.getClientId(), credential)
                .authority(azureAuthenticationCredential.getAuthority())
                .build();
        AzureAuthorizationUrlBuilder azureAuthorizationUrlBuilder=new AzureAuthorizationUrlBuilder(azureAuthenticationCredential,build);
        URL authorizationRequestUrl = azureAuthorizationUrlBuilder.build();
        System.out.println("url:" + authorizationRequestUrl.toString());
        Assert.assertNotNull(authorizationRequestUrl);
    }
}
