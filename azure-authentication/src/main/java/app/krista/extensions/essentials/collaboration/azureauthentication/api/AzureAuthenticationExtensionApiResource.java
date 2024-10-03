package app.krista.extensions.essentials.collaboration.azureauthentication.api;

import app.krista.extensions.essentials.collaboration.azureauthentication.service.UserInfoService;
import app.krista.extensions.essentials.collaboration.azureauthentication.service.auth.AzureOAuthService;
import app.krista.extensions.essentials.collaboration.azureauthentication.store.AzureAuthenticationTokenStore;
import com.github.scribejava.core.model.OAuth2AccessToken;

import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.github.scribejava.core.model.OAuthConstants.CODE;
import static com.github.scribejava.core.model.OAuthConstants.STATE;

@Path("/")
public class AzureAuthenticationExtensionApiResource {
    private final AzureOAuthService oAuth20Service;
    private final UserInfoService userInfoService;
    private final AzureAuthenticationTokenStore tokenStore;

    @Inject
    public AzureAuthenticationExtensionApiResource(AzureOAuthService oAuth20Service, UserInfoService userInfoService, AzureAuthenticationTokenStore tokenStore) {
        this.oAuth20Service = oAuth20Service;
        this.userInfoService = userInfoService;
        this.tokenStore = tokenStore;
    }

    @POST
    @Path("/login")
    @Consumes(MediaType.APPLICATION_JSON)
    @Produces(MediaType.APPLICATION_JSON)
    public String login() {
        System.out.println("login hit");
        return oAuth20Service.getAuthorizationURL();
    }

    @GET
    @Path("/callback")
    public String getCallbackResponse(@QueryParam(CODE) String code, @QueryParam(STATE) String state) {
        System.out.println("callback called");
        try {
            OAuth2AccessToken accessToken = oAuth20Service.getOAuth20Service().getAccessToken(code);
            String refreshToken = accessToken.getRefreshToken();
            System.out.println("refreshToken=" + refreshToken);
            CompletableFuture<String> accessTokenFuture = CompletableFuture.completedFuture(accessToken.getAccessToken());
            String email = userInfoService.getEmail(accessTokenFuture);
            tokenStore.saveRefreshToken(email, refreshToken);
            return "authenticated successfully";
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
    }
}
