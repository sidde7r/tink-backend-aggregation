package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagErrorHandler;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.TppMessage;

public class TargobankErrorHandler extends BankverlagErrorHandler {

    private static final TppMessage PSU_CREDENTIALS_INVALID =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code("PSU_CREDENTIALS_INVALID")
                    .text("9050:***Die eingegebenen Daten sind falsch.***")
                    .build();

    @Override
    protected AgentError handleUsernamePasswordErrors(ErrorResponse errorResponse) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(PSU_CREDENTIALS_INVALID)
                .test(errorResponse)) {
            return LoginError.INCORRECT_CREDENTIALS;
        }
        return null;
    }
}
