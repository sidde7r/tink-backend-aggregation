package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities.Links;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SamlinkSessionStorage {
    private SessionStorage sessionStorage;

    public SamlinkSessionStorage(SessionStorage storage) {
        this.sessionStorage = storage;
    }

    public void storeAccessToken(String tokenType, String accessToken) {
        Preconditions.checkNotNull(Strings.emptyToNull(tokenType), "Token type is empty");
        Preconditions.checkNotNull(Strings.emptyToNull(accessToken), "Access token is empty");

        String formatted = String.format("%s %s", tokenType, accessToken);
        sessionStorage.put(SamlinkConstants.Storage.ACCESS_TOKEN, formatted);
    }

    public String getAccessToken() {
        return sessionStorage.get(SamlinkConstants.Storage.ACCESS_TOKEN);
    }

    public boolean hasAccessToken() {
        return sessionStorage.containsKey(SamlinkConstants.Storage.ACCESS_TOKEN);
    }

    public void storeServicesEndpoints(Links links) {
        sessionStorage.put(SamlinkConstants.Storage.SERVICES_ENDPOINTS, links);
    }

    public String getServicesEndpoint(String relKey) {
        Links serviceEndpoints =
                sessionStorage
                        .get(SamlinkConstants.Storage.SERVICES_ENDPOINTS, Links.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Samlink: Services endpoints has not been stored"));

        return serviceEndpoints.getLinkPath(relKey);
    }

    public void storeLoginName(String name) {
        sessionStorage.put(SamlinkConstants.Storage.LOGIN_NAME, name);
    }

    public String getLoginName() {
        return sessionStorage.get(SamlinkConstants.Storage.LOGIN_NAME);
    }
}
