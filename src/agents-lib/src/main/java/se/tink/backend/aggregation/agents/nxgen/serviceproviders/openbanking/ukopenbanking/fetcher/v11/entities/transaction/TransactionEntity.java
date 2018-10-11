package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v11.entities.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.ZonedDateTime;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setExternalId(transactionId)
                .setAmount(amount)
                .setDescription(transactionInformation)
                .setPending(status == UkOpenBankingConstants.EntryStatusCode.PENDING)
                .setDateTime(bookingDateTime)
                .build();
    }

    public CreditCardTransaction toCreditCardTransaction(CreditCardAccount account) {

        return CreditCardTransaction.builder()
                .setCreditAccount(account)
                .setAmount(amount)
                .setDescription(transactionInformation)
                .setPending(status == UkOpenBankingConstants.EntryStatusCode.PENDING)
                .setDateTime(bookingDateTime)
                .build();
    }

    @JsonProperty("AccountId")
    private String accountId;
    @JsonProperty("TransactionId")
    private String transactionId;
    @JsonProperty("TransactionReference")
    private String transactionReference;
    @JsonProperty("Amount")
    private AmountEntity amount;
    @JsonProperty("CreditDebitIndicator")
    private String creditDebitIndicator;
    @JsonProperty("Status")
    private UkOpenBankingConstants.EntryStatusCode status;
    private ZonedDateTime bookingDateTime;
    private ZonedDateTime valueDateTime;
    @JsonProperty("TransactionInformation")
    private String transactionInformation;
    @JsonProperty("BankTransactionCode")
    private BankTransactionCodeEntity bankTransactionCode;
    @JsonProperty("ProprietaryBankTransactionCode")
    private ProprietaryBankTransactionCodeEntity proprietaryBankTransactionCode;
    @JsonProperty("Balance")
    private BalanceEntity balance;

    @JsonProperty("BookingDateTime")
    private void setBookingDateTime(String date) {
        bookingDateTime = ZonedDateTime.parse(date);
    }

    @JsonProperty("ValueDateTime")
    private void setValueDateTime(String date) {
        valueDateTime = ZonedDateTime.parse(date);
    }

}
