# Authentication

## Overview

The Azure Authentication Extension implements OAuth 2.0 authentication with Microsoft Entra ID (formerly Azure Active Directory) to provide secure user authentication and authorization. This extension supports delegated permissions, allowing users to authenticate with their Microsoft credentials while maintaining secure access to Krista resources.

## Authentication Modes

### User Account Authentication (Delegated Permissions)
The extension uses **delegated permissions** mode, which means:
- Users authenticate with their own Microsoft credentials
- The application acts on behalf of the authenticated user
- Permissions are granted through user consent
- Each user maintains their own authentication session

**When to Use**: This is the primary and recommended authentication mode for the Azure Authentication Extension, suitable for:
- Interactive user authentication
- Applications requiring user-specific permissions
- Scenarios where user identity is important
- Multi-user environments

## OAuth 2.0 Scopes and Permissions

### Required Scopes

The extension requires the following Microsoft Graph API scope:

| Scope | Full URL | Permission Level | Description |
|-------|----------|------------------|-------------|
| User.Read | `https://graph.microsoft.com/User.Read` | Delegated | Read the signed-in user's profile |

### Permission Details

#### User.Read Scope
- **Purpose**: Allows the application to read the signed-in user's profile information
- **Access Level**: Read-only access to user profile
- **Information Retrieved**:
  - User principal name (email address)
  - Display name
  - Basic profile information
- **User Consent**: Required on first authentication
- **Admin Consent**: Not required for this scope

### Why User.Read is Sufficient
The extension only requires basic user profile information for:
- User identification and account provisioning
- Email address retrieval for account mapping
- Basic profile information for user management

**Alternative Scopes Not Used**:
- `User.ReadWrite`: Not needed as the extension doesn't modify user profiles
- `Directory.Read.All`: Not required as only individual user info is needed
- `Mail.Read`: Not necessary as the extension doesn't access user emails

## OAuth 2.0 Grant Types

### Authorization Code Grant
- **Grant Type**: `authorization_code`
- **Used For**: Initial user authentication
- **Flow**: Interactive authentication with user consent
- **Security**: Most secure OAuth 2.0 flow for web applications

**Request Example**:
```http
GET https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/authorize?
client_id={client-id}&
response_type=code&
redirect_uri=https://extension.us.krista.app/extension/api/authn/v3/oauth/callback&
scope=https://graph.microsoft.com/User.Read&
state={state-value}
```

**Response Example**:
```http
HTTP/1.1 302 Found
Location: https://extension.us.krista.app/extension/api/authn/v3/oauth/callback?
code={authorization-code}&
state={state-value}
```

### Refresh Token Grant
- **Grant Type**: `refresh_token`
- **Used For**: Obtaining new access tokens without user interaction
- **Flow**: Automatic token refresh using stored refresh token
- **Security**: Enables seamless user experience

**Request Example**:
```http
POST https://login.microsoftonline.com/{tenant-id}/oauth2/v2.0/token
Content-Type: application/x-www-form-urlencoded

client_id={client-id}&
client_secret={client-secret}&
grant_type=refresh_token&
refresh_token={refresh-token}&
scope=https://graph.microsoft.com/User.Read
```

**Response Example**:
```json
{
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGciOiJSUzI1NiIs...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "AwABAAAAvPM1KaPlrEqdFSBzjqfTGBCmLdgfSTLEMPGYuNHSUYBrq...",
  "scope": "https://graph.microsoft.com/User.Read"
}
```

## Authentication Flow

### Step-by-Step OAuth Flow

1. **User Initiates Authentication**
   - User accesses Krista application
   - Extension detects unauthenticated user
   - Redirects to Azure authorization endpoint

2. **Azure Authorization**
   - User presented with Microsoft login page
   - User enters credentials and consents to permissions
   - Azure generates authorization code

3. **Authorization Code Exchange**
   - Azure redirects to extension callback URL with authorization code
   - Extension exchanges authorization code for access and refresh tokens
   - Tokens are securely stored

4. **User Profile Retrieval**
   - Extension uses access token to call Microsoft Graph API
   - Retrieves user profile information (email, name)
   - Creates or updates user account in Krista

5. **Session Creation**
   - Extension creates authenticated session
   - User gains access to Krista resources
   - Session maintained until expiration or logout

### Authorization Code Exchange Details

The extension performs the following steps during code exchange:

1. **Receive Authorization Code**: Extract code from callback URL
2. **Validate State Parameter**: Verify state to prevent CSRF attacks
3. **Exchange Code for Tokens**: POST request to token endpoint
4. **Store Tokens Securely**: Save access and refresh tokens
5. **Retrieve User Info**: Call Microsoft Graph with access token
6. **Provision Account**: Create or update user account
7. **Create Session**: Establish authenticated session

## Token Management

### Access Token Details
- **Lifetime**: 1 hour (3600 seconds)
- **Format**: JWT (JSON Web Token)
- **Usage**: Authenticate API calls to Microsoft Graph
- **Storage**: Temporarily stored in memory during session
- **Refresh**: Automatically refreshed using refresh token

### Refresh Token Details
- **Lifetime**: 
  - **Verified Apps**: Up to 90 days of inactivity
  - **Unverified Apps**: Up to 24 hours
- **Usage**: Obtain new access tokens without user interaction
- **Storage**: Securely stored and encrypted
- **Rotation**: New refresh token provided with each refresh

### Automatic Token Refresh
The extension automatically handles token refresh:

1. **Monitor Token Expiration**: Check access token expiry
2. **Refresh Before Expiry**: Refresh tokens proactively
3. **Handle Refresh Failures**: Re-authenticate user if refresh fails
4. **Update Stored Tokens**: Replace old tokens with new ones

## Token Expiration Scenarios

### Access Token Expiration
**Timeline**: Every 1 hour
**Handling**: 
- Automatic refresh using refresh token
- Seamless user experience
- No user interaction required

**Flow**:
1. Extension detects token expiration
2. Uses refresh token to get new access token
3. Updates stored tokens
4. Continues operation without interruption

### Refresh Token Expiration
**Timeline**: 
- Verified apps: Up to 90 days of inactivity
- Unverified apps: Up to 24 hours

**Handling**:
- User re-authentication required
- Redirect to Azure login page
- Complete OAuth flow again

### Token Revocation Scenarios
Tokens may be revoked due to:

1. **User Action**: User revokes app permissions in Azure
2. **Password Change**: User changes Microsoft password
3. **Security Events**: Suspicious activity detected
4. **Token Limits**: Maximum number of tokens exceeded
5. **Admin Action**: Administrator revokes access

**Error Handling Flow**:
1. Extension detects invalid token
2. Attempts token refresh
3. If refresh fails, initiates re-authentication
4. User redirected to Azure login
5. New tokens obtained after successful authentication

## Security Best Practices

### 1. Token Security
- **Encryption**: All tokens encrypted at rest
- **Secure Storage**: Tokens stored in secure session store
- **Limited Scope**: Minimal required permissions requested
- **Token Rotation**: Regular refresh token rotation

### 2. Communication Security
- **HTTPS Only**: All communication over encrypted channels
- **Certificate Validation**: Proper SSL/TLS validation
- **Secure Headers**: Implementation of security headers

### 3. Session Management
- **Session Timeout**: Configurable session timeouts
- **Secure Cookies**: HttpOnly and Secure cookie flags
- **Session Invalidation**: Proper session cleanup on logout

### 4. Error Handling
- **No Token Exposure**: Tokens never exposed in logs or errors
- **Graceful Degradation**: Proper error handling and user feedback
- **Audit Logging**: Security events logged for monitoring

## Troubleshooting Authentication

### Common Authentication Issues

#### Authentication Fails
**Symptoms**: User cannot complete login process
**Possible Causes**:
- Invalid Azure app configuration
- Missing or incorrect permissions
- Network connectivity issues
- Expired client secret

**Resolution Steps**:
1. Verify Azure app registration configuration
2. Check required permissions are granted
3. Ensure client secret is valid and not expired
4. Test network connectivity to Azure endpoints

#### Token Refresh Fails
**Symptoms**: Users frequently prompted to re-authenticate
**Possible Causes**:
- Refresh token expired
- Azure app configuration changed
- User revoked permissions
- Token storage issues

**Resolution Steps**:
1. Check refresh token expiration policies
2. Verify Azure app configuration unchanged
3. Confirm user hasn't revoked permissions
4. Review token storage mechanisms

#### Permission Denied Errors
**Symptoms**: "Insufficient permissions" errors
**Possible Causes**:
- Missing User.Read permission
- Admin consent required but not granted
- Incorrect permission configuration

**Resolution Steps**:
1. Verify User.Read permission is added to Azure app
2. Grant admin consent if required
3. Check permission configuration in Azure Portal

## See Also

- [Extension Configuration](/pages/ExtensionConfiguration.md) - Setup and configuration guide
- [Creating Azure App](/pages/CreatingAzureApp.md) - Azure Portal setup
- [Release Notes](/pages/ReleaseNotes.md) - Version history and updates
