package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;

public class BankIdAutostartTokenResponse extends CrossKeyResponse {
    public String getAutoStartToken() {
        return autoStartToken;
    }

    private String autoStartToken;

}
