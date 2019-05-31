package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.payment.PaymentException;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ErrorEntity {
    @JsonProperty("_type")
    private String type;

    @JsonProperty("http_code")
    private int httpCode;

    private RequestEntity request;
    private List<FailuresEntity> failures;

    @JsonIgnore
    public void parseAndThrow(Throwable cause) throws PaymentException {
        if ("ExternalError".equals(type)) {
            if (failures.isEmpty()) {
                throw new IllegalStateException(
                        "Got an error without failures from Nordea.", cause);
            } else {
                PaymentException paymentExceptionCause =
                        failures.stream().findFirst().get().buildRelevantException(cause);
                for (FailuresEntity failure : failures.subList(1, failures.size())) {
                    paymentExceptionCause = failure.buildRelevantException(paymentExceptionCause);
                }
                throw paymentExceptionCause;
            }
        }
    }
}
