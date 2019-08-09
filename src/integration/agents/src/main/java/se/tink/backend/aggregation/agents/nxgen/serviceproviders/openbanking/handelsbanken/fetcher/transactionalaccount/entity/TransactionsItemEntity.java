package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.text.ParseException;
import java.time.DateTimeException;
import java.util.Date;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class TransactionsItemEntity {

    private static final Logger LOGGER = LoggerFactory.getLogger(TransactionsItemEntity.class);

    @JsonProperty("amount")
    private TransactionAmountEntity transactionAmountEntity;

    private String ledgerDate;

    private String creditorName;

    private BalanceEntity balanceEntity;

    private String remittanceInformation;

    private String bookingDate;

    private String debtorName;

    private String valueDate;

    private String creditDebit;

    private String transactionDate;

    private String status;

    public TransactionAmountEntity getTransactionAmountEntity() {
        return transactionAmountEntity;
    }

    public String getLedgerDate() {
        return ledgerDate;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public BalanceEntity getBalanceEntity() {
        return balanceEntity;
    }

    public String getRemittanceInformation() {
        return remittanceInformation;
    }

    public String getBookingDate() {
        return bookingDate;
    }

    public String getDebtorName() {
        return debtorName;
    }

    public String getValueDate() {
        return valueDate;
    }

    public String getCreditDebit() {
        return creditDebit;
    }

    public String getTransactionDate() {
        return transactionDate;
    }

    public String getStatus() {
        return status;
    }

    private Date getDate() {
        Optional<Date> date = getDateFromValueDate();
        if (!date.isPresent()) {
            date = getDataFromTransactionDate();
        }
        return date.orElseThrow(() -> new DateTimeException(ExceptionMessages.NOT_PARSE_DATE));
    }

    private Optional<Date> getDateFromValueDate() {
        Optional<Date> result = Optional.empty();
        if (valueDate == null) {
            return result;
        }
        try {
            result = Optional.of(ThreadSafeDateFormat.FORMATTER_DAILY.parse(valueDate));
        } catch (ParseException e) {
            LOGGER.warn(HandelsbankenBaseConstants.ExceptionMessages.VALUE_DATE_MISSING);
        }
        return result;
    }

    private Optional<Date> getDataFromTransactionDate() {
        try {
            return Optional.of(ThreadSafeDateFormat.FORMATTER_DAILY.parse(transactionDate));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    private BigDecimal creditOrDebit() {
        return HandelsbankenBaseConstants.Transactions.CREDITED.equalsIgnoreCase(creditDebit)
                ? transactionAmountEntity.getContent()
                : transactionAmountEntity.getContent().negate();
    }

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setDate(getDate())
                .setAmount(
                        new ExactCurrencyAmount(
                                creditOrDebit(), transactionAmountEntity.getCurrency()))
                .setDescription(remittanceInformation)
                .setPending(
                        HandelsbankenBaseConstants.Transactions.IS_PENDING.equalsIgnoreCase(status))
                .build();
    }
}
