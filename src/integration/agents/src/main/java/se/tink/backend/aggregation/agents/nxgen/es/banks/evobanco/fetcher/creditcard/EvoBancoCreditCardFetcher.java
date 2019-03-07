package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.creditcard;

import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities.AnswerEntityGlobalPositionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginator;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;

import java.util.Collection;
import java.util.Collections;

public class EvoBancoCreditCardFetcher
        implements AccountFetcher<CreditCardAccount>, TransactionPagePaginator<CreditCardAccount> {

    private final EvoBancoApiClient bankClient;

    public EvoBancoCreditCardFetcher(EvoBancoApiClient bankClient) {
        this.bankClient = bankClient;
    }

    @Override
    public Collection<CreditCardAccount> fetchAccounts() {
        GlobalPositionResponse globalPositionResponse = bankClient.globalPosition();

        if (globalPositionResponse != null) {
            AnswerEntityGlobalPositionResponse answer = globalPositionResponse.getEeOGlobalbePosition().getAnswer();

            if (answer.getAgreementsList() == null || answer.getAgreementsList().isEmpty()) {
                return Collections.emptyList();
            }

            return answer.getCreditCardAccounts();
        }

        return Collections.emptyList();
    }

    @Override
    public PaginatorResponse getTransactionsFor(CreditCardAccount account, int page) {
        return bankClient.fetchCardTransactions(account.getBankIdentifier(), page);
    }
}
