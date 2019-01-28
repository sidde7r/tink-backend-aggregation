package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.Amount;

@JsonObject
public class ReservedTransactionEntity extends AbstractTransactionEntity {

    public Optional<Transaction> toTinkTransaction() {
        if (this.date == null || this.description == null) {
            return Optional.empty();
        }

        Amount amount = new Amount(this.currency, AgentParsingUtils.parseAmount(this.amount));

        if (amount.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                Transaction.builder()
                        .setAmount(amount)
                        .setDate(this.date)
                        .setDescription(SwedbankBaseConstants.Description.clean(this.description))
                        .setPending(true)
                        .build());
    }
}
