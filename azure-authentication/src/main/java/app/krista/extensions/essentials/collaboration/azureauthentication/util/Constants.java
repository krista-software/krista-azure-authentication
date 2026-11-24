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

public class Constants {
    private Constants() {
    }

    public static final String AUTH_URL_KEY = "authUrl";
    public static final String SEMI_COLON = ";";
    public static final String TENANT_ID = "tenantId";
    public static final String CLIENT_SECRET = "clientSecret";
    public static final String CLIENT_ID = "clientId";
    public static final String DEFAULT_CALLBACK_PATH = "/extension/api/authn/v3/oauth/callback";//NOSONAR
    public static final String CHATBOT_SESSION_ID = "chatbotSessionId";
    public static final String X_KRISTA_CONTEXT = "X-Krista-Context";
    public static final String COMMENT = "comment";
    public static final String COOKIE = "Cookie";
    public static final String BLANK_SPACE = " ";
    public static final String CODE = "code";
    public static final String STATE = "state";
    public static final String AUTHENTICATION_TYPE = "Azure Authentication";
    public static final String CLIENT_SESSION_ID = "clientSessionId";
    public static final String AUTH_CONTEXT_ID = "authContextId";
    public static final String TEXT_FIELD_TYPE = "Text";
    public static final String CALLER_URI = "caller_uri";
    public static final String X_KRISTA_ACCESS_TOKEN = "X-Krista-Access-Token";
}
