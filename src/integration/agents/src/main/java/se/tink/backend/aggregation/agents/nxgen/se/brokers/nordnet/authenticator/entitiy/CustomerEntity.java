package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.entitiy;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.entity.CustomerBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

@JsonObject
public class CustomerEntity extends CustomerBaseEntity {

    public IdentityData toTinkIdentity() {
        return SeIdentityData.of(getFirstName(), getLastName(), getNationalIdNumber());
    }
}
