package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v20.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import java.util.Optional;
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
        return Optional.ofNullable(transactionKey)
                .orElse(StringUtils.hashAsStringMD5(text + date.toString() + amount.toString()));
    }

    public String getText() {
        return text;
    }
}
