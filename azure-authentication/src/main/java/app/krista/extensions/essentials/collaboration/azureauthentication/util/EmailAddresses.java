package app.krista.extensions.essentials.collaboration.azureauthentication.util;

import java.util.regex.Pattern;

public final class EmailAddresses {

    public static final String DEFAULT_DOMAIN = "kristasoft.com";
    private static final Pattern EMAIL_ADDRESS_PATTERN =
            Pattern.compile("^[a-z0-9_+&*-]+(?:\\.[a-z0-9_+&*-]+)*@(?:[a-z0-9-]+\\.)+[a-z]{2,7}$");//NOSONAR

    private EmailAddresses() {
    }

    public static boolean isValidEmailAddress(String emailAddress) {
        if (emailAddress == null || emailAddress.isBlank()) {
            return false;
        }
        return EMAIL_ADDRESS_PATTERN.matcher(normalize(emailAddress)).matches();
    }

    private static String normalize(String emailAddress) {
        return emailAddress.strip().toLowerCase();
    }

    public static String normalizeEmailAddress(String emailAddress) {
        if (!isValidEmailAddress(emailAddress)) {
            throw new IllegalArgumentException("Email address is not valid");
        }
        return normalize(emailAddress);
    }

    public static String getDomainName(String emailAddress) {
        return normalizeEmailAddress(emailAddress).split("@")[1];
    }

    public static String getLocalPart(String emailAddress) {
        return normalizeEmailAddress(emailAddress).split("@")[0];
    }

}
