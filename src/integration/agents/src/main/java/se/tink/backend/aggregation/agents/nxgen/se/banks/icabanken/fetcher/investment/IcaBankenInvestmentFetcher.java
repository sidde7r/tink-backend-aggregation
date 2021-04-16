package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants.Policies;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.DepotEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.FundDetailsBodyEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.FundHoldingsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage.IcaBankenSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.source_info.AccountSourceInfo;
import se.tink.libraries.amount.ExactCurrencyAmount;

public class IcaBankenInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final IcaBankenApiClient apiClient;
    private final IcaBankenSessionStorage sessionStorage;

    public IcaBankenInvestmentFetcher(
            IcaBankenApiClient apiClient, IcaBankenSessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        if (!sessionStorage.hasPolicy(Policies.DEPOTS)) {
            return Collections.emptyList();
        }
        return apiClient.getInvestments().stream()
                .map(this::toInvestmentAccount)
                .collect(Collectors.toList());
    }

    public InvestmentAccount toInvestmentAccount(DepotEntity depot) {

        return InvestmentAccount.builder(depot.getDepotNumber())
                .setAccountNumber(depot.getDepotNumber())
                .setName(depot.getDepotName())
                .setCashBalance(ExactCurrencyAmount.inSEK(depot.getDisposable()))
                .setPortfolios(Collections.singletonList(addPortfolio(depot)))
                .sourceInfo(createAccountSourceInfo(depot))
                .build();
    }

    private AccountSourceInfo createAccountSourceInfo(DepotEntity depot) {
        return AccountSourceInfo.builder()
                .bankAccountType(depot.getInvestmentAccountType())
                .bankProductName(depot.getDepotName())
                .build();
    }

    private Portfolio addPortfolio(DepotEntity depot) {
        Portfolio portfolio = depot.toPortfolio();

        List<FundHoldingsEntity> holdings = depot.getFundHoldings();

        if (depotHasHoldings(holdings)) {
            portfolio.setInstruments(getInstruments(holdings));
        }

        return portfolio;
    }

    private boolean depotHasHoldings(List<FundHoldingsEntity> fundHoldings) {
        return !(fundHoldings == null || fundHoldings.isEmpty());
    }

    private List<Instrument> getInstruments(List<FundHoldingsEntity> holdings) {
        List<Instrument> instruments = new ArrayList<>();

        holdings.forEach(
                holdingEntity -> {
                    FundDetailsBodyEntity fundDetails =
                            apiClient.getFundDetails(holdingEntity.getFundId());
                    fundDetails.toInstrument(holdingEntity).ifPresent(instruments::add);
                });

        return instruments;
    }
}
