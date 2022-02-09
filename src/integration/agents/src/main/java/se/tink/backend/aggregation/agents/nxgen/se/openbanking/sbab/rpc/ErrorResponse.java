package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.ErrorMessage;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.SbabConstants.Errors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.i18n_aggregation.LocalizableKey;

@JsonObject
public class ErrorResponse {
    private static final Logger log = LoggerFactory.getLogger(ErrorResponse.class);

    @JsonProperty private String error;
    @JsonIgnore private boolean proxyError;

    public boolean isProxyError() {
        return proxyError;
    }

    @JsonIgnore
    public void handleErrors() {
        if (Errors.UNAUTHORIZED_CLIENT.equalsIgnoreCase(error)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }

        // SBAB seems to have a lot of issues with their KYC control. We're seeing users getting
        // this error and then 10 minutes later being able to refresh. Considered a bank side
        // failure, but logging as a warning so we can keep track of this issue.
        if (Errors.KYC_QUESTIONS_NOT_COMPLETED.equalsIgnoreCase(error)) {
            log.warn("Got kyc questions not completed error when refreshing access token.");
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    new LocalizableKey(ErrorMessage.KYC_MESSAGE));
        }
    }
}
