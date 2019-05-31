package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RelationshipsEntity {

    private AccountLinkTypesEntity transactions;
    private AccountLinkTypesEntity fundsAvailability;
    private AccountLinkTypesEntity balance;
}
