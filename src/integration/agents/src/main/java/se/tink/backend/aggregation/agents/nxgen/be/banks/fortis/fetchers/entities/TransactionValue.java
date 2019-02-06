package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionValue {
    private int numberPending;
    private boolean completeListFlag;
    private List<MovementsItem> movements;

    public int getNumberPending() {
        return numberPending;
    }

    public boolean isCompleteListFlag() {
        return completeListFlag;
    }

    public List<MovementsItem> getMovements() {
        return movements;
    }

    public Collection<Transaction> toTinkTransactions() {
        return movements
                .stream()
                .filter(transaction -> transaction.isValid())
                .map(MovementsItem::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
