package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiApiClient;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.SpankkiConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.entities.PortfolioEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.entities.PositionsEntity;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.rpc.InstrumentDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.fetcher.investment.rpc.InvestmentAccountResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SpankkiInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final SpankkiApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public SpankkiInvestmentFetcher(
            SpankkiApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        final InvestmentAccountResponse investmentsAccountResponse =
                apiClient.fetchInvestmentsAccount();
        if (!investmentsAccountResponse.getUserHasInvestments()) {
            return Collections.emptyList();
        }
        final List<PortfolioModule> portfolioModules =
                getPortfolioModules(investmentsAccountResponse);
        return investmentsAccountResponse.toTinkInvestmentAccount(
                portfolioModules, persistentStorage.get(Storage.CUSTOMER_USER_ID));
    }

    private List<PortfolioModule> getPortfolioModules(
            InvestmentAccountResponse investmentAccountResponse) {
        return investmentAccountResponse.getInvestmentsAccount().getPortfolios().stream()
                .map(this::getPortfolioModule)
                .collect(Collectors.toList());
    }

    private PortfolioModule getPortfolioModule(PortfolioEntity portfolio) {
        final List<InstrumentModule> instrumentModules =
                portfolio.getPositions().stream()
                        .map(p -> getInstrumentModule(portfolio.getPortfolioId(), p))
                        .collect(Collectors.toList());
        return portfolio.toTinkPortfolio(instrumentModules);
    }

    private InstrumentModule getInstrumentModule(
            String portfolioId, PositionsEntity positionsEntity) {
        final InstrumentDetailsResponse instrumentDetailsResponse =
                apiClient.fetchInstrumentDetails(portfolioId, positionsEntity.getSecurityID());
        return instrumentDetailsResponse.toTinkInstrument(positionsEntity.getSecurityID());
    }
}
