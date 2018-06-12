package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateUtils;

@JsonObject
public class AccountTransactionEntity {
    private String id;
    private String name;
    private String extendedName;
    private String transactionDate;
    private String valueDate;
    private AmountEntity amount;
    private ConceptEntity concept;

    @JsonIgnore
    public Transaction toTransaction() {
        return Transaction.builder()
                .setAmount(amount.getTinkAmount())
                .setDate(DateUtils.parseDate(transactionDate))
                .setDescription(name)
                .build();
    }

    public ConceptEntity getConcept() {
        return concept;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExtendedName() {
        return extendedName;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getValueDate() {
        return valueDate;
    }

    public AmountEntity getAmount() {
        return amount;
    }
}
