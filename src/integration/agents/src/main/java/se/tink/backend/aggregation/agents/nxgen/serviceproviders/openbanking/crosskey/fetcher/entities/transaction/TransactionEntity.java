package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Strings;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.Transactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction.Builder;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.TransactionDates;
import se.tink.backend.aggregation.utils.json.deserializers.LocalDateTimeDeserializer;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.chrono.AvailableDateInformation;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class TransactionEntity {

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime valueDateTime;

    @JsonDeserialize(using = LocalDateTimeDeserializer.class)
    private LocalDateTime bookingDateTime;

    private String accountId;
    private String addressLine;
    private AmountEntity amount;
    private TransactionBalanceEntity balance;
    private BankTransactionCodeEntity bankTransactionCode;
    private CardInstrumentEntity cardInstrument;
    private AmountEntity chargeAmount;
    private String creditDebitIndicator;
    private CreditorAccountEntity creditorAccount;
    private CreditorAgentEntity creditorAgent;
    private CurrencyExchangeEntity currencyExchange;
    private DebtorAccountEntity debtorAccount;
    private DebtorAgentEntity debtorAgent;
    private MerchantDetailsEntity merchantDetails;
    private ProprietaryBankTransactionCodeEntity proprietaryBankTransactionCode;
    private List<String> statementReference = null;
    private String status;
    private SupplementaryDataEntity supplementaryData;
    private String transactionId;
    private String transactionInformation;
    private String transactionReference;

    public String getCreditDebitIndicator() {
        return creditDebitIndicator;
    }

    public Transaction constructCreditCardTransaction() {

        ExactCurrencyAmount transactionAmount =
                ExactCurrencyAmount.of(amount.getAmount(), amount.getCurrency());

        transactionAmount =
                getCreditDebitIndicator().equals(TransactionTypeEntity.DEBIT)
                        ? transactionAmount.negate()
                        : transactionAmount;

        return (CreditCardTransaction)
                getAggregationTransaction(transactionAmount, getDescription());
    }

    public Transaction constructTransactionalAccountTransaction() {

        ExactCurrencyAmount transactionAmount =
                ExactCurrencyAmount.of(amount.getAmount(), amount.getCurrency());

        transactionAmount =
                getCreditDebitIndicator().equalsIgnoreCase(CrosskeyBaseConstants.Transactions.DEBIT)
                        ? transactionAmount.negate()
                        : transactionAmount;

        final String transactionName;
        if (creditorAccount != null && !Strings.isNullOrEmpty(creditorAccount.getName())) {
            transactionName = creditorAccount.getName();
        } else if (debtorAccount != null && !Strings.isNullOrEmpty(debtorAccount.getName())) {
            transactionName = debtorAccount.getName();
        } else {
            transactionName = "";
        }

        return (Transaction) getAggregationTransaction(transactionAmount, transactionName);
    }

    private AggregationTransaction getAggregationTransaction(
            ExactCurrencyAmount amount, String description) {
        Builder builder =
                Transaction.builder()
                        .setAmount(amount)
                        .setPending(!Transactions.STATUS_BOOKED.equalsIgnoreCase(status))
                        .setDescription(description)
                        .setDate(getBookedDate())
                        .setTransactionDates(getTinkTransactionDates())
                        .addExternalSystemIds(
                                TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                                transactionId)
                        .setProprietaryFinancialInstitutionType(getProprietaryBankTransactionCode())
                        .setTransactionReference(transactionReference);

        if (merchantDetails != null) {
            builder.setMerchantName(merchantDetails.getMerchantName());
            builder.setMerchantCategoryCode(merchantDetails.getMerchantCategoryCode());
        }

        return builder.build();
    }

    private TransactionDates getTinkTransactionDates() {
        TransactionDates.Builder builder = TransactionDates.builder();

        builder.setBookingDate(
                new AvailableDateInformation().setDate(bookingDateTime.toLocalDate()));

        if (valueDateTime != null) {
            builder.setValueDate(
                    new AvailableDateInformation().setDate(valueDateTime.toLocalDate()));
        }

        return builder.build();
    }

    @JsonIgnore
    private LocalDate getBookedDate() {
        LocalDateTime bookingDate =
                bookingDateTime == null
                        ? Optional.ofNullable(currencyExchange)
                                .map(CurrencyExchangeEntity::getQuotationDate)
                                .orElseThrow(IllegalStateException::new)
                        : bookingDateTime;

        return bookingDate.toLocalDate();
    }

    @JsonIgnore
    private String getDescription() {
        return Optional.ofNullable(transactionInformation)
                .orElse(StringUtils.capitalize(getProprietaryBankTransactionCode()));
    }

    private String getProprietaryBankTransactionCode() {
        return Optional.ofNullable(proprietaryBankTransactionCode)
                .map(ProprietaryBankTransactionCodeEntity::getCode)
                .orElse(getBankTransactionCodeEntity());
    }

    private String getBankTransactionCodeEntity() {
        return Optional.ofNullable(bankTransactionCode)
                .map(BankTransactionCodeEntity::getCode)
                .orElse("");
    }
}
