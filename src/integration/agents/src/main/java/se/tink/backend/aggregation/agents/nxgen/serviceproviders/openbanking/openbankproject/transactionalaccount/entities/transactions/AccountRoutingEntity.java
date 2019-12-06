package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountRoutingEntity {

    private String scheme;
    private String address;

    public String getScheme() {
        return scheme;
    }

    public String getAddress() {
        return address;
    }
}
