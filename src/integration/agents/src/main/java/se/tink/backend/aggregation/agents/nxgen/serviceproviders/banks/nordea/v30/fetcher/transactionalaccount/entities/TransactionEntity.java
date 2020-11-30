package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Date;
import java.util.Set;
import org.apache.commons.lang.StringUtils;
import se.tink.backend.aggregation.agents.models.TransactionPayloadTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.NordeaConfiguration;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.libraries.amount.ExactCurrencyAmount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public class TransactionEntity {
    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty private boolean booked;
    @JsonProperty private double amount;
    @JsonProperty private String currency;

    @JsonProperty("to_account_number")
    private String toAccountNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("booking_date")
    private Date bookingDate;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @JsonProperty("interest_date")
    private Date interestDate;

    @JsonProperty private String title;

    @JsonProperty("balance_after")
    private double balanceAfter;

    @JsonProperty("transaction_type")
    private TransactionTypeEntity transactionType;

    @JsonIgnore
    public Transaction toTinkTransaction(NordeaConfiguration nordeaConfiguration) {
        Builder transaction =
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(amount, currency))
                        .setPending(!booked)
                        .setDescription(title)
                        .setDate(bookingDate);

        if (nordeaConfiguration.isBusinessAgent()) {
            transaction.setPayload(
                    TransactionPayloadTypes.DETAILS,
                    SerializationUtils.serializeToString(getTransactionDetails()));
        }

        return transaction.build();
    }

    @JsonIgnore
    public boolean hasSeenTransactionBefore(Set<String> transactionIdsSeen) {
        if (transactionIdsSeen.contains(transactionId)) {
            return true;
        }
        transactionIdsSeen.add(transactionId);
        return false;
    }

    @JsonIgnore
    public TransactionDetails getTransactionDetails() {
        return new TransactionDetails(StringUtils.EMPTY, Strings.nullToEmpty(toAccountNumber));
    }
}
