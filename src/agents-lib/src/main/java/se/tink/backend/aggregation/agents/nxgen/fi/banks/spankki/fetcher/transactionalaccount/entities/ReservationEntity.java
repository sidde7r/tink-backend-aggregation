package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ReservationEntity {
    private String accountId;

    public ReservationEntity setAccountId(String accountId) {
        this.accountId = accountId;
        return this;
    }
}
