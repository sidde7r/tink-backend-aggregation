package se.tink.backend.aggregation.agents.nxgen.at.banks.ing;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.agents.nxgen.at.banks.ing.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IngAtSessionStorage {
    private final SessionStorage sessionStorage;

    public IngAtSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }


    public Optional<WebLoginResponse> getWebLoginResponse() {
        return sessionStorage.get(IngAtConstants.Storage.WEB_LOGIN_RESPONSE.name(), WebLoginResponse.class);
    }

    public void setWebLoginResponse(@Nonnull final WebLoginResponse webLoginResponse) {
        Preconditions.checkNotNull(webLoginResponse);
        sessionStorage.put(IngAtConstants.Storage.WEB_LOGIN_RESPONSE.name(), webLoginResponse);
    }

    public void setCurrentUrl(String currentUrl) {
        System.out.println("#### setCurrentUrl: Current URL=" + currentUrl); // FIXME!
        sessionStorage.put(IngAtConstants.Storage.CURRENT_URL.name(), currentUrl);
    }

    public Optional<String> getCurrentUrl() {
        final Optional<String> currentUrl = sessionStorage.get(IngAtConstants.Storage.CURRENT_URL.name(), String.class);
        System.out.println("#### getCurrentUrl: URL=" + currentUrl.orElse("null")); // FIXME!
        return currentUrl;
    }

    public void clear() {
        sessionStorage.clear();
    }
}

