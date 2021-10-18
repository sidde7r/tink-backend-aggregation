package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
@Getter
public class EngagementTransactionsResponse implements TransactionKeyPaginatorResponse<LinkEntity> {
    private TransactionAccountEntity account;
    private List<TransactionEntity> transactions;
    private List<ReservedTransactionEntity> reservedTransactions;
    private int uncategorizedExpenseTransactions;
    private int uncategorizedIncomeTransactions;
    private String uncategorizedSubcategoryId;
    private int uncategorizedSortOfReceivers;
    private boolean moreTransactionsAvailable;
    private int numberOfTransactions;
    private int numberOfReservedTransactions;
    private int numberOfBankGiroPrognosisTransactions;
    private LinksEntity links;

    public List<Transaction> toTransactions() {
        if (transactions == null) {
            transactions = Collections.emptyList();
        }

        return transactions.stream()
                .map(TransactionEntity::toTinkTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    public List<Transaction> reservedTransactionsToTransactions() {
        if (reservedTransactions == null) {
            reservedTransactions = Collections.emptyList();
        }

        return reservedTransactions.stream()
                .map(ReservedTransactionEntity::toTinkTransaction)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return toTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return areLinksInvalid() ? Optional.of(false) : Optional.of(true);
    }

    @Override
    public LinkEntity nextKey() {
        return links != null ? links.getNext() : null;
    }

    private boolean areLinksInvalid() {
        return !moreTransactionsAvailable
                || links == null
                || links.getNext() == null
                || !links.getNext().isValid();
    }
}
