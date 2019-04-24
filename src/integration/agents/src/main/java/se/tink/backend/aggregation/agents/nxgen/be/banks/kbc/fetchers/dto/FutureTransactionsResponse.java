package se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.fetchers.dto;

import com.google.common.base.Strings;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.HeaderResponse;
import se.tink.backend.aggregation.agents.nxgen.be.banks.kbc.dto.TypeValuePair;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

@JsonObject
public class FutureTransactionsResponse extends HeaderResponse {
    private List<FutureTransactionDto> transactions;
    private TypeValuePair repositioningKey;

    public Collection<UpcomingTransaction> getUpcomingTransactions() {

        return Optional.ofNullable(transactions).orElseGet(Collections::emptyList).stream()
                // When the user has no enough fund, the bank will reserve a transaction to reverse
                // the transaction in
                // the future, this transaction is without execution date
                .filter(futureTransactionDto -> futureTransactionDto.getExecutionDate() != null)
                .map(FutureTransactionDto::toUpcomingTransaction)
                .collect(Collectors.toList());
    }

    public boolean hasNext() {
        return repositioningKey != null && !Strings.isNullOrEmpty(repositioningKey.getValue());
    }

    public String nextKey() {
        return repositioningKey.getValue();
    }
}
