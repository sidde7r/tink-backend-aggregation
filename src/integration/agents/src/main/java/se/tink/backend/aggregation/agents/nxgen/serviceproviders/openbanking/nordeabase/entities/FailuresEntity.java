package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.function.BiFunction;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.payment.CreditorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.DebtorValidationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentAuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentValidationException;
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
                                    (description, cause) ->
                                            new PaymentValidationException(description, cause),
                                    "error.validation")
                            .put(
                                    (description, cause) ->
                                            new PaymentAuthenticationException(description, cause),
                                    "error.apikey.missing",
                                    "error.token",
                                    "error.token.invalid",
                                    "error.token.expired",
                                    "error.token.expired")
                            .put(
                                    (description, cause) ->
                                            new PaymentAuthorizationException(description, cause),
                                    "error.resource.denied")
                            .build();

    private String code;
    private String description;
    private String path;
    private String type;

    @JsonIgnore
    public PaymentException buildRelevantException(Throwable cause) {
        if (code == "error.validation") {
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
