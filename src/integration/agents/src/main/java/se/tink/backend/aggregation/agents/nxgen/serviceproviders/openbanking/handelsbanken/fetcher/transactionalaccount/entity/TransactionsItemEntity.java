package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

import java.text.ParseException;
import java.time.DateTimeException;
import java.util.Date;

@JsonObject
public class TransactionsItemEntity {

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

        if (valueDate != null) {
            try {
                return ThreadSafeDateFormat.FORMATTER_DAILY.parse(valueDate);
            } catch (ParseException e) {
                System.err.println(HandelsbankenBaseConstants.ExceptionMessages.VALUE_DATE_MISSING);
            }
        }

        try {
            return ThreadSafeDateFormat.FORMATTER_DAILY.parse(transactionDate);
        } catch (ParseException e) {
            throw new DateTimeException(ExceptionMessages.NOT_PARSE_DATE);
        }

    }

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setDate(getDate())
                .setAmount(
                        new Amount(transactionAmountEntity.getCurrency(), transactionAmountEntity.getContent()))
                .setDescription(remittanceInformation)
                .setPending(HandelsbankenBaseConstants.Transactions.IS_PENDING.equalsIgnoreCase(status))
                .build();
    }
}
