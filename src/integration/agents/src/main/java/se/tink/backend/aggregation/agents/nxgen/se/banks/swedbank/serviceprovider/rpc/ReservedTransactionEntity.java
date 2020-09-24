package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.rpc;

import com.google.common.base.Strings;
import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentParsingUtils;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class ReservedTransactionEntity extends AbstractTransactionEntity {

    public Optional<Transaction> toTinkTransaction() {
        if (this.date == null || getTransactionDescription() == null) {
            return Optional.empty();
        }

        double parsedAmount = AgentParsingUtils.parseAmount(amount);

        if (Strings.isNullOrEmpty(currency) || !Double.isFinite(parsedAmount)) {
            return Optional.empty();
        }

        return Optional.of(
                Transaction.builder()
                        .setAmount(ExactCurrencyAmount.of(parsedAmount, currency))
                        .setDate(this.date)
                        .setDescription(
                                SwedbankBaseConstants.Description.clean(
                                        getTransactionDescription()))
                        .setPending(true)
                        .build());
    }
}
