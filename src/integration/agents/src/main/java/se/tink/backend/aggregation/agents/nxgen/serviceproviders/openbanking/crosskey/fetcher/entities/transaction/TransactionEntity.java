package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.google.common.base.Strings;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.Format;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.Transactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class TransactionEntity {

    private String accountId;
    private String addressLine;
    private AmountEntity amount;
    private TransactionBalanceEntity balance;
    private BankTransactionCodeEntity bankTransactionCodeEntity;
    private String bookingDateTime;
    private CardInstrumentEntity cardInstrument;
    private AmountEntity chargeAmount;
    private String creditDebitIndicator;
    private CreditorAccountEntity creditorAccount;
    private CreditorAgentEntity creditorAgent;
    private CurrencyExchangeEntity currencyExchange;
    private DebtorAccountEntity debtorAccount;
    private DebtorAgentEntity debtorAgent;
    private MerchantDetailsEntity merchantDetails;
    private ProprietaryBankTransactionCodeEntity proprietaryBankTransactionCodeEntity;
    private List<String> statementReference = null;
    private String status;
    private SupplementaryDataEntity supplementaryData;
    private String transactionId;
    private String transactionInformation;
    private String transactionReference;
    private String valueDateTime;

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

        return CreditCardTransaction.builder()
                .setPending(!Transactions.STATUS_BOOKED.equalsIgnoreCase(status))
                .setDate(getBookedDate())
                .setDescription(getDescription())
                .setAmount(transactionAmount)
                .build();
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

        return Transaction.builder()
                .setPending(!Transactions.STATUS_BOOKED.equalsIgnoreCase(status))
                .setDate(getBookedDate())
                .setDescription(transactionName)
                .setAmount(transactionAmount)
                .build();
    }

    @JsonIgnore
    private Date getBookedDate() {
        String transactionDate =
                bookingDateTime == null
                        ? Optional.ofNullable(currencyExchange)
                                .map(CurrencyExchangeEntity::getQuotationDate)
                                .orElseThrow(IllegalStateException::new)
                        : bookingDateTime;
        try {
            return new SimpleDateFormat(Format.TRANSACTION_TIMESTAMP).parse(transactionDate);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }

    @JsonIgnore
    private String getDescription() {
        return Optional.ofNullable(transactionInformation)
                .orElse(StringUtils.capitalize(getProprietaryBankTransactionCode()));
    }

    private String getProprietaryBankTransactionCode() {
        return Optional.ofNullable(proprietaryBankTransactionCodeEntity)
                .map(ProprietaryBankTransactionCodeEntity::getCode)
                .orElse(getBankTransactionCodeEntity());
    }

    private String getBankTransactionCodeEntity() {
        return Optional.ofNullable(bankTransactionCodeEntity)
                .map(BankTransactionCodeEntity::getCode)
                .orElse("");
    }
}
