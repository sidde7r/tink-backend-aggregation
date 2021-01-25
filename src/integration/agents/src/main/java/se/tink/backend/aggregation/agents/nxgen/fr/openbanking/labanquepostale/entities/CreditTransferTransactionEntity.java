package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities.PaymentIdEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class CreditTransferTransactionEntity {

    private PaymentIdEntity paymentId;

    private String requestedExecutionDate;

    @JsonProperty("instructedAmount")
    private AmountEntity amount;

    private RemittanceInformationEntity remittanceInformation;

    private BeneficiaryEntity beneficiary;
}
