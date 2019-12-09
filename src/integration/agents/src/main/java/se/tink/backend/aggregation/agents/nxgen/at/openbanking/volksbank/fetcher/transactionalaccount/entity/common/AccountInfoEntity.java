package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.entity.common;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountInfoEntity {

    private String iban;
    private String currency;
}
