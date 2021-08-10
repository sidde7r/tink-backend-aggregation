package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.executor.payment.errors;

import java.util.List;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@JsonObject
public class PaymentErrorEntity {
    private List<ErrorEntity> errors;
}
