package se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.base.Objects;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.creditcard.entities.CardInvoiceInfo;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.entities.HandelsbankenSEAccount;
import se.tink.backend.aggregation.agents.nxgen.se.banks.handelsbanken.fetcher.transactionalaccount.entities.HandelsbankenSETransaction;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.handelsbanken.fetcher.transactionalaccount.rpc.TransactionsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionsSEResponse extends TransactionsResponse implements PaginatorResponse {

    private HandelsbankenSEAccount account;
    private List<HandelsbankenSETransaction> transactions;
    private CardInvoiceInfo cardInvoiceInfo;

    public HandelsbankenSEAccount getAccount() {
        return account;
    }

    public CardInvoiceInfo getCardInvoiceInfo() {
        return cardInvoiceInfo;
    }

    @Override
    public List<Transaction> toTinkTransactions() {

        return this.transactions.stream()
                .map(HandelsbankenSETransaction::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return toTinkTransactions();
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.empty();
    }

    @JsonIgnore
    public Stream<HandelsbankenSETransaction> getTodaysTransactions() {
        LocalDate today = LocalDate.now();

        return Optional.ofNullable(transactions).orElseGet(Collections::emptyList).stream()
                .filter(transaction -> Objects.equal(today, transaction.dueDateAsLocalDate()));
    }
}
