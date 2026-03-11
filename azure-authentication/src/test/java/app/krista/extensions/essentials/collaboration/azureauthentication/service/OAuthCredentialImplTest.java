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

import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.OAuthCredential;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.OAuthCredentialImpl;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class OAuthCredentialImplTest {

    private OAuthCredentialImpl credential;
    private String clientId;
    private String clientSecret;
    private String tenantId;

    @Before
    public void setUp() {
        clientId = "test-client-id-12345";
        clientSecret = "test-client-secret-67890";
        tenantId = "test-tenant-id-abcde";
        credential = new OAuthCredentialImpl(clientId, clientSecret, tenantId);
    }

    @Test
    public void testGetClientId() {
        assertEquals(clientId, credential.getClientId());
    }

    @Test
    public void testGetClientSecret() {
        assertEquals(clientSecret, credential.getClientSecret());
    }

    @Test
    public void testGetTenantId() {
        assertEquals(tenantId, credential.getTenantId());
    }

    @Test
    public void testConstructorWithNullValues() {
        OAuthCredentialImpl nullCredential = new OAuthCredentialImpl(null, null, null);
        assertNull(nullCredential.getClientId());
        assertNull(nullCredential.getClientSecret());
        assertNull(nullCredential.getTenantId());
    }

    @Test
    public void testConstructorWithEmptyStrings() {
        OAuthCredentialImpl emptyCredential = new OAuthCredentialImpl("", "", "");
        assertEquals("", emptyCredential.getClientId());
        assertEquals("", emptyCredential.getClientSecret());
        assertEquals("", emptyCredential.getTenantId());
    }

    @Test
    public void testConstructorWithMixedValues() {
        OAuthCredentialImpl mixedCredential = new OAuthCredentialImpl("client", null, "");
        assertEquals("client", mixedCredential.getClientId());
        assertNull(mixedCredential.getClientSecret());
        assertEquals("", mixedCredential.getTenantId());
    }

    @Test
    public void testImplementsOAuthCredentialInterface() {
        assertTrue(credential instanceof OAuthCredential);
    }

    @Test
    public void testCredentialImmutability() {
        String originalClientId = credential.getClientId();
        String originalClientSecret = credential.getClientSecret();
        String originalTenantId = credential.getTenantId();

        // Get values multiple times
        assertEquals(originalClientId, credential.getClientId());
        assertEquals(originalClientSecret, credential.getClientSecret());
        assertEquals(originalTenantId, credential.getTenantId());
    }

    @Test
    public void testConstructorWithLongValues() {
        String longClientId = "very-long-client-id-" + "x".repeat(100);
        String longClientSecret = "very-long-client-secret-" + "y".repeat(100);
        String longTenantId = "very-long-tenant-id-" + "z".repeat(100);

        OAuthCredentialImpl longCredential = new OAuthCredentialImpl(longClientId, longClientSecret, longTenantId);

        assertEquals(longClientId, longCredential.getClientId());
        assertEquals(longClientSecret, longCredential.getClientSecret());
        assertEquals(longTenantId, longCredential.getTenantId());
    }

    @Test
    public void testConstructorWithSpecialCharacters() {
        String specialClientId = "client-id!@#$%^&*()";
        String specialClientSecret = "secret_with-special.chars+123";
        String specialTenantId = "tenant/id\\with|special:chars";

        OAuthCredentialImpl specialCredential = new OAuthCredentialImpl(
                specialClientId, specialClientSecret, specialTenantId);

        assertEquals(specialClientId, specialCredential.getClientId());
        assertEquals(specialClientSecret, specialCredential.getClientSecret());
        assertEquals(specialTenantId, specialCredential.getTenantId());
    }

    @Test
    public void testMultipleInstancesAreIndependent() {
        OAuthCredentialImpl credential1 = new OAuthCredentialImpl("client1", "secret1", "tenant1");
        OAuthCredentialImpl credential2 = new OAuthCredentialImpl("client2", "secret2", "tenant2");

        assertEquals("client1", credential1.getClientId());
        assertEquals("client2", credential2.getClientId());
        assertNotEquals(credential1.getClientId(), credential2.getClientId());
    }
}

