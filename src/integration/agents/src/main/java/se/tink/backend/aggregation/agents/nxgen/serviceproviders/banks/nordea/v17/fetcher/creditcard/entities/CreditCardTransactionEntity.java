package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.creditcard.entities;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.serializer.NordeaHashMapDeserializer;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v17.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CreditCardTransactionEntity extends TransactionEntity {
    @JsonDeserialize(using = NordeaHashMapDeserializer.class)
    private String transactionTypeName;

    @JsonDeserialize(using = NordeaHashMapDeserializer.Boolean.class)
    private Boolean billedTransaction;

    @Override
    public Boolean isCoverReservationTransaction() {
        return billedTransaction != null && !billedTransaction;
    }

    @Override
    public String getTransactionType() {
        return transactionTypeName;
    }
}
