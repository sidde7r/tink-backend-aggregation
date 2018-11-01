package se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.fetcher.transactionalaccount.rpc.CreditCardResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.core.account.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.Storage.TICKET;
import static se.tink.backend.aggregation.agents.nxgen.es.banks.ibercaja.IberCajaConstants.Storage.USERNAME;

public class IberCajaCreditCardTransactionalFetcher implements TransactionFetcher<CreditCardAccount> {

    private final IberCajaApiClient bankClient;
    private final SessionStorage storage;

    public IberCajaCreditCardTransactionalFetcher(IberCajaApiClient bankClient,
            SessionStorage storage) {
        this.bankClient = bankClient;
        this.storage = storage;
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(CreditCardAccount account) {
        CreditCardResponse creditCardResponse = bankClient.fetchCreditCardsTransactionList(account.getBankIdentifier(),
                IberCajaConstants.DefaultRequestParams.REQUEST_ORDER,
                IberCajaConstants.DefaultRequestParams.REQUEST_TYPE, storage.get(TICKET), storage.get(USERNAME));
        return creditCardResponse.toTinkTransactions();
    }
}
