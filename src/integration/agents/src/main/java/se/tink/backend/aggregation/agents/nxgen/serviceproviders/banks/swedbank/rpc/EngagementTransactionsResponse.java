package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
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

    public TransactionAccountEntity getAccount() {
        return account;
    }

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public List<ReservedTransactionEntity> getReservedTransactions() {
        return reservedTransactions;
    }

    public int getUncategorizedExpenseTransactions() {
        return uncategorizedExpenseTransactions;
    }

    public int getUncategorizedIncomeTransactions() {
        return uncategorizedIncomeTransactions;
    }

    public String getUncategorizedSubcategoryId() {
        return uncategorizedSubcategoryId;
    }

    public int getUncategorizedSortOfReceivers() {
        return uncategorizedSortOfReceivers;
    }

    public boolean isMoreTransactionsAvailable() {
        return moreTransactionsAvailable;
    }

    public int getNumberOfTransactions() {
        return numberOfTransactions;
    }

    public int getNumberOfReservedTransactions() {
        return numberOfReservedTransactions;
    }

    public int getNumberOfBankGiroPrognosisTransactions() {
        return numberOfBankGiroPrognosisTransactions;
    }

    public LinksEntity getLinks() {
        return links;
    }

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
        if (!moreTransactionsAvailable) {
            return Optional.of(false);
        }

        if (links == null) {
            return Optional.of(false);
        }

        if (links.getNext() == null) {
            return Optional.of(false);
        }

        if (!links.getNext().isValid()) {
            return Optional.of(false);
        }

        return Optional.of(true);
    }

    @Override
    public LinkEntity nextKey() {
        return links != null ? links.getNext() : null;
    }
}
