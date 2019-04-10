package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.bec.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class LinksEntity {

    private LinkDetailsEntity balances;
    private LinkDetailsEntity transactions;
    private LinkDetailsEntity account;
}
