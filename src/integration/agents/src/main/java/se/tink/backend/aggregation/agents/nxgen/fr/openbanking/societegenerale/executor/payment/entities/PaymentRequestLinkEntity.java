package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class PaymentRequestLinkEntity {

    private ConsentApprovalEntity consentApproval;
}
