package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.transactions;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DebtorAccountEntity {
    private String bban;
    private String currency;
    private String iban;
    private String maskedPan;
    private String msisdn;
    private String pan;
}
