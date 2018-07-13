package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.rpc.AlandsBankenResponse;

public class UnexpectedFailureException extends IllegalArgumentException {
    public UnexpectedFailureException(AlandsBankenResponse response, String message) {
        super(message + " with errors: " + response.getStatus().getErrors());
    }
}
