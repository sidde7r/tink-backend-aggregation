package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.rpc;

import java.util.List;
import net.minidev.json.annotate.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities.Error;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebopenbanking.fetcher.cardaccounts.entities.Transactions;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.CreditCardTransaction;

@JsonObject
public class FetchCardAccountsTransactions {

    private Error error;
    private Transactions transactions;

    public Error getError() {
        return error;
    }

    public Transactions getTransactions() {
        return transactions;
    }

    @JsonIgnore
    public List<CreditCardTransaction> tinkTransactions(CreditCardAccount creditAccount) {
        return transactions.toTinkTransactions(creditAccount);
    }
}
