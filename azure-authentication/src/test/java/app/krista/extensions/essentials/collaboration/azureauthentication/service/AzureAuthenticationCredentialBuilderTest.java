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
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureAuthenticationCredentialBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class AzureAuthenticationCredentialBuilderTest {

    private AzureAuthenticationCredentialBuilder builder;
    private String routingUrl;

    @Before
    public void setUp() {
        builder = new AzureAuthenticationCredentialBuilder();
        routingUrl = "http://localhost:8765/extension/api/testId";
    }

    @Test
    public void testRoutingUrl() {
        AzureAuthenticationCredentialBuilder result = builder.routingUrl(routingUrl);
        assertNotNull(result);
        assertSame(builder, result); // Should return same instance for chaining
    }

    @Test
    public void testBuildWithValidAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("clientId", "test-client-id");
        attributes.put("clientSecret", "test-client-secret");
        attributes.put("tenantId", "test-tenant-id");

        builder.routingUrl(routingUrl);
        AzureAuthenticationCredential credential = builder.build(attributes);

        assertNotNull(credential);
        assertEquals("test-client-id", credential.getClientId());
        assertEquals("test-client-secret", credential.getClientSecret());
        assertEquals(routingUrl, credential.getRoutingUrl());
    }

    @Test
    public void testBuildAddsUserReadScope() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("clientId", "client-id");
        attributes.put("clientSecret", "client-secret");
        attributes.put("tenantId", "tenant-id");

        builder.routingUrl(routingUrl);
        AzureAuthenticationCredential credential = builder.build(attributes);

        assertNotNull(credential.getScopes());
        assertTrue(credential.getScopes().contains("User.Read"));
    }

    @Test
    public void testBuildWithEmptyAttributes() {
        Map<String, String> attributes = new HashMap<>();

        builder.routingUrl(routingUrl);
        AzureAuthenticationCredential credential = builder.build(attributes);

        assertNotNull(credential);
        assertNull(credential.getClientId());
        assertNull(credential.getClientSecret());
    }

    @Test
    public void testBuildWithPartialAttributes() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("clientId", "only-client-id");

        builder.routingUrl(routingUrl);
        AzureAuthenticationCredential credential = builder.build(attributes);

        assertNotNull(credential);
        assertEquals("only-client-id", credential.getClientId());
        assertNull(credential.getClientSecret());
    }

    @Test
    public void testBuildGeneratesCorrectAuthority() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("clientId", "client-id");
        attributes.put("clientSecret", "client-secret");
        attributes.put("tenantId", "my-tenant-id");

        builder.routingUrl(routingUrl);
        AzureAuthenticationCredential credential = builder.build(attributes);

        String authority = credential.getAuthority();
        assertEquals("https://login.microsoftonline.com/my-tenant-id", authority);
    }

    @Test
    public void testBuildGeneratesCorrectRedirectUrl() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("clientId", "client-id");
        attributes.put("clientSecret", "client-secret");
        attributes.put("tenantId", "tenant-id");

        String customRoutingUrl = "https://example.com/extension/api/abc123";
        builder.routingUrl(customRoutingUrl);
        AzureAuthenticationCredential credential = builder.build(attributes);

        String redirectUrl = credential.getRedirectUrl();
        assertEquals("https://example.com/extension/api/authn/v3/oauth/callback", redirectUrl);
    }

    @Test
    public void testMultipleBuildCallsWithSameBuilder() {
        Map<String, String> attributes1 = new HashMap<>();
        attributes1.put("clientId", "client-1");
        attributes1.put("clientSecret", "secret-1");
        attributes1.put("tenantId", "tenant-1");

        builder.routingUrl(routingUrl);
        AzureAuthenticationCredential credential1 = builder.build(attributes1);

        Map<String, String> attributes2 = new HashMap<>();
        attributes2.put("clientId", "client-2");
        attributes2.put("clientSecret", "secret-2");
        attributes2.put("tenantId", "tenant-2");

        AzureAuthenticationCredential credential2 = builder.build(attributes2);

        // Both credentials should be created successfully
        assertNotNull(credential1);
        assertNotNull(credential2);
        assertEquals("client-1", credential1.getClientId());
        assertEquals("client-2", credential2.getClientId());
    }

    @Test
    public void testRoutingUrlChaining() {
        String url1 = "http://localhost:8080/extension/api/id1";
        String url2 = "http://localhost:9090/extension/api/id2";

        AzureAuthenticationCredentialBuilder result = builder.routingUrl(url1).routingUrl(url2);

        Map<String, String> attributes = new HashMap<>();
        attributes.put("clientId", "client-id");
        attributes.put("clientSecret", "client-secret");
        attributes.put("tenantId", "tenant-id");

        AzureAuthenticationCredential credential = result.build(attributes);

        // Should use the last routing URL set
        assertEquals(url2, credential.getRoutingUrl());
    }

    @Test
    public void testBuildWithNullRoutingUrl() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("clientId", "client-id");
        attributes.put("clientSecret", "client-secret");
        attributes.put("tenantId", "tenant-id");

        // Don't set routing URL
        AzureAuthenticationCredential credential = builder.build(attributes);

        assertNotNull(credential);
        assertNull(credential.getRoutingUrl());
    }

    @Test
    public void testBuildScopesAreIndependent() {
        Map<String, String> attributes = new HashMap<>();
        attributes.put("clientId", "client-id");
        attributes.put("clientSecret", "client-secret");
        attributes.put("tenantId", "tenant-id");

        builder.routingUrl(routingUrl);
        AzureAuthenticationCredential credential1 = builder.build(attributes);
        AzureAuthenticationCredential credential2 = builder.build(attributes);

        // Scopes should contain User.Read for both
        assertTrue(credential1.getScopes().contains("User.Read"));
        assertTrue(credential2.getScopes().contains("User.Read"));
    }
}

