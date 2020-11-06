package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator;

import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;

public class PewCodeVerifier {
    public static void checkPewCode(Object pewCode) {
        if (null == pewCode) {
            return;
        }
        if (!(pewCode instanceof String)) {
            return;
        }
        if ("PEW0500".equals(pewCode)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }
    }
}
