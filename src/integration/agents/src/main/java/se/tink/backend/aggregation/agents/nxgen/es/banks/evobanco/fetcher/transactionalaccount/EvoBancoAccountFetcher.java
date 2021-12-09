package se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.transactionalaccount;

import java.util.Collection;
import java.util.Collections;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.EvoBancoConstants;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.entities.AnswerEntityGlobalPositionResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.evobanco.fetcher.rpc.GlobalPositionResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class EvoBancoAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private final EvoBancoApiClient bankClient;
    private final SessionStorage sessionStorage;

    public EvoBancoAccountFetcher(EvoBancoApiClient bankClient, SessionStorage sessionStorage) {
        this.bankClient = bankClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        GlobalPositionResponse globalPositionResponse = bankClient.globalPosition();

        if (globalPositionResponse != null) {
            globalPositionResponse.handleReturnCode();
            AnswerEntityGlobalPositionResponse answer =
                    globalPositionResponse.getEeOGlobalbePosition().getAnswer();

            if (answer.getAgreementsList() == null || answer.getAgreementsList().isEmpty()) {
                return Collections.emptyList();
            }

            return answer.getTransactionalAccounts(
                    sessionStorage.get(EvoBancoConstants.Storage.HOLDER_NAME), bankClient);
        }

        return Collections.emptyList();
    }
}
