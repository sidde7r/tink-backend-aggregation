package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vavr.collection.List;
import io.vavr.control.Option;
import java.util.Collections;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities.ComertialClassificationEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class StockAccountEntity {
    private String id;
    private String name;
    private String productDescription;
    private String productFamilyCode;
    private String subfamilyCode;
    private String subfamilyTypeCode;
    private String currency;
    private double currentBalance;
    private TransactionsEntity transactions;
    private String branch;
    private List<ComertialClassificationEntity> comertialClassifications;
    private List<SecurityEntity> securities;
    private List<ActionsEntity> actions;

    @JsonProperty("bocf")
    private String accountNumber;

    @JsonObject
    public InvestmentAccount toTinkAccount(BbvaApiClient apiClient, String holder) {
        final HolderName holderName = Option.of(holder).map(HolderName::new).getOrNull();

        return InvestmentAccount.builder(id)
                .setName(name)
                .setAccountNumber(accountNumber)
                .setHolderName(holderName)
                .setCashBalance(new Amount(currency, 0.0))
                .setPortfolios(getPortfolio(apiClient))
                .build();
    }

    private java.util.List<Portfolio> getPortfolio(BbvaApiClient apiClient) {
        Portfolio portfolio = new Portfolio();
        List<Instrument> instruments = getInstruments(apiClient);

        portfolio.setUniqueIdentifier(id);
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setTotalValue(currentBalance);
        portfolio.setTotalProfit(getTotalProfit(instruments));
        portfolio.setInstruments(instruments.toJavaList());

        return Collections.singletonList(portfolio);
    }

    private Double getTotalProfit(List<Instrument> instruments) {
        return Option.of(instruments)
                .getOrElse(List.empty())
                .map(Instrument::getProfit)
                .sum()
                .doubleValue();
    }

    private List<Instrument> getInstruments(BbvaApiClient apiClient) {
        return Option.of(securities)
                .getOrElse(List.empty())
                .map(securityEntity -> toTinkInstrument(apiClient, securityEntity));
    }

    private Instrument toTinkInstrument(BbvaApiClient apiClient, SecurityEntity securityEntity) {
        return securityEntity.toTinkInstrument(
                apiClient, Instrument.Type.STOCK, id, securityEntity.getRicCode());
    }
}
