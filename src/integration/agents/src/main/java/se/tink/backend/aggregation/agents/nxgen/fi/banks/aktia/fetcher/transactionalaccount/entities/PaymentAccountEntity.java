package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PaymentAccountEntity {
    private String id;
    private String name;
    private String iban;
    private double balance;
    private boolean defaultPaymentAccount;

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getIban() {
        return iban;
    }

    public double getBalance() {
        return balance;
    }

    public boolean isDefaultPaymentAccount() {
        return defaultPaymentAccount;
    }
}
