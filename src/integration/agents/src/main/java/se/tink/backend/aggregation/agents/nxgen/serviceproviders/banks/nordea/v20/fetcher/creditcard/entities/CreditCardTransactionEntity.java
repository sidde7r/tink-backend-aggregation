package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardTransactionEntity {
    @JsonProperty("withdrawnCode")
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String withdrawnCode;

    @JsonProperty("purchasedate")
    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date date;

    @JsonProperty("purchaseText")
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String text;

    @JsonProperty("transactionAmount")
    private Amount amount;

    public String getWithdrawnCode() {
        return withdrawnCode;
    }

    public Date getDate() {
        return date;
    }

    public String getText() {
        return text;
    }

    public Amount getAmount() {
        return amount;
    }

    @JsonObject
    public static class Amount {
        @JsonProperty("amountPart")
        @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
        private Double value;

        @JsonProperty("currencyCode")
        @JsonDeserialize(using = NordeaHashMapDeserializer.class)
        private String currency;

        public Double getValue() {
            return value;
        }

        public String getCurrency() {
            return currency;
        }
    }
}
