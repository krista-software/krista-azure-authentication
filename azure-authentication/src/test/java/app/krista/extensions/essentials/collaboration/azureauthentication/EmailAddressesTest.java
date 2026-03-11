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

import app.krista.extensions.essentials.collaboration.azureauthentication.util.EmailAddresses;
import org.junit.Test;

import static org.junit.Assert.*;

public class EmailAddressesTest {
    @Test
    public void testIsValidEmailAddress() {
        assertTrue(EmailAddresses.isValidEmailAddress("test@kristasoft.com"));
        assertTrue(EmailAddresses.isValidEmailAddress("user.name@domain.com"));
        assertFalse(EmailAddresses.isValidEmailAddress("invalid-email"));
        assertFalse(EmailAddresses.isValidEmailAddress("user@domain"));
        assertFalse(EmailAddresses.isValidEmailAddress("@domain.com"));
        assertFalse(EmailAddresses.isValidEmailAddress(null));
        assertFalse(EmailAddresses.isValidEmailAddress(""));
    }

    @Test
    public void testNormalizeEmailAddress() {
        assertEquals("test@kristasoft.com", EmailAddresses.normalizeEmailAddress("  TEST@KristaSoft.COM  "));
        assertThrows(IllegalArgumentException.class, () -> EmailAddresses.normalizeEmailAddress("invalid-email"));
    }

    @Test
    public void testGetDomainName() {
        assertEquals("kristasoft.com", EmailAddresses.getDomainName("test@kristasoft.com"));
        assertThrows(IllegalArgumentException.class, () -> EmailAddresses.getDomainName("invalid-email"));
    }

    @Test
    public void testGetLocalPart() {
        assertEquals("test", EmailAddresses.getLocalPart("test@kristasoft.com"));
        assertThrows(IllegalArgumentException.class, () -> EmailAddresses.getLocalPart("invalid-email"));
    }
}
