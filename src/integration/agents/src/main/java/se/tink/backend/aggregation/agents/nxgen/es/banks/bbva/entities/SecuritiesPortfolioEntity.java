package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.Collections;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class SecuritiesPortfolioEntity extends AbstractContractDetailsEntity {

    private List<SecurityEntity> securities;
    private AmountEntity balance;

    public List<SecurityEntity> getSecurities() {
        return securities;
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public InvestmentAccount toInvestmentAccount() {

        return InvestmentAccount.builder(getId())
                .setName(getProduct().getDescription())
                .setAccountNumber(getFormats().getBocf())
                .setHolderName(null)
                .setCashBalance(ExactCurrencyAmount.of(0d, getCurrency().getId()))
                .setPortfolios(getPortfolio())
                .build();
    }

    private java.util.List<Portfolio> getPortfolio() {
        Portfolio portfolio = new Portfolio();
        List<Instrument> instruments = getInstruments();

        portfolio.setUniqueIdentifier(getId());
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setTotalValue(balance.toTinkAmount().getDoubleValue());
        portfolio.setTotalProfit(0.00);
        portfolio.setInstruments(instruments.asJava());

        return Collections.singletonList(portfolio);
    }

    //    private Double getTotalProfit(List<Instrument> instruments) {
    //        return Option.of(instruments)
    //                .getOrElse(List.empty())
    //                .map(Instrument::getProfit)
    //                .sum()
    //                .doubleValue();
    //    }
    //
    private List<Instrument> getInstruments() {
        return Option.of(securities).getOrElse(List.empty()).map(SecurityEntity::toTinkInstrument);
    }
    //
    //    private Instrument toTinkInstrument(
    //            BbvaApiClient apiClient,
    //            se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities
    //                            .SecurityEntity
    //                    securityEntity) {
    //        return securityEntity.toTinkInstrument(
    //                apiClient, Instrument.Type.STOCK, id, securityEntity.getRicCode());
    //    }
}
