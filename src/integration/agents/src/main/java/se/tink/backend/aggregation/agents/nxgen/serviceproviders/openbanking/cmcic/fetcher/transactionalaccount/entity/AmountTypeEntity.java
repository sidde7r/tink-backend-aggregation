package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cmcic.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
@JsonInclude(Include.NON_NULL)
public class AmountTypeEntity {
    @JsonProperty("currency")
    private String currency = null;

    @JsonProperty("amount")
    private String amount = null;

    @JsonCreator
    private AmountTypeEntity(String currency, String amount) {
        this.currency = currency;
        this.amount = amount;
    }

    public static AmountTypeEntityBuilder builder() {
        return new AmountTypeEntityBuilder();
    }

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }

    public static class AmountTypeEntityBuilder {

        private String currency;
        private String amount;

        AmountTypeEntityBuilder() {}

        public AmountTypeEntityBuilder currency(String currency) {
            this.currency = currency;
            return this;
        }

        public AmountTypeEntityBuilder amount(String amount) {
            this.amount = amount;
            return this;
        }

        public AmountTypeEntity build() {
            return new AmountTypeEntity(currency, amount);
        }

        public String toString() {
            return "AmountTypeEntity.AmountTypeEntityBuilder(currency="
                    + this.currency
                    + ", amount="
                    + this.amount
                    + ")";
        }
    }
}
