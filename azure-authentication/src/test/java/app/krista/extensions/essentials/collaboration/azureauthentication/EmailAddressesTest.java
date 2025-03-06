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
