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

import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureAuthenticationCredential;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.OAuthCredential;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.OAuthCredentialImpl;
import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class AzureAuthenticationCredentialTest {

    private OAuthCredential oAuthCredential;
    private Set<String> scopes;
    private String routingUrl;
    private AzureAuthenticationCredential credential;

    @Before
    public void setUp() {
        oAuthCredential = new OAuthCredentialImpl("test-client-id", "test-client-secret", "test-tenant-id");
        scopes = new HashSet<>();
        scopes.add("User.Read");
        scopes.add("Mail.Read");
        routingUrl = "http://localhost:8765/extension/api/FwoDdIazmBVeU4DRo0F_sdQ_e_e";
        credential = new AzureAuthenticationCredential(oAuthCredential, scopes, routingUrl);
    }

    @Test
    public void testGetClientId() {
        assertEquals("test-client-id", credential.getClientId());
    }

    @Test
    public void testGetClientSecret() {
        assertEquals("test-client-secret", credential.getClientSecret());
    }

    @Test
    public void testGetScopes() {
        Set<String> result = credential.getScopes();
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.contains("User.Read"));
        assertTrue(result.contains("Mail.Read"));
    }

    @Test
    public void testGetAuthorityWithoutTenantId() {
        String authority = credential.getAuthority();
        assertNotNull(authority);
        assertEquals("https://login.microsoftonline.com/test-tenant-id", authority);
    }

    @Test
    public void testGetAuthorityWithTenantId() {
        String authority = credential.getAuthority("custom-tenant-id");
        assertNotNull(authority);
        assertEquals("https://login.microsoftonline.com/custom-tenant-id", authority);
    }

    @Test
    public void testGetAuthorityWithEmptyArray() {
        String authority = credential.getAuthority(new String[]{});
        assertNotNull(authority);
        assertEquals("https://login.microsoftonline.com/test-tenant-id", authority);
    }

    @Test
    public void testGetRoutingUrl() {
        assertEquals(routingUrl, credential.getRoutingUrl());
    }

    @Test
    public void testGetRedirectUrl() {
        String redirectUrl = credential.getRedirectUrl();
        assertNotNull(redirectUrl);
        assertEquals("http://localhost:8765/extension/api/authn/v3/oauth/callback", redirectUrl);
    }

    @Test
    public void testGetRedirectUrlWithDifferentRoutingUrl() {
        String customRoutingUrl = "https://example.com/extension/api/someId123";
        AzureAuthenticationCredential customCredential = new AzureAuthenticationCredential(
                oAuthCredential, scopes, customRoutingUrl);
        String redirectUrl = customCredential.getRedirectUrl();
        assertEquals("https://example.com/extension/api/authn/v3/oauth/callback", redirectUrl);
    }

    @Test
    public void testConstructorWithEmptyScopes() {
        Set<String> emptyScopes = new HashSet<>();
        AzureAuthenticationCredential emptyCredential = new AzureAuthenticationCredential(
                oAuthCredential, emptyScopes, routingUrl);
        assertNotNull(emptyCredential.getScopes());
        assertEquals(0, emptyCredential.getScopes().size());
    }

    @Test
    public void testConstructorWithSingleScope() {
        Set<String> singleScope = new HashSet<>();
        singleScope.add("User.Read");
        AzureAuthenticationCredential singleCredential = new AzureAuthenticationCredential(
                oAuthCredential, singleScope, routingUrl);
        assertEquals(1, singleCredential.getScopes().size());
        assertTrue(singleCredential.getScopes().contains("User.Read"));
    }

    @Test
    public void testGetAuthorityWithMultipleTenantIds() {
        String authority = credential.getAuthority("tenant1", "tenant2");
        assertEquals("https://login.microsoftonline.com/tenant1", authority);
    }

    @Test
    public void testGetRedirectUrlPreservesProtocol() {
        String httpsRoutingUrl = "https://secure.example.com/extension/api/abc123";
        AzureAuthenticationCredential httpsCredential = new AzureAuthenticationCredential(
                oAuthCredential, scopes, httpsRoutingUrl);
        String redirectUrl = httpsCredential.getRedirectUrl();
        assertTrue(redirectUrl.startsWith("https://"));
        assertEquals("https://secure.example.com/extension/api/authn/v3/oauth/callback", redirectUrl);
    }

    @Test
    public void testGetRedirectUrlWithComplexPath() {
        String complexRoutingUrl = "http://localhost:8080/extension/api/very/long/path/id";
        AzureAuthenticationCredential complexCredential = new AzureAuthenticationCredential(
                oAuthCredential, scopes, complexRoutingUrl);
        String redirectUrl = complexCredential.getRedirectUrl();
        assertEquals("http://localhost:8080/extension/api/authn/v3/oauth/callback", redirectUrl);
    }
}

