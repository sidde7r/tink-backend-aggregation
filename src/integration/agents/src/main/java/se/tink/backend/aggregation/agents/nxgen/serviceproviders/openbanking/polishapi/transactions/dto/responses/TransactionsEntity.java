package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.transactions.dto.responses;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.time.LocalDate;
import lombok.Getter;
import org.apache.commons.lang3.ObjectUtils;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.models.TransactionTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@Getter
@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionsEntity {

    private String transactionCategory;
    private String aspspTransactionId;
    private String amount;
    private String currency;
    private String description;
    private String transactionType;
    private LocalDate tradeDate;
    private String mcc;
    private String itemId;

    private String initiatorName;
    private AccountInformationEntity sender;
    private AccountInformationEntity recipient;
    private LocalDate bookingDate;
    private String postTransactionBalance;
    private String rejectionReason;
    private String rejectionDate;
    private String holdExpirationDate;

    @JsonIgnore
    public Transaction toTinkTransaction(
            PolishApiConstants.Transactions.TransactionTypeRequest typeRequest) {
        return (Transaction)
                Transaction.builder()
                        .setPending(isPending(typeRequest))
                        .setAmount(ExactCurrencyAmount.of(amount, currency))
                        .setDate(bookingDate)
                        .setDescription(ObjectUtils.firstNonNull(description, transactionType))
                        .setMerchantCategoryCode(mcc)
                        .setProprietaryFinancialInstitutionType(transactionCategory)
                        .setRawDetails(this)
                        .setTransactionDates(getTinkTransactionDates())
                        .setType(getTransactionTypes())
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                ObjectUtils.firstNonNull(aspspTransactionId, itemId))
                        .build();
    }

    @JsonIgnore
    private boolean isPending(PolishApiConstants.Transactions.TransactionTypeRequest typeRequest) {
        return typeRequest == PolishApiConstants.Transactions.TransactionTypeRequest.PENDING;
    }

    @JsonIgnore
    private TransactionDates getTinkTransactionDates() {
        return TransactionDates.builder()
                .setValueDate(new AvailableDateInformation().setDate(tradeDate))
                .setBookingDate(new AvailableDateInformation().setDate(bookingDate))
                .build();
    }

    @JsonIgnore
    private TransactionTypes getTransactionTypes() {
        if (transactionType.toLowerCase().startsWith("przel")) {
            return TransactionTypes.TRANSFER;
        } else if (transactionType.toLowerCase().contains("wypł")) {
            return TransactionTypes.WITHDRAWAL;
        } else {
            return TransactionTypes.PAYMENT;
        }
    }
}
