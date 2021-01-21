package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class PaymentTypeInformationEntity {

    private String serviceLevel;
    private String localInstrument;
    private String categoryPurpose;
}
