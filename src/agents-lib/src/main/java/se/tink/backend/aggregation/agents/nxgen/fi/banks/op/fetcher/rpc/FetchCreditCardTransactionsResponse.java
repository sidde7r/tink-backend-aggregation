package se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.op.fetcher.entities.OpBankCreditCardTransaction;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchCreditCardTransactionsResponse extends ArrayList<OpBankCreditCardTransaction> {
    @JsonIgnore
    public List<OpBankCreditCardTransaction> filterTransactions(
            List<OpBankCreditCardTransaction> processedTransactions) {
        return stream()
                .filter(OpBankCreditCardTransaction::isValidTransaction)
                .filter(transaction -> (!transaction.isDuplicateTransaction(processedTransactions)))
                .collect(Collectors.toList());
    }
}
