package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeEncValueTuple;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginatorResponse;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class TransactionsHistoryResponse extends HeaderResponse implements TransactionKeyPaginatorResponse<String> {
    private List<TransactionDto> transactions;
    private TypeEncValueTuple repositioningKey;
    private TypeValuePair accountBalance;
    private TypeValuePair accountCurrency;
    private TypeValuePair balanceIncludingReservations;
    private TypeValuePair balanceIncludinReservationAmountEur;
    private TypeValuePair reservationAmount;
    private TypeValuePair reservationIndicator;

    public List<TransactionDto> getTransactions() {
        return transactions;
    }

    public TypeEncValueTuple getRepositioningKey() {
        return repositioningKey;
    }

    public TypeValuePair getAccountBalance() {
        return accountBalance;
    }

    public TypeValuePair getAccountCurrency() {
        return accountCurrency;
    }

    public TypeValuePair getBalanceIncludingReservations() {
        return balanceIncludingReservations;
    }

    public TypeValuePair getBalanceIncludinReservationAmountEur() {
        return balanceIncludinReservationAmountEur;
    }

    public TypeValuePair getReservationAmount() {
        return reservationAmount;
    }

    public TypeValuePair getReservationIndicator() {
        return reservationIndicator;
    }

    @Override
    public Collection<Transaction> getTinkTransactions() {
        return transactions.stream()
                .map(TransactionDto::toTinkTransaction)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.of(!Strings.isNullOrEmpty(repositioningKey.getValue()));
    }

    @Override
    public String nextKey() {
        return SerializationUtils.serializeToString(repositioningKey);
    }
}
