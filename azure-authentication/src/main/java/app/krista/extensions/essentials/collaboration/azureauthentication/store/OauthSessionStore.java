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

package app.krista.extensions.essentials.collaboration.azureauthentication.store;

import app.krista.extensions.util.KeyValueStore;
import app.krista.model.field.util.GsonJsonMapper;
import app.krista.model.field.util.JavaTypes;
import app.krista.omni.sdk.spi.util.GsonJsonMapperImpl;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Map;
import java.util.UUID;

@Service
public class OauthSessionStore {
    public static final String OAUTH_KEY = "OAuthKey";
    private final KeyValueStore keyValueStore;
    private final GsonJsonMapper gsonJsonMapper;

    @Inject
    public OauthSessionStore(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
        this.gsonJsonMapper = GsonJsonMapperImpl.getInstance();
    }

    private Object get(String key) {
        return keyValueStore.get(key);
    }

    public String getRefreshToken(String email) {
        return (String) this.get(getRefreshTokenKey(email));
    }

    private String getAccessTokenKey(String email) {
        return "access_token_" + email;
    }

    private String getRefreshTokenKey(String email) {
        return "refresh_token_" + email;
    }

    private void remove(String key) {
        System.out.println("Removing Key : " + key);
        keyValueStore.remove(key);
    }

    public void saveAccessToken(String email, String refreshToken) {
        this.put(getAccessTokenKey(email), refreshToken);
    }

    public void saveRefreshToken(String email, String refreshToken) {
        this.put(getRefreshTokenKey(email), refreshToken);
    }

    private void put(String key, Object value) {
        keyValueStore.put(key, value);
    }

    public void saveSessionId(String accountId, String sessionId) {
        keyValueStore.put(getSessionKey(accountId), sessionId);
        keyValueStore.put(getSessionKey(sessionId), accountId);
    }

    private String getSessionKey(String accountId) {
        return "session_" + accountId;
    }

    public String getSessionId(String accountId) {
        return (String) keyValueStore.get(getSessionKey(accountId));
    }

    public void saveInvokerAttributes(Map<String, String> invokerAttributes) {
        keyValueStore.put(OAUTH_KEY, gsonJsonMapper.toString(invokerAttributes));
    }

    public Map<String, String> getInvokerAttributes() {
        return gsonJsonMapper.fromString(keyValueStore.get(OAUTH_KEY).toString(), JavaTypes.getTypedClass(Map.class, String.class, String.class));
    }

    public String storeAuthCredentials(Map<String, String> oauthAttributes) {
        String authContextId = UUID.randomUUID().toString();
        keyValueStore.put(authContextId, gsonJsonMapper.toString(oauthAttributes));
        return authContextId;
    }

    public Map<String, String> getOAuthAttributes(String authContextId) {
        return gsonJsonMapper.fromString(keyValueStore.get(authContextId).toString(), JavaTypes.getTypedClass(Map.class, String.class, String.class));
    }

    public void removeOAuthAttributes(String authContextId) {
        if (authContextId == null) {
            return;
        }
        System.out.println("Initialised to remove auth context Id : " + authContextId);
        this.remove(authContextId);
    }
}
