package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.creditcard.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardTransactionEntity extends TransactionEntity {
    @JsonProperty("coverReservationTransaction")
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean reservation;

    @JsonProperty("billedTransaction")
    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean billed;

    @JsonProperty("transactionEntryType")
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String entryType;

    @Override
    public Boolean isReservation() {
        return reservation;
    }

    public Boolean isBilled() {
        return billed;
    }

    public String getEntryType() {
        return entryType;
    }
}
