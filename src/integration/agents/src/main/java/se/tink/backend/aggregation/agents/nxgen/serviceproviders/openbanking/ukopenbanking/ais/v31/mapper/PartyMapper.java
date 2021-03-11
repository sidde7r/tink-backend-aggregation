package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.libraries.identitydata.IdentityData;

public class PartyMapper {

    public static IdentityData toIdentityData(PartyV31Entity party) {
        return IdentityData.builder().setFullName(party.getName()).setDateOfBirth(null).build();
    }
}
