package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc;

import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.OutputEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities.AccountTransactionsEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities.TransactionEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public final class GetTransactionsResponse {
    private OutputEntity output;

    public List<AggregationTransaction> getTransactions() {
        return Optional.ofNullable(output).map(OutputEntity::getAccountTransactions)
                .map(AccountTransactionsEntity::getTransactions).orElseGet(Collections::emptyList)
                .stream()
                .map(GetTransactionsResponse::toTinkTransaction)
                .collect(Collectors.toList());
    }

    private static AggregationTransaction toTinkTransaction(
            final TransactionEntity transactionsEntity) {
        final String description = extractDescription(transactionsEntity);
        final Date date = timestampToDate(transactionsEntity.getCreationTimeStamp());
        final ExactCurrencyAmount amount =
                ExactCurrencyAmount.of(
                        transactionsEntity.getAmount(), transactionsEntity.getCurrency());
        return Transaction.builder()
                .setDescription(description)
                .setDate(date)
                .setAmount(amount)
                .build();
    }

    private static String extractDescription(final TransactionEntity transactionsEntity) {
        final StringJoiner joiner = new StringJoiner(" | ");
        final String counterparty = transactionsEntity.getCounterparty();
        final String details = transactionsEntity.getDescription();
        if (!Strings.isNullOrEmpty(counterparty)) {
            joiner.add(counterparty);
        }
        if (!Strings.isNullOrEmpty(details)) {
            joiner.add(details);
        }
        return joiner.toString();
    }

    private static Date timestampToDate(final String creationTimeStamp) {
        final SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH.mm.ss.SSSSSS");
        try {
            return format.parse(creationTimeStamp);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }
}
