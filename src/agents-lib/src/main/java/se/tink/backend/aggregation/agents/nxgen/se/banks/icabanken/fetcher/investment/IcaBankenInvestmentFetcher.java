package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.DepotsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.FundDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.fetcher.investment.entities.FundHoldingsEntity;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.core.Amount;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Portfolio;

public class IcaBankenInvestmentFetcher implements AccountFetcher<InvestmentAccount> {
    private final IcaBankenApiClient client;
    private final SessionStorage session;

    public IcaBankenInvestmentFetcher(IcaBankenApiClient client, SessionStorage session) {
        this.client = client;
        this.session = session;
    }

    @Override
    public Collection<InvestmentAccount> fetchAccounts() {
        Collection<InvestmentAccount> accounts = client.getInvestments()
                .getBody().getDepots().stream().map( depot-> toInvestmentAccount(depot))
                .collect(Collectors.toList());

        return accounts;

    }

    private Instrument toInstrument(FundHoldingsEntity holdingEntity, FundDetailsEntity detailsEntity){
        Instrument instrument = new Instrument();

        instrument.setType(Instrument.Type.FUND);
        instrument.setUniqueIdentifier(String.join(detailsEntity.getiSIN()
                + detailsEntity.getTradingCode()));
        instrument.setIsin(detailsEntity.getiSIN());
        instrument.setMarketPlace(detailsEntity.getTradingCode());
        instrument.setAverageAcquisitionPrice(holdingEntity.getInvestedAmount()
                / holdingEntity.getShares());
        instrument.setCurrency(detailsEntity.getTradingCode());
        instrument.setMarketValue(holdingEntity.getMarketValue());
        instrument.setName(holdingEntity.getFundName());
        instrument.setPrice(detailsEntity.getNetAssetValue());
        instrument.setQuantity(holdingEntity.getShares());
        instrument.setProfit((instrument.getMarketValue()
                - instrument.getAverageAcquisitionPrice())
                * instrument.getQuantity());
        instrument.setRawType(detailsEntity.getCategory());

        return instrument;
    }

    public Portfolio toPortfolio(DepotsEntity depot){
        Portfolio portfolio = new Portfolio();
        portfolio.setTotalValue(depot.getTotalDepotValue());
        portfolio.setUniqueIdentifier(depot.getDepotNumber());
        portfolio.setType(depot.getPortfolioType());
        portfolio.setRawType(depot.getInvestmentAccountType());
        portfolio.setTotalProfit(depot.getTotalDepotValue() - depot.getInvestedAmount());

        String sessionId = session.get(IcaBankenConstants.IdTags.SESSION_ID_TAG);

        if (depot.getFundHoldings().isEmpty()) { return portfolio; }
        portfolio.setInstruments(
                depot.getFundHoldings().stream().map(
                        holdingEntity -> toInstrument( holdingEntity, client.getInstrument(holdingEntity.getFundId())
                                .getBody())
                        ).collect(Collectors.toList()));

        return portfolio;
    }

    public InvestmentAccount toInvestmentAccount(DepotsEntity depot){
        List<Portfolio> portfolios = new ArrayList<Portfolio>();
        portfolios.add(toPortfolio(depot));
        return InvestmentAccount.builder(depot.getDepotNumber(), Amount.inSEK(depot.getTotalDepotValue()))
                .setAccountNumber(depot.getDepotNumber())
                .setName(depot.getDepotName())
                .setPortfolios(portfolios)
                .setBankIdentifier(depot.getDepotNumber())
                .build();
    }

}
