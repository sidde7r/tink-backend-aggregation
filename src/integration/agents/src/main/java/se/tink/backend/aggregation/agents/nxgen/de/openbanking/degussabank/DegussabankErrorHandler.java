package se.tink.backend.aggregation.agents.nxgen.de.openbanking.degussabank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagErrorHandler;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.TppMessage;

public class DegussabankErrorHandler extends BankverlagErrorHandler {

    private static final TppMessage PSU_CREDENTIALS_INVALID =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code("FORMAT_ERROR")
                    .text("Fehler beim Registrieren der Nutzerdaten")
                    .build();
    private static final TppMessage OTP_INVALID =
            TppMessage.builder()
                    .category(TppMessage.ERROR)
                    .code("FORMAT_ERROR")
                    .text("9050:Nachricht teilweise fehlerhaft.\\n9941:TAN ungültig.\\n")
                    .build();

    @Override
    protected Optional<AgentError> handleUsernamePasswordErrors(ErrorResponse errorResponse) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(PSU_CREDENTIALS_INVALID)
                .test(errorResponse)) {
            return Optional.of(LoginError.INCORRECT_CREDENTIALS);
        }
        return Optional.empty();
    }

    @Override
    protected Optional<AgentError> handleOtpErrors(ErrorResponse errorResponse) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(OTP_INVALID).test(errorResponse)) {
            return Optional.of(LoginError.INCORRECT_CHALLENGE_RESPONSE);
        }
        return Optional.empty();
    }
}
