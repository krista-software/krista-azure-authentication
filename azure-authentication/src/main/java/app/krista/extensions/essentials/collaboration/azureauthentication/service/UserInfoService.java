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
