package app.krista.extensions.essentials.collaboration.azureauthentication.store;

import app.krista.extensions.util.KeyValueStore;
import app.krista.model.field.util.GsonJsonMapper;
import app.krista.model.field.util.JavaTypes;
import app.krista.omni.sdk.spi.util.GsonJsonMapperImpl;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
public class OauthSessionStore {
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

    public String getAccessToken(String email) {
        return (String) this.get(getAccessTokenKey(email));
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

    public void removeAccessToken(String email) {
        this.remove(getAccessTokenKey(email));
    }

    private void remove(String key) {
        System.out.println("Removing Key : "+ key);
        keyValueStore.remove(key);
    }

    public boolean doesAccessTokenExist(String email) {
        return this.contains(getAccessToken(email));
    }

    private boolean contains(String key) {
        return keyValueStore.contains(key);
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

    public Set<String> getKeys() {
        return keyValueStore.getKeys();
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
        keyValueStore.put(toOauthKey(), gsonJsonMapper.toString(invokerAttributes));
    }

    public Map<String, String> getInvokerAttributes() {
        return gsonJsonMapper.fromString(keyValueStore.get(toOauthKey()).toString(), JavaTypes.getTypedClass(Map.class, String.class, String.class));
    }

    private String toOauthKey() {
        return "OAuthKey"; //NOSONAR
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
        if(authContextId == null){ return;}
        System.out.println("Initialised to remove auth context Id : "+ authContextId);
        this.remove(authContextId);
    }
}
