package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.Map;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;

@JsonObject
public class SecuritiesPortfolioEntity extends AbstractContractDetailsEntity {

    private List<SecurityEntity> securities;
    private AmountEntity balance;

    @JsonIgnore
    public List<SecurityEntity> getSecurities() {
        return securities != null ? securities : List.empty();
    }

    public AmountEntity getBalance() {
        return balance;
    }

    public InvestmentAccount toInvestmentAccount(
            Double totalProfit, Map<String, Double> instrumentsProfit) {
        return InvestmentAccount.nxBuilder()
                .withPortfolios(getPortfolio(totalProfit, instrumentsProfit))
                .withZeroCashBalance(getCurrency().getId())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getId())
                                .withAccountNumber(getAccountNumber())
                                .withAccountName(getAccountName())
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifierType.TINK, getId()))
                                .build())
                .build();
    }

    @JsonIgnore
    @Override
    protected String getAccountNumber() {
        return getFormats().getBocf();
    }

    private PortfolioModule getPortfolio(
            Double totalProfit, Map<String, Double> instrumentsProfit) {
        return PortfolioModule.builder()
                .withType(PortfolioType.DEPOT)
                .withUniqueIdentifier(getId())
                .withCashValue(0)
                .withTotalProfit(totalProfit == null ? 0.00 : totalProfit)
                .withTotalValue(balance.toTinkAmount().getDoubleValue())
                .withInstruments(getInstruments(instrumentsProfit).asJava())
                .build();
    }

    private List<InstrumentModule> getInstruments(Map<String, Double> instrumentsProfit) {
        return Option.of(securities)
                .getOrElse(List.empty())
                .map(i -> i.toTinkInstrument(instrumentsProfit));
    }
}
