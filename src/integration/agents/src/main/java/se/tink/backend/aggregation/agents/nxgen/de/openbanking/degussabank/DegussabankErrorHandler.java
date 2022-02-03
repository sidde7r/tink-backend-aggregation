package se.tink.backend.aggregation.agents.nxgen.de.openbanking.degussabank;

import java.util.Optional;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.BankverlagErrorHandler;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.berlingroup.error.TppMessage;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

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
    protected Optional<RuntimeException> handleUsernamePasswordErrors(
            ErrorResponse errorResponse, HttpResponseException httpResponseException) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(PSU_CREDENTIALS_INVALID)
                .test(errorResponse)) {
            return Optional.of(LoginError.INCORRECT_CREDENTIALS.exception(httpResponseException));
        }
        return Optional.empty();
    }

    @Override
    protected Optional<RuntimeException> handleOtpErrors(
            ErrorResponse errorResponse, HttpResponseException httpResponseException) {
        if (ErrorResponse.anyTppMessageMatchesPredicate(OTP_INVALID).test(errorResponse)) {
            return Optional.of(
                    LoginError.INCORRECT_CHALLENGE_RESPONSE.exception(httpResponseException));
        }
        return Optional.empty();
    }
}
