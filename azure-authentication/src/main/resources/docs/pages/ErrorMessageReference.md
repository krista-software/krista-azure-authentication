# Error Message Reference Guide

## Overview

This comprehensive guide provides detailed information about all error messages you may encounter when using the Azure Authentication Extension. Each error includes the exact message text, possible causes, and step-by-step resolution instructions.

## How to Use This Guide

1. **Find Your Error**: Use Ctrl+F (Cmd+F on Mac) to search for keywords from your error message
2. **Identify the Cause**: Review the possible causes for your specific error
3. **Follow Resolution Steps**: Complete the resolution steps in order
4. **Verify the Fix**: Test your configuration after implementing the solution
5. **Get Additional Help**: If the error persists, see the [Support](#support) section

## Authentication and Authorization Errors

### Authentication Required
**Error Message**: "Authentication required. Please sign in with your Microsoft account to continue. If you continue to see this message, verify your Azure app configuration and permissions."

**When This Occurs**: When accessing the extension without proper authentication
**Possible Causes**:
- User session has expired
- User has not signed in yet
- Authentication cookies were cleared
- Azure app configuration issues

**Resolution Steps**:
1. Click the sign-in button or link
2. Complete the Microsoft authentication flow
3. Grant requested permissions when prompted
4. If problem persists, check Azure app configuration
5. Verify redirect URI is correctly configured

### Session Expired
**Error Message**: "Your session has expired. Please sign in again to continue."

**When This Occurs**: When trying to use the extension with an expired session
**Possible Causes**:
- Session timeout reached
- User was inactive for extended period
- Browser cookies were cleared
- System clock issues

**Resolution Steps**:
1. Sign out completely from the extension
2. Clear browser cache and cookies for the site
3. Sign in again with your Microsoft account
4. Ensure system clock is correct
5. Contact administrator if problem persists

### Invalid Token
**Error Message**: "Your session has expired or your authentication token is invalid. Please sign in again. If this problem persists, contact your administrator."

**When This Occurs**: When the authentication token cannot be validated
**Possible Causes**:
- Token has expired
- Token was revoked by user or administrator
- Azure app configuration changed
- Network connectivity issues during token validation

**Resolution Steps**:
1. Sign out and sign in again
2. Check if you revoked app permissions in your Microsoft account
3. Verify Azure app configuration hasn't changed
4. Test network connectivity to Microsoft services
5. Contact administrator if issue continues

## Configuration Errors

### Invalid Client ID
**Error Message**: "Invalid Client ID. Please verify your Azure app registration and ensure the Client ID is correct. You can find the Client ID in Azure Portal > App registrations > Your app > Overview."

**When This Occurs**: During extension configuration or validation
**Possible Causes**:
- Incorrect Client ID format
- Client ID from wrong Azure app
- Typos in Client ID entry
- Using Object ID instead of Application (client) ID

**Resolution Steps**:
1. Go to Azure Portal > App registrations
2. Select your application
3. Copy the "Application (client) ID" from Overview page
4. Ensure it's in GUID format: `12345678-1234-1234-1234-123456789abc`
5. Paste exactly into extension configuration
6. Validate configuration

### Invalid Client Secret
**Error Message**: "Invalid Client Secret. The secret may have expired or is incorrect. Please generate a new client secret in Azure Portal > App registrations > Your app > Certificates & secrets."

**When This Occurs**: During configuration validation or authentication
**Possible Causes**:
- Client secret has expired
- Incorrect secret value
- Secret from wrong Azure app
- Using Secret ID instead of secret value

**Resolution Steps**:
1. Go to Azure Portal > App registrations > Your app
2. Navigate to "Certificates & secrets"
3. Check if current secret is expired (red warning icon)
4. Create new client secret if needed
5. Copy the VALUE immediately (shown only once)
6. Update extension configuration with new secret
7. Test configuration

### Invalid Tenant ID
**Error Message**: "Invalid Tenant ID. Please verify your Azure directory ID is correct. You can find the Tenant ID in Azure Portal > Microsoft Entra ID > Overview."

**When This Occurs**: During configuration setup or validation
**Possible Causes**:
- Incorrect Tenant ID format
- Using wrong Azure directory
- Typos in Tenant ID
- Using domain name instead of Tenant ID

**Resolution Steps**:
1. Go to Azure Portal > Microsoft Entra ID > Overview
2. Copy the "Tenant ID" (also called Directory ID)
3. Ensure it's in GUID format: `87654321-4321-4321-4321-210987654321`
4. Verify you're in the correct Azure directory
5. Update extension configuration
6. Validate configuration

### Missing Required Parameters
**Error Message**: "Required configuration parameters are missing. Please ensure all required fields are filled: Client ID, Client Secret, and Tenant ID."

**When This Occurs**: When trying to save or validate incomplete configuration
**Possible Causes**:
- One or more required fields are empty
- Fields contain only whitespace
- Configuration was partially cleared

**Resolution Steps**:
1. Check all required fields are filled:
   - Client ID
   - Client Secret
   - Tenant ID
2. Ensure no fields contain only spaces
3. Verify all values are correct
4. Save configuration
5. Run validation test

## Network and Connectivity Errors

### Network Connection Failed
**Error Message**: "Unable to connect to Microsoft Azure services. Please check your internet connection and ensure that Azure endpoints are accessible from your network."

**When This Occurs**: When the extension cannot reach Microsoft services
**Possible Causes**:
- No internet connection
- Firewall blocking Azure endpoints
- Proxy configuration issues
- DNS resolution problems

**Resolution Steps**:
1. Check internet connectivity
2. Try accessing https://login.microsoftonline.com in browser
3. Contact network administrator about firewall rules
4. Check proxy settings if applicable
5. Verify DNS resolution is working
6. Test from different network if possible

### Azure Service Unavailable
**Error Message**: "Microsoft Azure authentication service is temporarily unavailable. Please try again in a few minutes. If the problem persists, check Azure service status."

**When This Occurs**: When Microsoft Azure services are experiencing issues
**Possible Causes**:
- Azure service outage
- Maintenance window
- Regional service issues
- High load on Azure services

**Resolution Steps**:
1. Wait 5-10 minutes and try again
2. Check Azure Service Health: https://status.azure.com
3. Try authentication from different location
4. Contact Microsoft support if widespread issue
5. Monitor Azure status page for updates

## Permission and Access Errors

### Insufficient Permissions
**Error Message**: "Insufficient permissions. Please ensure the 'User.Read' permission is granted to your Azure app and that admin consent has been provided if required."

**When This Occurs**: When the app lacks necessary permissions
**Possible Causes**:
- User.Read permission not added to Azure app
- Admin consent not granted
- Permissions were revoked
- User doesn't have permission to consent

**Resolution Steps**:
1. Go to Azure Portal > App registrations > Your app
2. Navigate to "API permissions"
3. Ensure "User.Read" permission is listed
4. Check if admin consent is required and granted
5. Ask administrator to grant consent if needed
6. Test authentication after permission changes

### Domain Not Supported
**Error Message**: "Your email domain is not supported for authentication. Please contact your administrator to add your domain to the supported domains list."

**When This Occurs**: When user's email domain is not in allowed list
**Possible Causes**:
- User's domain not in supported domains configuration
- Domain restrictions enabled
- Typo in domain configuration
- User using personal email instead of work email

**Resolution Steps**:
1. Contact your administrator
2. Provide your email domain (e.g., company.com)
3. Ask administrator to add domain to supported list
4. Try authentication after domain is added
5. Use work email if currently using personal email

## File and Resource Errors

### Configuration File Load Failed
**Error Message**: "Unable to load configuration file. This is an internal error. Please contact your administrator or check the extension installation."

**When This Occurs**: When extension cannot load internal configuration
**Possible Causes**:
- Extension installation corrupted
- File system permissions issues
- Server configuration problems
- Missing extension files

**Resolution Steps**:
1. Contact your administrator immediately
2. Report the exact error message
3. Provide timestamp when error occurred
4. Administrator should check extension installation
5. May require extension reinstallation

### Authentication Page Load Failed
**Error Message**: "Unable to load the authentication page. This may be due to network issues or server problems. Please try again in a few moments."

**When This Occurs**: When authentication page cannot be displayed
**Possible Causes**:
- Network connectivity issues
- Server overload
- Browser compatibility issues
- JavaScript disabled

**Resolution Steps**:
1. Refresh the page
2. Check internet connection
3. Try different browser
4. Ensure JavaScript is enabled
5. Clear browser cache and cookies
6. Contact administrator if problem persists

## Validation and Format Errors

### Invalid Email Format
**Error Message**: "Invalid email address format. Please enter a valid email address (e.g., user@company.com)."

**When This Occurs**: When entering an incorrectly formatted email
**Possible Causes**:
- Missing @ symbol
- Invalid characters in email
- Missing domain extension
- Extra spaces or characters

**Resolution Steps**:
1. Check email format: user@domain.com
2. Remove any extra spaces
3. Ensure @ symbol is present
4. Verify domain has valid extension (.com, .org, etc.)
5. Use work email address if available

### Invalid Domain Format
**Error Message**: "Invalid domain format. Domain names must be valid (e.g., company.com). Please check your domain configuration."

**When This Occurs**: When configuring supported domains
**Possible Causes**:
- Domain missing extension (.com, .org)
- Invalid characters in domain name
- Spaces in domain name
- Using full email instead of domain

**Resolution Steps**:
1. Use domain format: company.com (not user@company.com)
2. Remove any spaces
3. Ensure valid domain extension
4. Use only letters, numbers, hyphens, and dots
5. Test domain format before saving

## Support

### Getting Additional Help

If you continue to experience issues after following this guide:

1. **Document the Error**:
   - Copy the exact error message
   - Note when the error occurs
   - Record steps that led to the error

2. **Gather Information**:
   - Extension version
   - Browser and version
   - Operating system
   - Network environment (corporate, home, etc.)

3. **Contact Support**:
   - Provide all documented information
   - Include screenshots if helpful
   - Mention steps already attempted

4. **Administrator Resources**:
   - [Extension Configuration](/pages/ExtensionConfiguration.md)
   - [Authentication Guide](/pages/Authentication.md)
   - [Azure App Setup](/pages/CreatingAzureApp.md)

### Emergency Contacts

For critical authentication issues affecting multiple users:
- Contact your IT administrator immediately
- Escalate to Microsoft support if Azure services are affected
- Check Azure Service Health for known issues

---

> **📝 Note**: This guide covers the most common error scenarios. If you encounter an error not listed here, please contact your administrator with the exact error message for assistance.
