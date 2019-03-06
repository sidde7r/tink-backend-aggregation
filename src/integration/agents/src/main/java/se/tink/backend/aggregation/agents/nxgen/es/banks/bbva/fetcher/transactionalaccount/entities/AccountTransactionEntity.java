package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BranchEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateUtils;

@JsonObject
public class AccountTransactionEntity {
    private String id;
    private ContractEntity contract;
    private ConceptEntity concept;
    private BranchEntity branch;
    private String extendedName;
    private OriginEntity origin;
    private String transactionDate;
    private String valueDate;
    private AmountEntity amount;
    private ExtendedDateEntity extendedDate;
    private CategoryEntity humanCategory;
    private CategoryEntity humanSubcategory;
    private String humanConceptName;
    private String humanExtendedConceptName;
    private DocumentEntity document;
    private String name;

    @JsonIgnore
    public Transaction toTransaction() {
        return Transaction.builder()
                .setAmount(amount.toTinkAmount())
                .setDate(DateUtils.parseDate(transactionDate))
                .setDescription(humanConceptName)
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
