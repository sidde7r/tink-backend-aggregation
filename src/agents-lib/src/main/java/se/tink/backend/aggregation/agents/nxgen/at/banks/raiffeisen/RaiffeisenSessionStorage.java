package se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen;

import com.google.common.base.Preconditions;
import java.util.Optional;
import javax.annotation.Nonnull;
import se.tink.backend.aggregation.agents.nxgen.at.banks.raiffeisen.authenticator.rpc.WebLoginResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class RaiffeisenSessionStorage {
    private final SessionStorage sessionStorage;

    public RaiffeisenSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

     public Optional<WebLoginResponse> getWebLoginResponse() {
        return sessionStorage.get(RaiffeisenConstants.Storage.WEB_LOGIN_RESPONSE.name(), WebLoginResponse.class);
    }

    public void setWebLoginResponse(@Nonnull final WebLoginResponse loginResponse) {
        Preconditions.checkNotNull(loginResponse);
        sessionStorage.put(RaiffeisenConstants.Storage.WEB_LOGIN_RESPONSE.name(), loginResponse);
    }

    public void clear() {
        sessionStorage.clear();
    }
}
