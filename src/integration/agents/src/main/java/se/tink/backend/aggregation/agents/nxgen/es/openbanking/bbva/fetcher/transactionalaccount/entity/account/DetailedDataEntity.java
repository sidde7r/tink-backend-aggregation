package se.tink.backend.aggregation.agents.nxgen.es.openbanking.bbva.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailedDataEntity {

    private AccountEntity account;

    public AccountEntity getAccount() {
        return account;
    }
}