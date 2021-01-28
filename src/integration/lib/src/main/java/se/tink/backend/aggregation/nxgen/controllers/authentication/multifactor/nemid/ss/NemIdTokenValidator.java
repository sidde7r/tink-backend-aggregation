package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss;

import static se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NEM_ID_PREFIX;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import java.util.Map.Entry;
import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.NemIdConstants.NemIdErrorCodes;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.nemid.exception.NemIdError;

@Slf4j
@RequiredArgsConstructor
public class NemIdTokenValidator {

    private static final String LUNAR_ISSUER_BASE64 = "THVuYXI=";

    private static final ImmutableList<String> KNOWN_REQUEST_ISSUERS =
            ImmutableList.of(LUNAR_ISSUER_BASE64);

    private static final Map<String, AgentError> ERR_CODE_MAPPING =
            ImmutableMap.<String, AgentError>builder()
                    .put(NemIdErrorCodes.REJECTED, NemIdError.REJECTED)
                    .put(NemIdErrorCodes.INTERRUPTED, NemIdError.INTERRUPTED)
                    .put(NemIdErrorCodes.TIMEOUT, NemIdError.TIMEOUT)
                    .put(NemIdErrorCodes.TECHNICAL_ERROR, BankServiceError.BANK_SIDE_FAILURE)
                    .put(NemIdErrorCodes.NO_AGREEMENT, LoginError.NOT_CUSTOMER)
                    .put(NemIdErrorCodes.NEMID_LOCKED, NemIdError.NEMID_LOCKED)
                    .put(NemIdErrorCodes.NEMID_BLOCKED, NemIdError.NEMID_BLOCKED)
                    .put(NemIdErrorCodes.NEMID_PASSWORD_BLOCKED, NemIdError.NEMID_PASSWORD_BLOCKED)
                    .build();

    private final NemIdTokenParser nemIdTokenParser;

    public void verifyTokenIsValid(String tokenBase64) {
        NemIdTokenStatus tokenStatus = nemIdTokenParser.extractNemIdTokenStatus(tokenBase64);
        if (containsIgnoreCase(tokenStatus.getCode(), "success")) {
            return;
        }
        if (StringUtils.isBlank(tokenStatus.getCode())
                && StringUtils.isBlank(tokenStatus.getMessage())
                && KNOWN_REQUEST_ISSUERS.contains(tokenStatus.getRequestIssuer())) {
            return;
        }
        throwInvalidTokenException(tokenBase64, tokenStatus);
    }

    public void throwInvalidTokenExceptionWithoutValidation(String tokenBase64) {
        NemIdTokenStatus tokenStatus = nemIdTokenParser.extractNemIdTokenStatus(tokenBase64);
        throwInvalidTokenException(tokenBase64, tokenStatus);
    }

    private void throwInvalidTokenException(String tokenBase64, NemIdTokenStatus tokenStatus) {

        for (Entry<String, AgentError> entry : ERR_CODE_MAPPING.entrySet()) {
            if (containsIgnoreCase(tokenStatus.getMessage(), entry.getKey())) {
                throw entry.getValue().exception();
            }
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
