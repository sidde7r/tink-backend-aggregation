package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Date;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionsEntity {
    @JsonFormat(pattern = "yyyyMMdd")
    @JsonProperty
    private Date dueDate;

    @JsonFormat(pattern = "yyyyMMdd")
    @JsonProperty
    private Date bookingDate;

    @JsonProperty private String transactionId;
    @JsonProperty private String amount;
    @JsonProperty private String currency;
    @JsonProperty private String originalAmount;
    @JsonProperty private String originalCurrency;
    @JsonProperty private String recieverName;
    @JsonProperty private String reference;
    @JsonProperty private String textCode;
    @JsonProperty private boolean ownRegistered;
    @JsonProperty private boolean copyable;
    @JsonProperty private boolean sepaTransaction;
    @JsonProperty private String periodicity;
    @JsonProperty private String endDate;

    @JsonIgnore
    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(amount, currency))
                .setDate(bookingDate)
                .setDescription(recieverName)
                .addExternalSystemIds(
                        TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID,
                        transactionId)
                .build();
    }
}
