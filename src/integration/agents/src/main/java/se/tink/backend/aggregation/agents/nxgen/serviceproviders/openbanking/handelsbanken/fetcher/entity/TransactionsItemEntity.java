package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import lombok.Getter;
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
@Getter
public class TransactionsItemEntity {

    /** The date on which the transaction is recorded in the account. Only available for SE. */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate ledgerDate;

    /** The date on which a transaction is booked on the account. Only available for FI. */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    /** The date on which the transaction took place. Only available for SE. */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate transactionDate;

    /** The date the transaction debits/credits the account. Only available for UK. */
    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate valueDate;

    @JsonAlias("transactionDetails")
    private String remittanceInformation;

    @JsonProperty("amount")
    @JsonAlias("transactionAmount")
    private TransactionAmountEntity transactionAmountEntity;

    private String creditorName;
    private BalanceEntity balanceEntity;
    private String debtorName;
    private String creditDebit;
    private String status;

    public Boolean hasDate() {
        return ledgerDate != null || transactionDate != null || valueDate != null;
    }

    protected ExactCurrencyAmount getTinkAmount() {
        return HandelsbankenBaseConstants.Transactions.CREDITED.equalsIgnoreCase(creditDebit)
                ? transactionAmountEntity.getAmount()
                : transactionAmountEntity.getAmount().negate();
    }

    public Transaction toTinkTransaction(String providerMarket) {

        return (Transaction)
                Transaction.builder()
                        .setDate(ObjectUtils.firstNonNull(ledgerDate, transactionDate, valueDate))
                        .setAmount(getTinkAmount())
                        .setDescription(remittanceInformation)
                        .setPending(
                                HandelsbankenBaseConstants.Transactions.IS_PENDING.equalsIgnoreCase(
                                        status))
                        .setPayload(
                                TransactionPayloadTypes.DETAILS,
                                SerializationUtils.serializeToString(getTinkTransactionDetails()))
                        .setProprietaryFinancialInstitutionType(creditDebit)
                        .setTransactionDates(getTinkTransactionDates())
                        .setProviderMarket(providerMarket)
                        .build();
    }

    private TransactionDates getTinkTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();

        if (valueDate != null) {
            builder.setValueDate(new AvailableDateInformation().setDate(valueDate));
        }

        LocalDate tinkBookingDate = getTinkBookingDate();
        if (tinkBookingDate != null) {
            builder.setBookingDate(new AvailableDateInformation().setDate(tinkBookingDate));
        }

        return builder.build();
    }

    /**
     * Booking date is only available in FI. Ledger date is only available in SE. Ledger date is
     * another term for booking date so both are treated as Tink booking dates.
     */
    private LocalDate getTinkBookingDate() {
        return bookingDate != null ? bookingDate : ledgerDate;
    }

    @JsonIgnore
    public TransactionDetails getTinkTransactionDetails() {
        return new TransactionDetails(StringUtils.EMPTY, StringUtils.EMPTY);
    }
}
