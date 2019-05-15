package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksConstants.TransactionsStatuses;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@JsonObject
public class TransactionEntity {

    @JsonProperty("AccountId")
    private String accountId;

    @JsonProperty("AddressLine")
    private String addressLine;

    @JsonProperty("Amount")
    private AmountEntity amount;

    @JsonProperty("BookingDateTime")
    private Date bookingDateTime;

    @JsonProperty("CreditDebitIndicator")
    private String creditDebitIndicator;

    @JsonProperty("ProprietaryBankTransactionCode")
    private CodeEntity proprietaryBankTransactionCode;

    @JsonProperty("Status")
    private String status;

    @JsonProperty("TransactionId")
    private String transactionId;

    @JsonProperty("ValueDateTime")
    private Date valueDateTime;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(amount.toAmount())
                .setDate(bookingDateTime)
                .setPending(!status.equalsIgnoreCase(TransactionsStatuses.BOOKED))
                .setExternalId(transactionId)
                .build();
    }
}
