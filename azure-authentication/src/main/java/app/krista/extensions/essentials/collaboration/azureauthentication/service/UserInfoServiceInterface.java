package app.krista.extensions.essentials.collaboration.azureauthentication.service;

import java.util.concurrent.CompletableFuture;

public interface UserInfoServiceInterface {
    /**
     * Retrieves a user's email address from an access token.
     *
     * @param accessTokenFuture a CompletableFuture of String representing the access token
     * @return a String representing the user's email
     */
    String getEmail(CompletableFuture<String> accessTokenFuture);
}
