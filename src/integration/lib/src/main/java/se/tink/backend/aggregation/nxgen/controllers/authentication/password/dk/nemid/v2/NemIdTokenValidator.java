package se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.NEM_ID_PREFIX;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.dk.nemid.v2.NemIdConstantsV2.NemIdErrorCodes;

@Slf4j
@RequiredArgsConstructor
public class NemIdTokenValidator {

    private final NemIdTokenParser nemIdTokenParser;

    public void verifyTokenIsValid(String tokenBase64) {
        NemIdTokenStatus tokenStatus = nemIdTokenParser.extractNemIdTokenStatus(tokenBase64);
        if (containsIgnoreCase(tokenStatus.getCode(), "success")) {
            return;
        }
        throwInvalidTokenException(tokenBase64, tokenStatus);
    }

    public void throwInvalidTokenExceptionWithoutValidation(String tokenBase64) {
        NemIdTokenStatus tokenStatus = nemIdTokenParser.extractNemIdTokenStatus(tokenBase64);
        throwInvalidTokenException(tokenBase64, tokenStatus);
    }

    private void throwInvalidTokenException(String tokenBase64, NemIdTokenStatus tokenStatus) {
        if (containsIgnoreCase(tokenStatus.getMessage(), NemIdErrorCodes.REJECTED)) {
            throw NemIdError.REJECTED.exception();
        }
        if (containsIgnoreCase(tokenStatus.getMessage(), NemIdErrorCodes.TIMEOUT)) {
            throw NemIdError.TIMEOUT.exception();
        }
        log.error(
                "{} Unknown NemId token status error message: {}\n"
                        + "Token status code:{}\n"
                        + "Token base 64:{}",
                NEM_ID_PREFIX,
                tokenStatus.getMessage(),
                tokenStatus.getCode(),
                tokenBase64);
        throw LoginError.CREDENTIALS_VERIFICATION_ERROR.exception(
                NEM_ID_PREFIX + " Unknown NemId token error message: " + tokenStatus.getMessage());
    }

    private boolean containsIgnoreCase(String value, String containedElement) {
        return value.toLowerCase().contains(containedElement.toLowerCase());
    }
}
