package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.HoldingsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.InvestmentResultEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.PortfolioResultEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.StocksItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc.InvestmentAccountResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc.StockInfoResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc.StockPriceResponse;

public final class RevolutInvestmentHelper {

    public static PortfolioResultEntity calculateResult(
            StockInfoResponse stockInfoResponse,
            StockPriceResponse stockPriceResponse,
            InvestmentAccountResponse accountsResponse) {
        List<InvestmentResultEntity> result = new ArrayList<>();
        for (StocksItemEntity item : stockInfoResponse.getStocks()) {

            Optional<HoldingsItemEntity> holdingsItem =
                    accountsResponse.getHoldings().stream()
                            .filter(holding -> holding.getId().equals(item.getTicker()))
                            .findFirst();

            if (!holdingsItem.isPresent()) {
                continue;
            }

            List<InvestmentResultEntity> list =
                    stockPriceResponse.stream()
                            .filter(
                                    price ->
                                            price.getInstrument()
                                                    .equalsIgnoreCase(item.getTicker()))
                            .map(
                                    price ->
                                            new InvestmentResultEntity(
                                                    item, price, holdingsItem.get()))
                            .collect(Collectors.toList());
            result.addAll(list);
        }

        return new PortfolioResultEntity(
                accountsResponse.getHoldingsInfo().getBalanceEntity().getAmount(), result);
    }
}
