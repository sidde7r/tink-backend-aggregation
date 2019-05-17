package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.fetcher.transactionalaccount.entity.transaction;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    private String type;
    private String id;
    private TransactionAttributesEntity attributes;
    private Links links;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(attributes.getBalanceAfterOperation().toTinkAmount())
                .setDate(attributes.getValueDate())
                .setDescription(attributes.getConcept())
                .build();
    }
}
