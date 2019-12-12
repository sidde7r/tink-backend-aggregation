package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
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


    public InvestmentAccount toInvestmentAccount(Double totalProfit) {
        return InvestmentAccount.builder(getId())
                .setName(getAccountName())
                .setAccountNumber(getAccountNumber())
                .setHolderName(null)
                .setCashBalance(ExactCurrencyAmount.of(0d, getCurrency().getId()))
                .setPortfolios(getPortfolio(totalProfit))
                .build();
    }

    @JsonIgnore
    @Override
    protected String getAccountNumber() {
        return getFormats().getBocf();
    }

    private java.util.List<Portfolio> getPortfolio(Double totalProfit) {
        Portfolio portfolio = new Portfolio();
        List<Instrument> instruments = getInstruments();

        portfolio.setUniqueIdentifier(getId());
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setTotalValue(balance.toTinkAmount().getDoubleValue());
        portfolio.setTotalProfit(totalProfit);
        portfolio.setInstruments(instruments.asJava());

        return Collections.singletonList(portfolio);
    }

    private List<Instrument> getInstruments() {
        return Option.of(securities).getOrElse(List.empty()).map(SecurityEntity::toTinkInstrument);
    }
}
