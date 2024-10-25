package app.krista.extensions.essentials.collaboration.azureauthentication.common;

import java.util.List;
import java.util.Map;

public class AuthenticationResponse {

    private final String clientSessionId;
    private final String name;
    private final String avatarUrl;
    private final String accountId;
    private final String kristaAccountId;
    private final String personId;
    private final List<String> roles;
    private final String inboxId;
    private final boolean isWorkspaceAdmin;
    private final boolean isApplianceManager;
    private final Map<String, String> identificationToken;
    private final Map<String, Object> extras;

    public AuthenticationResponse(String clientSessionId, String name, String avatarUrl, String accountId,
                                  String kristaAccountId, String personId, List<String> roles, String inboxId,
                                  boolean isWorkspaceAdmin, boolean isApplianceManager,
                                  Map<String, String> identificationToken, Map<String, Object> extras) {
        this.clientSessionId = clientSessionId;
        this.name = name;
        this.avatarUrl = avatarUrl;
        this.accountId = accountId;
        this.kristaAccountId = kristaAccountId;
        this.personId = personId;
        this.roles = roles;
        this.inboxId = inboxId;
        this.isWorkspaceAdmin = isWorkspaceAdmin;
        this.isApplianceManager = isApplianceManager;
        this.identificationToken = identificationToken;
        this.extras = extras;
    }

    public String getClientSessionId() {
        return clientSessionId;
    }

    public String getName() {
        return name;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getPersonId() {
        return personId;
    }

    public List<String> getRoles() {
        return roles;
    }

    public String getInboxId() {
        return inboxId;
    }

    public boolean isWorkspaceAdmin() {
        return isWorkspaceAdmin;
    }

    public boolean isApplianceManager() {
        return isApplianceManager;
    }

    public Map<String, String> getIdentificationToken() {
        return identificationToken;
    }

    public Map<String, Object> getExtras() {
        return extras;
    }

    public String getKristaAccountId() {
        return kristaAccountId;
    }

    @Override
    public String toString() {
        return "AuthenticationResponse{" +
                "clientSessionId='" + clientSessionId + '\'' +
                ", name='" + name + '\'' +
                ", avatarUrl='" + avatarUrl + '\'' +
                ", accountId='" + accountId + '\'' +
                ", kristaAccountId='" + kristaAccountId + '\'' +
                ", personId='" + personId + '\'' +
                ", roles=" + roles +
                ", inboxId='" + inboxId + '\'' +
                ", isWorkspaceAdmin=" + isWorkspaceAdmin +
                ", isApplianceManager=" + isApplianceManager +
                ", identificationToken=" + identificationToken +
                ", extras=" + extras +
                '}';
    }

}
