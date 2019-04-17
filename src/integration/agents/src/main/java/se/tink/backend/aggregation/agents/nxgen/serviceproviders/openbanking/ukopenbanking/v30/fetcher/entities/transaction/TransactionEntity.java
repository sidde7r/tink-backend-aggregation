package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.entities.transaction;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {
    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("TransactionId")
    private String transactionId;

    @JsonProperty("TransactionReference")
    private String transactionReference;

    @JsonProperty("Amount")
    private AmountEntity amount;

    @JsonProperty("CreditDebitIndicator")
    private UkOpenBankingApiDefinitions.CreditDebitIndicator creditDebitIndicator;

    @JsonProperty("Status")
    private UkOpenBankingApiDefinitions.EntryStatusCode status;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("BookingDateTime")
    private Date bookingDateTime;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("ValueDateTime")
    private Date valueDateTime;

    @JsonProperty("TransactionInformation")
    private String transactionInformation;

    @JsonProperty("BankTransactionCode")
    private BankTransactionCodeEntity bankTransactionCode;

    @JsonProperty("ProprietaryBankTransactionCode")
    private ProprietaryBankTransactionCodeEntity proprietaryBankTransactionCode;

    @JsonProperty("Balance")
    private BalanceEntity balance;

    @JsonProperty("AddressLine")
    private String addressLine;

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setExternalId(transactionId)
                .setAmount(getSignedAmount())
                .setDescription(transactionInformation)
                .setPending(status == UkOpenBankingApiDefinitions.EntryStatusCode.PENDING)
                .setDate(bookingDateTime)
                .build();
    }

    public CreditCardTransaction toCreditCardTransaction(CreditCardAccount account) {

        return CreditCardTransaction.builder()
                .setCreditAccount(account)
                .setAmount(getSignedAmount())
                .setDescription(transactionInformation)
                .setPending(status == UkOpenBankingApiDefinitions.EntryStatusCode.PENDING)
                .setDate(bookingDateTime)
                .build();
    }

    private Amount getSignedAmount() {
        if (UkOpenBankingApiDefinitions.CreditDebitIndicator.DEBIT == creditDebitIndicator) {
            return amount.negate();
        }
        return amount;
    }
}
