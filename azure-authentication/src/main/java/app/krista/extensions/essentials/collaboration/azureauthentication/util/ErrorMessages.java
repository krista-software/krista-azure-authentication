package app.krista.extensions.essentials.collaboration.azureauthentication.util;

/**
 * User-friendly error messages for Azure Authentication Extension.
 * These messages provide clear, actionable guidance to help users resolve issues.
 */
public final class ErrorMessages {
    private ErrorMessages() {
        // Utility class - prevent instantiation
    }

    // Authentication and Authorization Errors
    public static final String UNAUTHORIZED_USER = 
        "Authentication required. Please sign in with your Microsoft account to continue. " +
        "If you continue to see this message, verify your Azure app configuration and permissions.";

    public static final String INVALID_TOKEN = 
        "Your session has expired or your authentication token is invalid. " +
        "Please sign in again. If this problem persists, contact your administrator.";

    public static final String TOKEN_REFRESH_FAILED = 
        "Unable to refresh your authentication session. This may happen if your permissions have been revoked " +
        "or your Azure app configuration has changed. Please sign in again.";

    // Configuration Errors
    public static final String INVALID_CLIENT_ID = 
        "Invalid Client ID. Please verify your Azure app registration and ensure the Client ID is correct. " +
        "You can find the Client ID in Azure Portal > App registrations > Your app > Overview.";

    public static final String INVALID_CLIENT_SECRET = 
        "Invalid Client Secret. The secret may have expired or is incorrect. " +
        "Please generate a new client secret in Azure Portal > App registrations > Your app > Certificates & secrets.";

    public static final String INVALID_TENANT_ID = 
        "Invalid Tenant ID. Please verify your Azure directory ID is correct. " +
        "You can find the Tenant ID in Azure Portal > Microsoft Entra ID > Overview.";

    public static final String MALFORMED_AUTHORIZATION_URL = 
        "Unable to create authorization URL. Please check your Azure app configuration, " +
        "especially the Client ID and Tenant ID. Ensure they are valid GUIDs.";

    // File and Resource Errors
    public static final String CONFIGURATION_FILE_LOAD_FAILED = 
        "Unable to load configuration file. This is an internal error. " +
        "Please contact your administrator or check the extension installation.";

    public static final String AUTHENTICATION_PAGE_LOAD_FAILED = 
        "Unable to load the authentication page. This may be due to network issues or server problems. " +
        "Please try again in a few moments.";

    // Domain and Email Validation Errors
    public static final String INVALID_EMAIL_FORMAT = 
        "Invalid email address format. Please enter a valid email address (e.g., user@company.com).";

    public static final String DOMAIN_NOT_SUPPORTED = 
        "Your email domain is not supported for authentication. " +
        "Please contact your administrator to add your domain to the supported domains list.";

    public static final String EMPTY_SUPPORTED_DOMAINS = 
        "No supported domains configured. Please contact your administrator to configure supported email domains.";

    public static final String INVALID_DOMAIN_FORMAT = 
        "Invalid domain format. Domain names must be valid (e.g., company.com). " +
        "Please check your domain configuration.";

    public static final String INVALID_INPUT_TYPE = 
        "Invalid configuration format. Expected text input but received a different type. " +
        "Please check your configuration values.";

    // Parameter Validation Errors
    public static final String MISSING_REQUIRED_PARAMETERS = 
        "Required configuration parameters are missing. Please ensure all required fields are filled: " +
        "Client ID, Client Secret, and Tenant ID.";

    public static final String EMPTY_PARAMETER_LIST = 
        "Configuration parameters list is empty. Please provide the required Azure app credentials.";

    public static final String MISSING_INVOKER_ATTRIBUTES = 
        "Extension configuration not found. Please configure the extension with your Azure app credentials " +
        "before using it.";

    // Network and Connectivity Errors
    public static final String NETWORK_CONNECTION_FAILED = 
        "Unable to connect to Microsoft Azure services. Please check your internet connection " +
        "and ensure that Azure endpoints are accessible from your network.";

    public static final String AZURE_SERVICE_UNAVAILABLE = 
        "Microsoft Azure authentication service is temporarily unavailable. " +
        "Please try again in a few minutes. If the problem persists, check Azure service status.";

    public static final String FIREWALL_BLOCKING_REQUEST = 
        "Network request blocked. Your firewall or proxy may be blocking connections to Microsoft Azure. " +
        "Please contact your network administrator to allow access to Azure authentication endpoints.";

    // Permission and Access Errors
    public static final String INSUFFICIENT_PERMISSIONS = 
        "Insufficient permissions. Please ensure the 'User.Read' permission is granted to your Azure app " +
        "and that admin consent has been provided if required.";

    public static final String ADMIN_CONSENT_REQUIRED = 
        "Administrator consent required. Please ask your Azure administrator to grant consent " +
        "for this application to access user profile information.";

    public static final String USER_ACCESS_DENIED = 
        "Access denied. You may have declined permission during sign-in, or your account may not have " +
        "the necessary permissions. Please try signing in again and grant the requested permissions.";

    // Redirect URI Errors
    public static final String REDIRECT_URI_MISMATCH = 
        "Redirect URI mismatch. The redirect URI in your Azure app registration must exactly match: " +
        "https://extension.us.krista.app/extension/api/authn/v3/oauth/callback";

    public static final String MISSING_REDIRECT_URI = 
        "Missing redirect URI in Azure app registration. Please add the required redirect URI: " +
        "https://extension.us.krista.app/extension/api/authn/v3/oauth/callback";

    // Account and Role Errors
    public static final String ACCOUNT_CREATION_FAILED = 
        "Unable to create user account. This may be due to insufficient permissions or configuration issues. " +
        "Please contact your administrator.";

    public static final String ROLE_ASSIGNMENT_FAILED = 
        "Unable to assign user role. The specified role may not exist or you may not have permission " +
        "to assign roles. Please contact your administrator.";

    public static final String WORKSPACE_ACCESS_DENIED = 
        "Access to this workspace is denied. Please contact your workspace administrator " +
        "to grant you access or verify your account permissions.";

    // Session and State Errors
    public static final String SESSION_EXPIRED = 
        "Your session has expired. Please sign in again to continue.";

    public static final String INVALID_SESSION_STATE = 
        "Invalid session state. This may be due to a security issue or browser problems. " +
        "Please clear your browser cache and try signing in again.";

    public static final String SESSION_CREATION_FAILED = 
        "Unable to create authentication session. This is an internal error. " +
        "Please try again or contact your administrator if the problem persists.";

    // Generic Error Messages
    public static final String UNEXPECTED_ERROR = 
        "An unexpected error occurred. Please try again. If the problem persists, " +
        "contact your administrator with the error details.";

    public static final String CONFIGURATION_VALIDATION_FAILED = 
        "Configuration validation failed. Please check all your Azure app settings and try again. " +
        "Ensure your Client ID, Client Secret, and Tenant ID are correct and not expired.";

    public static final String AUTHENTICATION_FLOW_INTERRUPTED = 
        "Authentication process was interrupted. This may be due to network issues or browser problems. " +
        "Please try signing in again.";

    // Success Messages
    public static final String AUTHENTICATION_SUCCESSFUL = 
        "Authentication successful! You are now signed in.";

    public static final String CONFIGURATION_VALID = 
        "Configuration is valid. Your Azure app settings are correctly configured.";

    public static final String TOKEN_REFRESHED_SUCCESSFULLY = 
        "Authentication session refreshed successfully.";

    // Helper Methods for Dynamic Messages
    public static String missingRequiredParameter(String parameterName) {
        return String.format("Required parameter '%s' is missing. Please provide a valid %s in your configuration.", 
                           parameterName, parameterName);
    }

    public static String invalidParameterFormat(String parameterName, String expectedFormat) {
        return String.format("Invalid format for '%s'. Expected format: %s. Please check your input and try again.", 
                           parameterName, expectedFormat);
    }

    public static String domainNotSupported(String domain) {
        return String.format("The domain '%s' is not supported for authentication. " +
                           "Please contact your administrator to add this domain to the supported domains list.", domain);
    }

    public static String userNotFound(String email) {
        return String.format("User account not found for '%s'. Please verify the email address " +
                           "or contact your administrator to create an account.", email);
    }

    public static String configurationError(String component, String suggestion) {
        return String.format("Configuration error in %s. %s Please check your settings and try again.", 
                           component, suggestion);
    }

    public static String networkError(String endpoint, String action) {
        return String.format("Unable to connect to %s for %s. Please check your network connection " +
                           "and try again. If the problem persists, contact your administrator.", endpoint, action);
    }
}
