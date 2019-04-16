package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.openbankproject.transactionalaccount.entities.transactions;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class TransactionEntity {

    private String id;

    @JsonProperty("this_account")
    private ThisAccountEntity thisAccount;

    @JsonProperty("other_account")
    private OtherAccountEntity otherAccount;

    private DetailsEntity details;

    private MetadataEntity metadata;

    public Transaction toTinkTransaction() {
        return Transaction.builder()
                .setAmount(getAmount())
                .setDescription(details.getDescription())
                .setDate(details.getPosted())
                .setPending(false)
                .build();
    }

    public Amount getAmount() {
        return new Amount(details.getValue().getCurrency(), details.getValue().getAmount());
    }
}
