package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountNumberEntity {

    private String formatted;

    private String value;

    public String getValue() {
        return value;
    }
}
