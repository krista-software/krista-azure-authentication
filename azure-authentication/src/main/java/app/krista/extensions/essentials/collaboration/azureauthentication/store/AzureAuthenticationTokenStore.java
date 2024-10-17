package app.krista.extensions.essentials.collaboration.azureauthentication.store;

import app.krista.extensions.util.KeyValueStore;
import org.jvnet.hk2.annotations.Service;

import javax.inject.Inject;
import java.util.Set;

@Service
public class AzureAuthenticationTokenStore {
    private final KeyValueStore keyValueStore;

    @Inject
    public AzureAuthenticationTokenStore(KeyValueStore keyValueStore) {
        this.keyValueStore = keyValueStore;
    }

    private Object get(String key) {
        return keyValueStore.get(key);
    }

    public String getAccessToken(String email) {
        return (String) this.get(getAccessTokenKey(email));
    }

    private String getAccessTokenKey(String email) {
        return "access_token_" + email;
    }

    public void removeAccessToken(String email) {
        this.remove(getAccessTokenKey(email));
    }

    private void remove(String key) {
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

    private void put(String key, Object value) {
        keyValueStore.put(key, value);
    }

    public Set<String> getKeys() {
        return keyValueStore.getKeys();
    }

    public void saveSessionId(String accountId, String sessionId) {
        keyValueStore.put(getSessionKey(accountId), sessionId);
    }

    private String getSessionKey(String accountId) {
        return "session_" + accountId;
    }

    public String getSessionId(String accountId) {
        return (String) keyValueStore.get(getSessionKey(accountId));
    }
}
