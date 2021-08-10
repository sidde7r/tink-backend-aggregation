package se.tink.backend.aggregation.agents.summary.refresh.transactions;

import java.time.LocalDate;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter(AccessLevel.PACKAGE)
public class TransactionsSummary {

    private List<Integer> fetched;
    private LocalDate oldestTransactionDate;
}
