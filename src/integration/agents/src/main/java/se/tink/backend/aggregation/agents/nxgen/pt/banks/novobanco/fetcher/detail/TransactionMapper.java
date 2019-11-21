package se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.detail;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.pt.banks.novobanco.fetcher.entity.response.MovementsEntity;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class TransactionMapper {
    public static Transaction mapToTinkTransaction(MovementsEntity movement, String currency) {
        LocalDate dateOfOperation = LocalDate.parse(movement.getDateOfOperation());
        return Transaction.builder()
                .setAmount(ExactCurrencyAmount.of(movement.getSum(), currency))
                .setDate(dateOfOperation)
                .setDescription(movement.getDescription())
                .build();
    }
}
