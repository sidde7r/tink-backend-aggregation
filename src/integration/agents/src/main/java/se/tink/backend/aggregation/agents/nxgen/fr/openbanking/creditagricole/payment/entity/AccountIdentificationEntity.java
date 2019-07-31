package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.entity;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AccountIdentificationEntity {
    @JsonProperty("iban")
    private String iban = null;

    @JsonProperty("currency")
    private String currency = null;

    public AccountIdentificationEntity() {}

    private AccountIdentificationEntity(String iban, String currency) {
        this.iban = iban;
        this.currency = currency;
    }

    public static AccountIdentificationEntityBuilder builder() {
        return new AccountIdentificationEntityBuilder();
    }

    public AccountIdentificationEntity iban(String iban) {
        this.iban = iban;
        return this;
    }

    public String getIban() {
        return iban;
    }

    public void setIban(String iban) {
        this.iban = iban;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public static class AccountIdentificationEntityBuilder {

        private String iban;
        private String currency;

        AccountIdentificationEntityBuilder() {}

        public AccountIdentificationEntityBuilder iban(String iban) {
            this.iban = iban;
            return this;
        }

        public AccountIdentificationEntityBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public AccountIdentificationEntity build() {
            return new AccountIdentificationEntity(iban, currency);
        }

        public String toString() {
            return "AccountIdentificationEntity.AccountIdentificationEntityBuilder(iban="
                    + this.iban
                    + ", currency="
                    + this.currency
                    + ")";
        }
    }
}
