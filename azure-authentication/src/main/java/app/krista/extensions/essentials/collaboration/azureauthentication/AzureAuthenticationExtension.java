package app.krista.extensions.essentials.collaboration.azureauthentication;

import app.krista.extension.impl.anno.Domain;
import app.krista.extension.impl.anno.Extension;
import app.krista.extension.impl.anno.StaticResource;
@Domain(id = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7",
        name = "Authentication",
        ecosystemId = "catEntryEcosystem_d3b05047-07b0-4b06-95a3-9fb8f7f608d9",
        ecosystemName = "Krista",
        ecosystemVersion = "3e7e09ed-688f-41fa-ab7c-ff879e750011")
@Extension(implementingDomainIds = "catEntryDomain_db053e8f-a194-4dde-aa6f-701ef7a6b3a7", jaxrsId = "authn", requireWorkspaceAdminRights = true)
@StaticResource(path = "docs", file = "docs")
public class AzureAuthenticationExtension {
}
