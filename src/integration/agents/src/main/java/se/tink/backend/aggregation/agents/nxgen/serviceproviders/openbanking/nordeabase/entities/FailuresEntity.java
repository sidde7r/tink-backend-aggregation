package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableMap;
import java.util.function.BiFunction;
import se.tink.backend.aggregation.agents.exceptions.payment.*;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FailuresEntity {
    private String code;
    private String description;
    private String path;
    private String type;

    @JsonIgnore
    private static final ImmutableMap<String, BiFunction<String, Throwable, PaymentException>>
            errorCodeToPaymentException =
                    ImmutableMap.<String, BiFunction<String, Throwable, PaymentException>>builder()
                            .put(
                                    "error.validation",
                                    (description, cause) ->
                                            new PaymentValidationException(description, cause))
                            .put(
                                    "error.apikey.missing",
                                    (description, cause) ->
                                            new PaymentAuthenticationException(description, cause))
                            .put(
                                    "error.token",
                                    (description, cause) ->
                                            new PaymentAuthenticationException(description, cause))
                            .put(
                                    "error.token.invalid",
                                    (description, cause) ->
                                            new PaymentAuthenticationException(description, cause))
                            .put(
                                    "error.token.expired",
                                    (description, cause) ->
                                            new PaymentAuthenticationException(description, cause))
                            .put(
                                    "error.resource.denied",
                                    (description, cause) ->
                                            new PaymentAuthorizationException(description, cause))
                            .build();

    @JsonIgnore
    public PaymentException buildRelevantException(Throwable cause) {
        if (code == "error.validation") {
            if (path != null) {
                if (path.startsWith("creditor")) {
                    return new CreditorValidationException(description, path, cause);
                } else if (path.startsWith("debtor")) {
                    return new DebtorValidationException(description, path, cause);
                } else {
                    return new PaymentValidationException(description, path, cause);
                }
            }
        } else if (errorCodeToPaymentException.get(code) == null) {
            return new PaymentException(this.toString(), cause);
        }
        return errorCodeToPaymentException.get(code).apply(description, cause);
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
