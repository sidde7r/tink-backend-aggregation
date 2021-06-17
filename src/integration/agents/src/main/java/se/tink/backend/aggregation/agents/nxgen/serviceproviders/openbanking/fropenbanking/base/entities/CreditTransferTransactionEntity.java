package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Builder
public class CreditTransferTransactionEntity {

    private PaymentIdEntity paymentId;

    @JsonProperty("instructedAmount")
    private AmountEntity amount;

    private List<String> remittanceInformation;
}
