package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.executor.payment.entities;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@AllArgsConstructor
@JsonObject
public class RemittanceInformationStructured {
    private String reference;
}
