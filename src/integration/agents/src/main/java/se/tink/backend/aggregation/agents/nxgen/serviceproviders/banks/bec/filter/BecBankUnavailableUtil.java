package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.ErrorMessages.FUNCTION_NOT_AVAILABLE;

public class BecBankUnavailableUtil {
    public static boolean isBankUnavailableStatus(int status) {
        return status == 400 || status == 500;
    }

    public static boolean isBankUnavailableErrorMessage(String response) {
        return FUNCTION_NOT_AVAILABLE.stream().anyMatch(response::contains);
    }
}
