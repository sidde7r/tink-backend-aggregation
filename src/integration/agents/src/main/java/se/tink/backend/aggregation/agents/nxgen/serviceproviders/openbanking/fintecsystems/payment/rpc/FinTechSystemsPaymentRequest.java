package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintecsystems.payment.rpc;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import javax.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;

@Getter
@Setter
@JsonObject
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class FinTechSystemsPaymentRequest {
    String amount;
    String currencyId;
    String recipientHolder;
    String recipientIban;

    @Size(min = 5, max = 140)
    String purpose;

    String senderBic;
    String senderBankCode;
    String senderCountryId;
}
