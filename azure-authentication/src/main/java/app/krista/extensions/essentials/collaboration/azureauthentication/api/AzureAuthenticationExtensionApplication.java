package app.krista.extensions.essentials.collaboration.azureauthentication.api;

import app.krista.extensions.essentials.collaboration.azureauthentication.service.CorsFilter;
import org.jvnet.hk2.annotations.ContractsProvided;
import org.jvnet.hk2.annotations.Service;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import java.util.Set;

@Service
@ContractsProvided(Application.class)
@ApplicationPath("/")
public class AzureAuthenticationExtensionApplication extends Application {
    @Override
    public Set<Class<?>> getClasses() {
        return Set.of(AzureAuthenticationExtensionApiResource.class, CorsFilter.class);
    }
}
