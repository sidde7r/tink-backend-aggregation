package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.text.ParseException;
import java.time.DateTimeException;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.date.ThreadSafeDateFormat;

@JsonObject
public class TransactionsItem {

    @JsonProperty("amount")
    private TransactionAmount transactionAmount;

    @JsonProperty("ledgerDate")
    private String ledgerDate;

    @JsonProperty("creditorName")
    private String creditorName;

    @JsonProperty("balance")
    private Balance balance;

    @JsonProperty("remittanceInformation")
    private String remittanceInformation;

    @JsonProperty("bookingDate")
    private String bookingDate;

    @JsonProperty("debtorName")
    private String debtorName;

    @JsonProperty("valueDate")
    private String valueDate;

    @JsonProperty("creditDebit")
    private String creditDebit;

    @JsonProperty("transactionDate")
    private String transactionDate;

    @JsonProperty("status")
    private String status;

    public TransactionAmount getTransactionAmount() {
        return transactionAmount;
    }

    public String getLedgerDate() {
        return ledgerDate;
    }

    public String getCreditorName() {
        return creditorName;
    }

    public Balance getBalance() {
        return balance;
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
                        new Amount(transactionAmount.getCurrency(), transactionAmount.getContent()))
                .setDescription(remittanceInformation)
                .setPending(HandelsbankenBaseConstants.Transactions.IS_PENDING.equalsIgnoreCase(status))
                .build();
    }
}
