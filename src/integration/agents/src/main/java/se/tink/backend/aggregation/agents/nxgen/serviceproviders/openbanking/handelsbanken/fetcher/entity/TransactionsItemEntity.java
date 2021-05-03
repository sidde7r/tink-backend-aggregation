package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import java.time.LocalDate;
import java.util.Objects;
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

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate ledgerDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate bookingDate;

    @JsonDeserialize(using = LocalDateDeserializer.class)
    private LocalDate transactionDate;

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
    public TransactionDetails getTinkTransactionDetails() {
        return new TransactionDetails(StringUtils.EMPTY, StringUtils.EMPTY);
    }
}
