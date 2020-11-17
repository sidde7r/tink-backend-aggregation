package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.IdentityDataV31Entity;
import se.tink.libraries.identitydata.IdentityData;

public class IdentityDataMapper {

    public IdentityData map(IdentityDataV31Entity identityDataV31Entity) {
        return IdentityData.builder()
                .setFullName(identityDataV31Entity.getName())
                .setDateOfBirth(null)
                .build();
    }
}
