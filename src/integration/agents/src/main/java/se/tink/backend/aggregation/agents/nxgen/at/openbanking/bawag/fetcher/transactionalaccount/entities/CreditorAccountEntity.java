package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditorAccountEntity {
    private String bban;
    private String currency;
    private String iban;
    private String maskedPan;
    private String msisdn;
    private String pan;
}
