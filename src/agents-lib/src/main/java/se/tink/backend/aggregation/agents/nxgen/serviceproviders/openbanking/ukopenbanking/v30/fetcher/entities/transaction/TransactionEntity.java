package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v30.fetcher.entities.transaction;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.time.ZonedDateTime;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.UkOpenBankingConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.core.Amount;

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
    private UkOpenBankingConstants.CreditDebitIndicator creditDebitIndicator;
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
    @JsonProperty("AddressLine")
    private String addressLine;

    public Transaction toTinkTransaction() {

        return Transaction.builder()
                .setExternalId(transactionId)
                .setAmount(getSignedAmount())
                .setDescription(transactionInformation)
                .setPending(status == UkOpenBankingConstants.EntryStatusCode.PENDING)
                .setDateTime(bookingDateTime)
                .build();
    }

    public CreditCardTransaction toCreditCardTransaction(CreditCardAccount account) {

        return CreditCardTransaction.builder()
                .setCreditAccount(account)
                .setAmount(getSignedAmount())
                .setDescription(transactionInformation)
                .setPending(status == UkOpenBankingConstants.EntryStatusCode.PENDING)
                .setDateTime(bookingDateTime)
                .build();
    }

    private Amount getSignedAmount() {
        if (UkOpenBankingConstants.CreditDebitIndicator.DEBIT == creditDebitIndicator) {
            return amount.negate();
        }
        return amount;
    }

    @JsonProperty("BookingDateTime")
    private void setBookingDateTime(String date) {
        if (!Strings.isNullOrEmpty(date)) {
            bookingDateTime = ZonedDateTime.parse(date);
        }
    }

    @JsonProperty("ValueDateTime")
    private void setValueDateTime(String date) {
        if (!Strings.isNullOrEmpty(date)) {
            valueDateTime = ZonedDateTime.parse(date);
        }
    }
}
