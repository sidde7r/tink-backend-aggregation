package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ContractEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.LoanEntity;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.PositionEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;

public class BbvaLoanFetcher implements AccountFetcher<LoanAccount> {
    private final BbvaApiClient apiClient;

    public BbvaLoanFetcher(BbvaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public Collection<LoanAccount> fetchAccounts() {
        return apiClient.fetchFinancialDashboard().getPositions().stream()
                .map(PositionEntity::getContract)
                .map(ContractEntity::getLoan)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .map(LoanEntity::toTinkLoanAccount)
                .collect(Collectors.toList());
    }
}
