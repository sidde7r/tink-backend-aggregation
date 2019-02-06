package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.fetchers.entities;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class UpcomingTransactionValue {
    private int numberPending;
    private boolean completeListFlag;
    private List<TransferItem> transfer;

    public int getNumberPending() {
        return numberPending;
    }

    public boolean isCompleteListFlag() {
        return completeListFlag;
    }

    public List<TransferItem> getMovements() {
        return transfer;
    }

    public Collection<UpcomingTransaction> toTinkTransactions() {
        return transfer.stream()
                .filter(transaction -> transaction.isValid())
                .map(TransferItem::toTinkTransaction)
                .collect(Collectors.toList());
    }
}
