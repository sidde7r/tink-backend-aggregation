package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Date;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.entity.transaction.TransactionAccountInfoEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.entity.transaction.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sdc.fetcher.transactionalaccount.entity.transaction.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateFormat;

@JsonObject
public class TransactionsResponse implements PaginatorResponse {

    @JsonIgnore private String providerMarket;
    @JsonIgnore private LocalDate fromDate;
    @JsonIgnore private LocalDate toDate;

    private TransactionAccountInfoEntity account;
    private Transactions transactions;

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return Stream.concat(
                        transactions.getBooked().stream()
                                .filter(te -> isWithinRequestedDateRange(te, fromDate, toDate))
                                .map(te -> te.toBookedTinkTransaction(providerMarket)),
                        transactions.getPending().stream()
                                .filter(te -> isWithinRequestedDateRange(te, fromDate, toDate))
                                .map(te -> te.toPendingTinkTransaction(providerMarket)))
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }

    public TransactionsResponse setProviderMarket(String providerMarket) {
        this.providerMarket = providerMarket;
        return this;
    }

    public TransactionsResponse setFromDate(Date fromDate) {
        this.fromDate = DateFormat.convertToLocalDateViaInstant(fromDate);
        return this;
    }

    public TransactionsResponse setToDate(Date toDate) {
        this.toDate = DateFormat.convertToLocalDateViaInstant(toDate);
        return this;
    }

    private boolean isWithinRequestedDateRange(
            TransactionEntity transaction, LocalDate fromDate, LocalDate toDate) {
        return !transaction.getBookingDate().isBefore(fromDate)
                && !transaction.getBookingDate().isAfter(toDate);
    }
}
