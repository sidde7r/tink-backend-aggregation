package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class TransactionEntity {
    @JsonProperty("isCoverReservationTransaction")
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean reservation;
    @JsonProperty("transactionAmount")
    @JsonDeserialize(using = NordeaHashMapDeserializer.Double.class)
    private Double amount;
    @JsonProperty("transactionCounterpartyName")
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String counterPartyName;
    @JsonProperty("transactionCurrency")
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String currency;
    @JsonProperty("transactionDate")
    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date date;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String transactionKey;
    @JsonProperty("transactionText")
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String text;

    public Boolean isReservation() {
        return reservation;
    }

    public Double getAmount() {
        return amount;
    }

    public String getCounterPartyName() {
        return counterPartyName;
    }

    public String getCurrency() {
        return currency;
    }

    public Date getDate() {
        return date;
    }

    public String getTransactionKey() {
        if (transactionKey != null) {
            return transactionKey;
        }

        String transText = text != null ? text : counterPartyName;
        return StringUtils.hashAsStringMD5(Strings.nullToEmpty(transText) + date.toString() + amount.toString());
    }

    public String getText() {
        return text;
    }
}
