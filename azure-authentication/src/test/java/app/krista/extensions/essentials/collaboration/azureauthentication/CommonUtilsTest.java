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

import app.krista.extensions.essentials.collaboration.azureauthentication.util.CommonUtils;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.junit.Assert.*;

public class CommonUtilsTest {

    @Test
    public void testIsValidDomain() {
        assertTrue(CommonUtils.isValidDomain("example.com"));
        assertFalse(CommonUtils.isValidDomain("invalid_domain"));
        assertFalse(CommonUtils.isValidDomain(null));
        assertFalse(CommonUtils.isValidDomain(""));
    }

    @Test
    public void testIsEmailDomainPresentInSupportedWorkspaceDomains() {
        assertTrue(CommonUtils.isEmailDomainPresentInSupportedWorkspaceDomains("user@example.com", "example.com,domain.com"));
        assertFalse(CommonUtils.isEmailDomainPresentInSupportedWorkspaceDomains("user@random.com", "example.com,domain.com"));
        assertTrue(CommonUtils.isEmailDomainPresentInSupportedWorkspaceDomains("user@example.com", "All"));
        assertTrue(CommonUtils.isEmailDomainPresentInSupportedWorkspaceDomains("user@example.com", ""));
        assertFalse(CommonUtils.isEmailDomainPresentInSupportedWorkspaceDomains(null, "example.com"));
    }

    @Test
    public void testGetDate() {
        assertNotNull(CommonUtils.getDate());
    }

    @Test
    public void testIsValidEmail() {
        assertTrue(CommonUtils.isValidEmail("user@example.com"));
        assertFalse(CommonUtils.isValidEmail("invalid-email"));
        assertFalse(CommonUtils.isValidEmail(null));
    }

    @Test
    public void testValidateDomains() {
        try {
            CommonUtils.validateDomains("example.com,domain.com");
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        assertThrows(IllegalArgumentException.class, () -> CommonUtils.validateDomains("invalid_domain"));
        assertThrows(IllegalArgumentException.class, () -> CommonUtils.validateDomains(""));
        assertThrows(IllegalArgumentException.class, () -> CommonUtils.validateDomains(123));
    }

    @Test
    public void testValidateInputParams() {
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("email", "user@example.com");
        Set<String> requiredInputs = Set.of("email");
        try {
            CommonUtils.validateInputParams(attributes, requiredInputs, "example.com");
        } catch (Exception e) {
            fail("Exception should not be thrown");
        }

        assertThrows(IllegalArgumentException.class, () -> CommonUtils.validateInputParams(null, requiredInputs, "example.com"));
        assertThrows(IllegalArgumentException.class, () -> CommonUtils.validateInputParams(attributes, null, "example.com"));
    }

    @Test
    public void testFindValue() {
        assertEquals("value", CommonUtils.findValue("key=value"));
        assertNull(CommonUtils.findValue("key"));
        assertEquals("123", CommonUtils.findValue("number=123"));
    }
}

