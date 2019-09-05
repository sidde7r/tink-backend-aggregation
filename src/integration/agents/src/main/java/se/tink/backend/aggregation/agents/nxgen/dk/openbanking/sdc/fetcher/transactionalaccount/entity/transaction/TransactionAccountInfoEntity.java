package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionAccountInfoEntity {

    private String iban;
    private String bban;
    private String currency;
}
