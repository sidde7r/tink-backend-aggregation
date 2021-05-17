package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.function.BiFunction;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;

@JsonObject
public class FailuresEntity {
    @JsonIgnore
    private static final GenericTypeMapper<BiFunction<String, Throwable, PaymentException>, String>
            errorCodeToPaymentExceptionMapper =
                    GenericTypeMapper
                            .<BiFunction<String, Throwable, PaymentException>, String>
                                    genericBuilder()
                            .put(
                                    (msg, cause) -> new PaymentValidationException(msg, cause),
                                    "error.validation")
                            .put(
                                    (msg, cause) -> new PaymentAuthenticationException(msg, cause),
                                    "error.apikey.missing",
                                    "error.token",
                                    "error.token.invalid",
                                    "error.token.expired")
                            .put(
                                    (msg, cause) -> new PaymentAuthorizationException(msg, cause),
                                    "error.resource.denied")
                            .build();

    private String code;
    private String description;
    private String path;
    private String type;

    @JsonIgnore
    public PaymentException buildRelevantException(Throwable cause) {
        if ("error.validation".equals(code)) {
            if (!Strings.isNullOrEmpty(path)) {
                if (path.startsWith("creditor")) {
                    return new CreditorValidationException(description, path, cause);
                } else if (path.startsWith("debtor")) {
                    return new DebtorValidationException(description, path, cause);
                }
                return new PaymentValidationException(description, path, cause);
            }
        }

        return errorCodeToPaymentExceptionMapper
                .translate(code)
                .map(e -> e.apply(description, cause))
                .orElseGet(() -> new PaymentException(this.toString(), cause));
    }

    @JsonIgnore
    public boolean isBankSideFailure() {
        return "error.server".equalsIgnoreCase(code);
    }

    @JsonIgnore
    public boolean isConsentNotFound() {
        return NordeaBaseConstants.ErrorMessages.CONSENT_NOT_FOUND.equalsIgnoreCase(description);
    }

    @JsonIgnore
    public boolean isFetchCertificateFailure() {
        return NordeaBaseConstants.ErrorMessages.CERTIFICATE_FETCH_FAILED.equalsIgnoreCase(
                description);
    }

    @JsonIgnore
    public boolean isRefreshTokenInvalid() {
        return NordeaBaseConstants.ErrorMessages.TOKEN_INVALID.equalsIgnoreCase(description);
    }

    @JsonIgnore
    @Override
    public String toString() {
        return "FailuresEntity[code : "
                + code
                + " description : "
                + description
                + " path : "
                + path
                + " type : "
                + type;
    }
}
