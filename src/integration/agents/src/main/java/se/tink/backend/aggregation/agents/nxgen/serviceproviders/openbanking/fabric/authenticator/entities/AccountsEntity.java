package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountsEntity {
    private String iban;
    private String bban;
    private String currency;
    private String maskedPan;
    private String msisdn;
    private String pan;
    private String allPsd2;
    private String availableAccounts;
    private String balances;
}
