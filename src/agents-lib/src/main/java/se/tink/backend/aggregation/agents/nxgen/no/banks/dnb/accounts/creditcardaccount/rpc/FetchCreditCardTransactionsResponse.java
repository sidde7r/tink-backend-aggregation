package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.entities.TransEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.entities.TransactionEntity;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.accounts.creditcardaccount.entities.TransactionsEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchCreditCardTransactionsResponse {
    private TransEntity trans;
    private List<TransactionsEntity> transactions;

    public TransEntity getTrans() {
        return trans;
    }

    public List<TransactionsEntity> getTransactions() {
        return transactions;
    }
}
