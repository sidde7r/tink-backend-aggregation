package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class TransactionsItemEntity {

    @JsonProperty("amount")
    @JsonAlias("transactionAmount")
    private TransactionAmountEntity transactionAmountEntity;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate ledgerDate;

    private String creditorName;

    private BalanceEntity balanceEntity;

    @JsonAlias("transactionDetails")
    private String remittanceInformation;

    private String bookingDate;

    private String debtorName;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    private String creditDebit;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate transactionDate;

    private String status;

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
        return ledgerDate != null || transactionDate != null || valueDate != null;
    }

    public String getDebtorName() {
        return debtorName;
    }

    @JsonDeserialize(using = LocalDateDeserializer.class)
    public LocalDate getValueDate() {
        return valueDate;
    }

    @JsonDeserialize(using = LocalDateDeserializer.class)
    public LocalDate getTransactionDate() {
        return transactionDate;
    }

    public String getStatus() {
        return status;
    }

    protected ExactCurrencyAmount creditOrDebit() {
        return HandelsbankenBaseConstants.Transactions.CREDITED.equalsIgnoreCase(creditDebit)
                ? transactionAmountEntity.getAmount()
                : transactionAmountEntity.getAmount().negate();
    }

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setDate(ObjectUtils.firstNonNull(ledgerDate, transactionDate, valueDate))
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
