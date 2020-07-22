package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InstructionsEntity {

    private List<TransferDestinationEntity> transferDestinations;
}
