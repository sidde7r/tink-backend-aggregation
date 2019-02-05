package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.rpc.CrossKeyResponse;

public class UnexpectedFailureException extends IllegalArgumentException {
    public UnexpectedFailureException(CrossKeyResponse response, String message) {
        super(message + " with errors: " + response.getStatus().getErrors());
    }
}
