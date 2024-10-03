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

    public String getRefreshToken(String email) {
        return (String) this.get(getRefreshTokenKey(email));
    }

    private String getRefreshTokenKey(String email) {
        return "refresh_token_" + email;
    }

    public void removeRefreshToken(String email) {
        this.remove(getRefreshTokenKey(email));
    }

    private void remove(String key) {
        keyValueStore.remove(key);
    }

    public boolean doesRefreshTokenExist(String email) {
        return this.contains(getRefreshToken(email));
    }

    private boolean contains(String key) {
        return keyValueStore.contains(key);
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
}
