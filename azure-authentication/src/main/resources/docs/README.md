# Azure Authentication Extension

## Overview

The Azure Authentication Extension provides seamless OAuth 2.0 authentication for Microsoft Azure users within the Krista platform. This extension enables secure user authentication using Microsoft Entra ID (formerly Azure Active Directory), allowing organizations to leverage their existing Azure infrastructure for user management and access control.

## Key Features

- ✅ **OAuth 2.0 Authentication**: Secure authentication using Microsoft's OAuth 2.0 implementation
- ✅ **Microsoft Entra ID Integration**: Direct integration with Microsoft Entra ID for user authentication
- ✅ **Automatic Account Provisioning**: Automatic creation and management of user accounts
- ✅ **Token Management**: Automatic handling of access and refresh tokens
- ✅ **Role-Based Access Control**: Configurable default roles for new users
- ✅ **Session Management**: Secure session handling and management
- ✅ **Trust Extension Support**: Enhanced security with trust extension capabilities

## Quick Start Guide

### Prerequisites
- Microsoft Azure subscription with Entra ID access
- Workspace administrator privileges in Krista
- Ability to create app registrations in Azure Portal

### Getting Started
1. **Create Azure App Registration** - Set up your application in Azure Portal
2. **Configure Extension** - Add your Azure credentials to the extension
3. **Enable Trust Extension** - Activate enhanced security features
4. **Test Connection** - Validate your configuration

## Documentation Structure

### 🚀 Getting Started
- [Extension Configuration](/pages/ExtensionConfiguration.md) - Complete setup and configuration guide
- [Authentication](/pages/Authentication.md) - OAuth 2.0 flows and token management
- [Creating Azure App](/pages/CreatingAzureApp.md) - Step-by-step Azure Portal setup

### 🔧 Troubleshooting
- [Error Message Reference](/pages/ErrorMessageReference.md) - Comprehensive error guide with solutions

### 📚 Additional Resources
- [Release Notes](/pages/ReleaseNotes.md) - Version history and updates

## Support & Resources

- **Extension Version**: 1.0.3
- **Krista Service APIs**: 1.0.117
- **Supported Authentication**: OAuth 2.0 with Microsoft Entra ID
- **Required Permissions**: User.Read (Microsoft Graph API)
- **Java Version**: 21

## Security Considerations

> **⚠️ Important**: This extension requires workspace administrator rights and handles sensitive authentication data. Ensure proper security measures are in place.

> **🔒 Security Best Practice**: Always use the Trust Extension feature in production environments for enhanced security.

## Quick Links

- [Extension Configuration](/pages/ExtensionConfiguration.md)
- [Authentication Setup](/pages/Authentication.md)
- [Azure App Creation](/pages/CreatingAzureApp.md)
- [Error Message Reference](/pages/ErrorMessageReference.md)
- [Release Notes](/pages/ReleaseNotes.md)
