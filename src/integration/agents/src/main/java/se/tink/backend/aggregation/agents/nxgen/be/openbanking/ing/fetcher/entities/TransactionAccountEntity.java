package se.tink.backend.aggregation.agents.nxgen.be.openbanking.ing.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionAccountEntity {

    private String iban;
    private String bban;
    private String bin;
}