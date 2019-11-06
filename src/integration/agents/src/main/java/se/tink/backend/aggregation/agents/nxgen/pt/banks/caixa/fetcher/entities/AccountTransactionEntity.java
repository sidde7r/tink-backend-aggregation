package se.tink.backend.aggregation.agents.nxgen.pt.banks.caixa.fetcher.entities;

import java.util.ArrayList;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public final class AccountTransactionEntity {

    private String operationStatus;

    private String customerMessage;

    private Integer operationId;

    private List<TransactionEntity> transactions = new ArrayList<>();

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }
}
