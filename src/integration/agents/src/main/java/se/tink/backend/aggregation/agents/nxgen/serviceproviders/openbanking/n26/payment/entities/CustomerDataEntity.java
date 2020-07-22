package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.payment.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class CustomerDataEntity {

    private List<String> legalNames;
}
