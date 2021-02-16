package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.BranchEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class AccountTransactionEntity {
    private String id;
    private ContractEntity contract;
    private ConceptEntity concept;
    private BranchEntity branch;
    private String extendedName;
    private OriginEntity origin;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date transactionDate;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    private Date valueDate;

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
                .setDate(transactionDate)
                .setDescription(createFullDescription())
                .build();
    }

    @JsonIgnore
    public String createFullDescription() {
        if (!Strings.isNullOrEmpty(humanExtendedConceptName)) {
            return humanConceptName + " " + humanExtendedConceptName;
        }
        return humanConceptName;
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

    public Date getTransactionDate() {
        return transactionDate;
    }

    public Date getValueDate() {
        return valueDate;
    }

    public AmountEntity getAmount() {
        return amount;
    }
}
