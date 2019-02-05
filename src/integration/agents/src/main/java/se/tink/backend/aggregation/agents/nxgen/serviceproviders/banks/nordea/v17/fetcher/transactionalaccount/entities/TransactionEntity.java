package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.util.Date;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.strings.StringUtils;

@JsonObject
public class TransactionEntity {
    @JsonDeserialize(using = NordeaHashMapDeserializer.Date.class)
    private Date transactionDate;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String transactionText;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String transactionType;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String transactionTypeCode;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String transactionCurrency;
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String transactionAmount;
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean isCoverReservationTransaction;

    public Date getTransactionDate() {
        return transactionDate;
    }

    public String getTransactionText() {
        return transactionText;
    }

    public String getTransactionType() {
        return transactionType;
    }

    public String getTransactionTypeCode() {
        return transactionTypeCode;
    }

    public String getTransactionCurrency() {
        return transactionCurrency;
    }

    public Double getTransactionAmount() {
        return AgentParsingUtils.parseAmount(transactionAmount);
    }

    public Boolean isCoverReservationTransaction() {
        return isCoverReservationTransaction;
    }

    public String getTransactionKey() {
        return StringUtils.hashAsStringMD5(transactionText + transactionDate.toString() + transactionAmount);
    }
}
