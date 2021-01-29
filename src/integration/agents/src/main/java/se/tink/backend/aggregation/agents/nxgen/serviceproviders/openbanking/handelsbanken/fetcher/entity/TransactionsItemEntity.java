package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.time.DateTimeException;
import java.util.Date;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.ExceptionMessages;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.date.ThreadSafeDateFormat;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class TransactionsItemEntity {

    @JsonProperty("amount")
    @JsonAlias("transactionAmount")
    private TransactionAmountEntity transactionAmountEntity;

    private String ledgerDate;

    private String creditorName;

    private BalanceEntity balanceEntity;

    @JsonAlias("transactionDetails")
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

    public Boolean hasDate() {
        return !Strings.isNullOrEmpty(transactionDate) || !Strings.isNullOrEmpty(valueDate);
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
        Optional<Date> date = getDateFromTransactionDate();
        return date.orElseThrow(() -> new DateTimeException(ExceptionMessages.NOT_PARSE_DATE));
    }

    private Optional<Date> getDateFromTransactionDate() {
        try {
            if (!Strings.isNullOrEmpty(transactionDate)) {
                return Optional.of(ThreadSafeDateFormat.FORMATTER_DAILY.parse(transactionDate));
            }
            return Optional.of(ThreadSafeDateFormat.FORMATTER_DAILY.parse(valueDate));
        } catch (ParseException e) {
            return Optional.empty();
        }
    }

    protected ExactCurrencyAmount creditOrDebit() {
        return HandelsbankenBaseConstants.Transactions.CREDITED.equalsIgnoreCase(creditDebit)
                ? transactionAmountEntity.getAmount()
                : transactionAmountEntity.getAmount().negate();
    }

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setDate(getDate())
                .setAmount(creditOrDebit())
                .setDescription(remittanceInformation)
                .setPending(
                        HandelsbankenBaseConstants.Transactions.IS_PENDING.equalsIgnoreCase(status))
                .setPayload(
                        TransactionPayloadTypes.DETAILS,
                        SerializationUtils.serializeToString(getTransactionDetails()))
                .build();
    }

    @JsonIgnore
    public TransactionDetails getTransactionDetails() {
        return new TransactionDetails(StringUtils.EMPTY, StringUtils.EMPTY);
    }
}
