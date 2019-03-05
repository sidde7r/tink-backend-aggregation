package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;

public class LoginProvidersResponse extends CrossKeyResponse {
    private List<String> availableLoginProviders;

    @JsonIgnore
    public boolean canUseBankId() {
        return availableLoginProviders.contains(CrossKeyConstants.MultiFactorAuthentication.MOBILE_BANK_ID);
    }
}
