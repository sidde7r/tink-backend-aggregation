package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class OwnerEntity {

    private String firstName;

    private String lastName;

    private String name;

    public String getName() {
        return name;
    }
}
