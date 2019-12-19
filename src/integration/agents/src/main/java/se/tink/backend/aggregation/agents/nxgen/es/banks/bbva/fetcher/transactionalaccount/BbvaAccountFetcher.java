package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount;

import io.vavr.control.Option;
import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.PositionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public class BbvaAccountFetcher implements AccountFetcher<TransactionalAccount> {

    private BbvaApiClient apiClient;

    public BbvaAccountFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<TransactionalAccount> fetchAccounts() {
        return apiClient
                .fetchFinancialDashboard()
                .getPositions()
                .map(PositionEntity::getContract)
                .map(ContractEntity::getAccount)
                .filter(Option::isDefined)
                .map(Option::get)
                .filter(AccountEntity::isTransactionalAccount)
                .filter(AccountEntity::hasBalance)
                .map(AccountEntity::toTinkTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toList());
    }
}
