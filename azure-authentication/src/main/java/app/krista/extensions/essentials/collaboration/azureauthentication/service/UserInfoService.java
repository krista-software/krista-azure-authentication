package app.krista.extensions.essentials.collaboration.azureauthentication.service;

import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.jvnet.hk2.annotations.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * Service class dealing with user information operations.
 */
@Service
public class UserInfoService implements UserInfoServiceInterface {
    /**
     * Retrieves a user's email address from an access token.
     *
     * @param accessTokenFuture a CompletableFuture of String representing the access token
     * @return a String representing the user's email
     */
    @Override
    public String getEmail(CompletableFuture<String> accessTokenFuture) {
        GraphServiceClient<Request> graphClient = GraphServiceClient
                .builder()
                .authenticationProvider(url -> accessTokenFuture)
                .buildClient();
        return Objects.requireNonNull(graphClient
                .me()
                .buildRequest()
                .get())
                .userPrincipalName;
    }
}
