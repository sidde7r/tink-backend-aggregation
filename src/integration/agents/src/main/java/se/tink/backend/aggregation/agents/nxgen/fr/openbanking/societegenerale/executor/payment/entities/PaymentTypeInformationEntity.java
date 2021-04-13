package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.executor.payment.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@Getter
public class PaymentTypeInformationEntity {
    private String serviceLevel;
    private String localInstrument;
}
