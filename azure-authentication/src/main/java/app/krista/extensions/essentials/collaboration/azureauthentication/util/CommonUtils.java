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

package app.krista.extensions.essentials.collaboration.azureauthentication.util;

import app.krista.extension.authorization.AuthorizationException;
import app.krista.ksdk.authentication.AuthenticationSettings;
import static app.krista.extensions.essentials.collaboration.azureauthentication.util.ErrorMessages.*;

import javax.ws.rs.core.NewCookie;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CommonUtils {
    private CommonUtils() {
    }

    public static boolean isValidDomain(String domainName) {

        // If the string is empty
        // return false
        if (domainName == null || domainName.isEmpty()) {
            return false;
        }

        // Regex to check valid domain name.
        String regex = "^((?!-)[A-Za-z0-9-]"
                + "{1,63}(?<!-)\\.)"
                + "+[A-Za-z]{2,6}";

        // Compile the ReGex
        Pattern p
                = Pattern.compile(regex);

        // Pattern class contains matcher()
        // method to find the matching
        // between the given string and
        // regular expression.
        Matcher m = p.matcher(domainName);

        // Return if the string
        // matched the ReGex
        return m.matches();
    }

    public static boolean isEmailDomainPresentInSupportedWorkspaceDomains(String email, String allDomains) {
        if (email == null || email.isBlank()) {
            return false;
        }
        if (allDomains == null || allDomains.isEmpty() || allDomains.equalsIgnoreCase("All")) {
            return true;
        } else {
            String[] splitDomains = allDomains.split(",");
            String[] splitEmail = email.split("@");
            for (String domain : splitDomains) {
                if (domain.contains(splitEmail[1])) {
                    return true;
                }
            }
        }
        return false;
    }

    public static void addSupportedDomainsToWorkspace(String email, List<String> supportedDomainsForWorkspace,
                                                      AuthenticationSettings authenticationSettings)
            throws AuthorizationException {
        if (email != null && supportedDomainsForWorkspace != null && authenticationSettings != null) {
            String[] splitEmail = email.split("@");
            if (splitEmail.length > 1 && !supportedDomainsForWorkspace.contains(splitEmail[1])) {
                authenticationSettings.addSupportedDomains(splitEmail[1]);
            }
        }
    }

    public static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss Z");
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(Calendar.getInstance().getTime());
    }

    public static boolean isValidEmail(String email) {
        //https://www.geeksforgeeks.org/check-email-address-valid-not-java/
        if (email == null) {
            return false;
        }
        String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\." +
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,7}$"; //NOSONAR
        Pattern pat = Pattern.compile(emailRegex);
        return pat.matcher(email).matches();
    }

    public static void validateDomains(Object supportedDomains) {
        if (!(supportedDomains instanceof String)) {
            throw new IllegalArgumentException(INVALID_INPUT_TYPE);
        }
        if (((String) supportedDomains).isEmpty()) {
            throw new IllegalArgumentException(EMPTY_SUPPORTED_DOMAINS);
        }
        String supportedDomainsString = (String) supportedDomains;
        String[] domains = supportedDomainsString.split(",");
        if (domains.length == 0) {
            throw new IllegalArgumentException(EMPTY_SUPPORTED_DOMAINS);
        }
        for (String domain : domains) {
            String trimmedDomain = domain.trim();
            if (!trimmedDomain.equalsIgnoreCase("all") && !CommonUtils.isValidDomain(trimmedDomain)) {
                throw new IllegalArgumentException(invalidParameterFormat("domain", "company.com") +
                    " Invalid domain: " + domain);
            }
        }
    }

    public static void validateInputParams(Map<String, Object> invokerAttributes, Set<String> requiredInputs,
                                           Object supportedDomains) {
        if (invokerAttributes == null || invokerAttributes.isEmpty()) {
            throw new IllegalArgumentException(MISSING_INVOKER_ATTRIBUTES);
        }
        if (requiredInputs == null || requiredInputs.isEmpty()) {
            throw new IllegalArgumentException(EMPTY_PARAMETER_LIST);
        }
        Set<String> missingRequiredParams = new HashSet<>();
        for (String requiredParam : requiredInputs) {
            if (!invokerAttributes.containsKey(requiredParam) || invokerAttributes.get(requiredParam) == null ||
                    invokerAttributes.get(requiredParam).toString().isEmpty()) {
                missingRequiredParams.add(requiredParam);
            }
        }
        if (!missingRequiredParams.isEmpty()) {
            throw new IllegalArgumentException(MISSING_REQUIRED_PARAMETERS +
                " Missing parameters: " + String.join(", ", missingRequiredParams));
        }
        validateDomains(supportedDomains);
    }

    public static void validateIfSupportedDomain(String email, String allDomains, Object supportedDomains) {
        if (!isValidEmail(email)) {
            throw new IllegalArgumentException(INVALID_EMAIL_FORMAT + " Provided email: " + email);
        }
        if (!supportedDomains.toString().isEmpty()) {
            boolean isSupportedFromExtension = CommonUtils
                    .isEmailDomainPresentInSupportedWorkspaceDomains(email,
                            supportedDomains.toString());
            boolean isSupportedFromWorkSpace =
                    isEmailDomainPresentInSupportedWorkspaceDomains(email,
                            allDomains);
            if (!isSupportedFromExtension && !isSupportedFromWorkSpace) {
                String domain = email.substring(email.indexOf("@") + 1);
                throw new IllegalArgumentException(domainNotSupported(domain));
            }
        }
    }

    /**
     * Parses the given string and returns the part of string after the "=" sign.
     *
     * @param keyValueString the string to parse. It should be in a format like "key=value"
     * @return value part of the given string after the "=" sign, null if there is none
     */
    public static String findValue(String keyValueString) {
        String[] keyValue = keyValueString.split("=");
        return keyValue.length > 1 ? keyValue[1] : null;
    }

    public static String buildSecureCookie(String name, String value, String domain, int maxAge, Date expiry) {
        NewCookie baseCookie = new NewCookie(
                name,
                value,
                "/",
                domain,
                NewCookie.DEFAULT_VERSION,
                "Cookie for session management",
                maxAge,
                expiry,
                true,
                true
        );
        return baseCookie+ "; SameSite=None";
    }
}
