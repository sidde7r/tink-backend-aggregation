package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class AccountEntity {
    private Double currentBalance;
    private String iban;
    private String number;
    private String currency;
    private String title;
    private String bic;
    private String type;

    public AccountEntity() {}

    public AccountEntity(
            final Double currentBalance,
            final String iban,
            final String number,
            final String currency,
            final String title,
            final String bic,
            final String type) {
        this.currentBalance = currentBalance;
        this.iban = iban;
        this.number = number;
        this.currency = currency;
        this.title = title;
        this.bic = bic;
        this.type = type;
    }

    public Double getCurrentBalance() {
        return currentBalance;
    }

    public String getIban() {
        return iban;
    }

    public String getNumber() {
        return number;
    }

    public String getCurrency() {
        return currency;
    }

    public String getTitle() {
        return title;
    }

    public String getBic() {
        return bic;
    }

    public String getType() {
        return type;
    }
}
