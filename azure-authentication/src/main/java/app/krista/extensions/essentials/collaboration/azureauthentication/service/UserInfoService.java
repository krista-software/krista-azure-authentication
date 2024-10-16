package app.krista.extensions.essentials.collaboration.azureauthentication.service;

import com.microsoft.graph.requests.GraphServiceClient;
import okhttp3.Request;
import org.jvnet.hk2.annotations.Service;

import java.util.Objects;
import java.util.concurrent.CompletableFuture;

@Service
public class UserInfoService {
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
