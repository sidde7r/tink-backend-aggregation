package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingV31Constants.Time.DEFAULT_OFFSET;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional.entity.TransactionEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
import se.tink.libraries.enums.MarketCode;

@Slf4j
public final class StarlingTransactionMapper {

    public static Transaction toTinkTransaction(TransactionEntity transaction) {

        ExactCurrencyAmount transactionAmount = transaction.getAmount().toExactCurrencyAmount();

        if (transaction.isOutDirection()) {
            transactionAmount = transactionAmount.negate();
        }

        Builder builder =
                Transaction.builder()
                        .setAmount(transactionAmount)
                        .setPending(transaction.isPending())
                        .setProprietaryFinancialInstitutionType(transaction.getSource())
                        .setProviderMarket(String.valueOf(MarketCode.UK))
                        .setTransactionDates(getTransactionDates(transaction))
                        .setDate(getDate(transaction))
                        .setDescription(getDescription(transaction));

        if (transaction.hasFeedItemUid()) {
            builder.addExternalSystemIds(
                    TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                    transaction.getFeedItemUid());
        }

        if (transaction.isMerchantCounterPartyType()) {
            builder.setMerchantName(transaction.getCounterPartyName());
        }
        return (Transaction) builder.build();
    }

    private static String getDescription(TransactionEntity transaction) {
        if (transaction.hasReference()) {
            return transaction.getReference();
        }
        return transaction.getCounterPartyName();
    }

    private static Date getDate(TransactionEntity transaction) {
        if (transaction.hasSettlementTime()) {
            return getDateOfTransaction(transaction.getSettlementTime());
        }
        return getDateOfTransaction(transaction.getTransactionTime());
    }

    private static Date getDateOfTransaction(Instant instant) {
        try {
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
            return simpleDateFormat.parse(instant.toString());
        } catch (ParseException e) {
            log.warn(
                    "[StarlingTransactionMapper] Issue with parsing date of transaction {}",
                    instant);
            return Date.from(instant);
        }
    }

    private static TransactionDates getTransactionDates(TransactionEntity transaction) {
        TransactionDates.Builder transactionDates =
                TransactionDates.builder()
                        .setTransactionDate(
                                prepareTransactionDate(transaction.getTransactionTime()));

        if (transaction.hasSettlementTime()) {
            transactionDates.setBookingDate(
                    prepareTransactionDate(transaction.getSettlementTime()));
        }

        return transactionDates.build();
    }

    private static AvailableDateInformation prepareTransactionDate(Instant transactionDateTime) {
        return new AvailableDateInformation()
                .setDate(LocalDateTime.ofInstant(transactionDateTime, DEFAULT_OFFSET).toLocalDate())
                .setInstant(transactionDateTime);
    }
}
