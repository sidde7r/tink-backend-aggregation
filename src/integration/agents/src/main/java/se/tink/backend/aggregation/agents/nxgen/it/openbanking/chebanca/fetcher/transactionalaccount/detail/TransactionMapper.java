package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.detail;

import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.DateConfig.TRANSACTION_DATE_TIME_FMT;
import static se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.ChebancaConstants.DateConfig.ZONE_ID;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.exception.RequiredDataMissingException;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.AmountEntity;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionMapper {
    private static final DateTimeFormatter formatter =
            DateTimeFormatter.ofPattern(TRANSACTION_DATE_TIME_FMT).withZone(ZONE_ID);

    public static Transaction toTinkTransaction(
            TransactionEntity transactionEntity, boolean isPending) {
        return Transaction.builder()
                .setPending(isPending)
                .setAmount(getAmount(transactionEntity))
                .setDateTime(getTransactionDateAsString(transactionEntity, isPending))
                .setDescription(transactionEntity.getShortDescription())
                .build();
    }

    private static ZonedDateTime getTransactionDateAsString(
            TransactionEntity transactionEntity, boolean isPending) {
        final String dateToParse;
        if (isPending) {
            dateToParse = transactionEntity.getDateLiquidationValue();
        } else {
            dateToParse = transactionEntity.getDateAccountingCurrency();
        }

        return Optional.ofNullable(dateToParse)
                .map(dateAsString -> ZonedDateTime.parse(dateAsString, formatter))
                .orElseThrow(
                        () ->
                                new RequiredDataMissingException(
                                        "Could not parse the given transaction date"));
    }

    private static ExactCurrencyAmount getAmount(TransactionEntity transactionEntity) {
        return Optional.of(transactionEntity)
                .map(TransactionEntity::getAmountTransaction)
                .map(AmountEntity::toAmount)
                .orElseThrow(
                        () ->
                                new RequiredDataMissingException(
                                        "No transaction's amount data present"));
    }
}
