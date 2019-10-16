package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.CrossKeyConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.entities.CrossKeyAccount;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.entities.InstrumentDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.entities.InstrumentEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.fetcher.investment.rpc.PortfolioResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;

public class CrossKeyInvestmentsFetcher implements AccountFetcher<InvestmentAccount> {

    private final CrossKeyApiClient client;
    private final CrossKeyConfiguration agentConfiguration;

    public CrossKeyInvestmentsFetcher(
            CrossKeyApiClient client, CrossKeyConfiguration agentConfiguration) {
        this.client = client;
        this.agentConfiguration = agentConfiguration;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        return client.fetchAccounts().getAccounts().stream()
                .filter(CrossKeyAccount::isInvestmentAccount)
                .map(
                        account ->
                                account.toInvestmentAccount(
                                        agentConfiguration, fetchPortfolio(account)))
                .collect(Collectors.toList());
    }

    private Portfolio fetchPortfolio(CrossKeyAccount account) {
        PortfolioResponse portfolioResponse = client.fetchPortfolio(account.getAccountId());
        Portfolio portfolio = portfolioResponse.toTinkPortfolio(account);
        portfolio.setInstruments(fetchInstruments(portfolioResponse));
        return portfolio;
    }

    private List<Instrument> fetchInstruments(PortfolioResponse portfolioResponse) {
        return portfolioResponse.getInstrumentGroups().stream()
                .flatMap(
                        instrumentGroup ->
                                instrumentGroup.getInstruments().stream()
                                        .filter(InstrumentEntity::hasIsinCode)
                                        .map(
                                                instrument ->
                                                        toTinkInstrument(
                                                                instrumentGroup
                                                                        .getTypeOfInstruments(),
                                                                instrument)))
                .collect(Collectors.toList());
    }

    private Instrument toTinkInstrument(int typeOfInstrument, InstrumentEntity instrument) {
        switch (typeOfInstrument) {
            case 0: // TODO verify if for type 0, we can fecth intrument Details
            case 1:
                return instrument.toTinkInstrument(
                        typeOfInstrument, fetchInstrumentDetails(instrument));
            case 2:
                return instrument.toTinkInstrument(
                        typeOfInstrument,
                        isFund(instrument) ? Instrument.Type.FUND : Instrument.Type.OTHER);
            default:
                return instrument.toTinkInstrument(typeOfInstrument);
        }
    }

    private boolean isFund(InstrumentEntity instrument) {
        return client.fetchFundInfo(instrument.getInstrumentId()).getStatus().isSuccess();
    }

    private InstrumentDetailsEntity fetchInstrumentDetails(InstrumentEntity instrument) {
        return client.fetchInstrumentDetails(instrument.getIsinCode(), instrument.getMarketPlace())
                .getInstrumentDetails();
    }
}
