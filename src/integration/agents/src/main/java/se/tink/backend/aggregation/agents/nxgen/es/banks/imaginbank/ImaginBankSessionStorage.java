package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.authenticator.rpc.LoginResponse;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class ImaginBankSessionStorage {
    private final SessionStorage sessionStorage;

    public ImaginBankSessionStorage(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    public void setLoginResponse(LoginResponse loginResponse) {
        Preconditions.checkNotNull(loginResponse);
        sessionStorage.put(
                ImaginBankConstants.Storage.LOGIN_RESPONSE,
                SerializationUtils.serializeToString(loginResponse));
    }

    public LoginResponse getLoginResponse() {
        return sessionStorage
                .get(ImaginBankConstants.Storage.LOGIN_RESPONSE, LoginResponse.class)
                .orElseThrow(() -> new IllegalStateException("Could not fetch login response."));
    }
}
