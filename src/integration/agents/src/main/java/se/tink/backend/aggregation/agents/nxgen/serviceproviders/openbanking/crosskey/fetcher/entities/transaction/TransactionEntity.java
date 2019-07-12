package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.Format;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseConstants.Transactions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.UpperCamelCaseStrategy.class)
public class TransactionEntity {

    private String accountId;
    private String addressLine;
    private AmountEntity amount;
    private TransactionBalanceEntity balance;
    private BankTransactionCodeEntity bankTransactionCode;
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
    private ProprietaryBankTransactionCodeEntity proprietaryBankTransactionCode;
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

        Amount transactionAmount = new Amount(amount.getCurrency(), amount.getAmount());

        transactionAmount =
                getCreditDebitIndicator().equals(TransactionTypeEntity.CREDIT)
                        ? transactionAmount.negate()
                        : transactionAmount;

        return CreditCardTransaction.builder()
                .setPending(!Transactions.STATUS_BOOKED.equalsIgnoreCase(status))
                .setDate(getBookedDate())
                .setDescription(creditorAccount.getName())
                .setAmount(transactionAmount)
                .build();
    }

    public Transaction constructTransactionalAccountTransaction() {

        Amount transactionAmount = new Amount(amount.getCurrency(), amount.getAmount());

        transactionAmount =
                getCreditDebitIndicator().equalsIgnoreCase(CrosskeyBaseConstants.Transactions.DEBIT)
                        ? transactionAmount.negate()
                        : transactionAmount;

        String transactionName =
                Optional.ofNullable(creditorAccount.getName()).orElse(debtorAccount.getName());

        return Transaction.builder()
                .setPending(!Transactions.STATUS_BOOKED.equalsIgnoreCase(status))
                .setDate(getBookedDate())
                .setDescription(transactionName)
                .setAmount(transactionAmount)
                .build();
    }

    @JsonIgnore
    private Date getBookedDate() {
        try {
            return new SimpleDateFormat(Format.TRANSACTION_TIMESTAMP).parse(bookingDateTime);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }
}
