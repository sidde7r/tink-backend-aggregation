package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.executor.payment.enums.BerlinGroupPaymentStatus;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@NoArgsConstructor
@AllArgsConstructor
public class PaymentStatusEntity {

    private String paymentInformationStatus;

    public BerlinGroupPaymentStatus getPaymentStatus() {
        return BerlinGroupPaymentStatus.fromString(paymentInformationStatus);
    }
}
