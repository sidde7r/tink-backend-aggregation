package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.balance;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAccountDetailsEntity {

    private String iban;
    private String bban;
    private String currency;
}
