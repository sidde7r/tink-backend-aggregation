package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.Objects;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;
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

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

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

    public LocalDate getBookingDate() {
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

    public Transaction toTinkTransaction(String providerMarket) {

        return (Transaction)
                Transaction.builder()
                        .setDate(ObjectUtils.firstNonNull(ledgerDate, transactionDate, valueDate))
                        .setAmount(creditOrDebit())
                        .setDescription(remittanceInformation)
                        .setPending(
                                HandelsbankenBaseConstants.Transactions.IS_PENDING.equalsIgnoreCase(
                                        status))
                        .setPayload(
                                TransactionPayloadTypes.DETAILS,
                                SerializationUtils.serializeToString(getTransactionDetails()))
                        .setProprietaryFinancialInstitutionType(creditDebit)
                        .setTransactionDates(getTransactionDates())
                        .setProviderMarket(providerMarket)
                        .build();
    }

    private TransactionDates getTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();

        builder.setValueDate(new AvailableDateInformation().setDate(getTinkValueDate()));

        if (Objects.nonNull(bookingDate)) {
            builder.setBookingDate(new AvailableDateInformation().setDate(bookingDate));
        }

        return builder.build();
    }

    /**
     * transactionDate is not set for all transactions. In those cases valueDate corresponds to what
     * we classify as value date at Tink so we use that instead.
     */
    private LocalDate getTinkValueDate() {
        return Objects.nonNull(ledgerDate) ? ledgerDate : transactionDate;
    }

    @JsonIgnore
    public TransactionDetails getTransactionDetails() {
        return new TransactionDetails(StringUtils.EMPTY, StringUtils.EMPTY);
    }
}
