package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.investment.entities.InvestmentEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.investment.entities.MiscEntity;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.investment.entities.TotalsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

@RequiredArgsConstructor
public class JyskeBankInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final JyskeBankApiClient apiClient;

    // The investment account parsing is insufficient due to lack of ambassador data.
    // Agent is currently logging investment response data.
    // See https://tinkab.atlassian.net/browse/ITE-2727
    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        final InvestmentEntity investments = apiClient.fetchInvestments().getInvestmentEntity();
        final TotalsEntity investmentAccounts = investments.getTotals();

        if (investmentAccounts.getMisc().hasInvestmentAccount()) {
            return investments.getListings().getCustodyAccounts().getMisc().stream()
                    .map(MiscEntity::toTinkInvestmentAccount)
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }
}
