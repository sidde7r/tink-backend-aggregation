package se.tink.backend.aggregation.agents.nxgen.uk.banks.revolut.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CounterpartEntity {
    private int amount;
    private String currency;
    private AccountEntity account;
}
