package se.tink.backend.aggregation.agents.nxgen.fi.brokers.nordnet.authenticator.entity;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.entity.CustomerBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.FiIdentityData;

@JsonObject
public class CustomerEntity extends CustomerBaseEntity {

    public IdentityData toTinkIdentity() {
        return FiIdentityData.of(getFirstName(), getLastName(), getNationalIdNumber());
    }
}
