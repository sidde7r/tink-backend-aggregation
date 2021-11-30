package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans.fetcher.transactionalaccounts.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Date;
import se.tink.backend.aggregation.agents.models.TransactionExternalSystemIdType;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction.Builder;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountTransactionEntity {
    private String transaktionsId;

    @JsonProperty("text")
    private String description;

    @JsonProperty("belopp")
    private double amount;

    @JsonProperty("datum")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date date;

    private boolean harDetaljer;
    private double saldo;

    public Transaction toTinkTransaction() {

        Builder builder =
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.inSEK(amount))
                        .setDate(date)
                        .setDescription(description);

        if (!Strings.isNullOrEmpty(transaktionsId)) {
            builder.addExternalSystemIds(
                    TransactionExternalSystemIdType.PROVIDER_GIVEN_TRANSACTION_ID, transaktionsId);
        }

        return builder.build();
    }
}
