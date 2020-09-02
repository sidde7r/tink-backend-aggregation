package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fropenbanking.base.entities;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@AllArgsConstructor
@NoArgsConstructor
public class InitiatingPartyEntity {

    private String name;
}
