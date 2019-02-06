package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountEntity {
    private String id;
    private String iban;
    private String bic;
    private String name;
    private String primaryOwnerName;
    private double balance;
}
