package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.entities;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TransferEntity {

    private String id;

    private String status;
}
