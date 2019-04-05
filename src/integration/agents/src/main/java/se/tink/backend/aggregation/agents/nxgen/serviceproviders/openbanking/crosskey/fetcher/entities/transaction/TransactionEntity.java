package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.fetcher.entities.transaction;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
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

    public Transaction toTinkTransaction(TransactionTypeEntity transactionType) {
        return transactionType == TransactionTypeEntity.DEBIT
                ? constructTransactionalAccountTransaction()
                : constructCreditCardTransaction();
    }

    private Transaction constructCreditCardTransaction() {
        return CreditCardTransaction.builder()
                .setPending(!Transactions.STATUS_BOOKED.equalsIgnoreCase(status))
                .setExternalId(accountId)
                .setDate(getBookedDate())
                .setAmount(new Amount(amount.getCurrency(), amount.getAmount()))
                .build();
    }

    private Transaction constructTransactionalAccountTransaction() {
        return Transaction.builder()
                .setPending(!Transactions.STATUS_BOOKED.equalsIgnoreCase(status))
                .setExternalId(accountId)
                .setDate(getBookedDate())
                .setAmount(new Amount(amount.getCurrency(), amount.getAmount()))
                .build();
    }

    @JsonIgnore
    public Date getBookedDate() {
        try {
            return new SimpleDateFormat(Format.TRANSACTION_TIMESTAMP).parse(bookingDateTime);
        } catch (ParseException e) {
            throw new IllegalStateException(e);
        }
    }
}
