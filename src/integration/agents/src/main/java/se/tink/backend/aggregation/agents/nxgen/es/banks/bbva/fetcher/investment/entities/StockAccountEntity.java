package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaApiClient;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
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
    private List<ComertialClassificationsEntity> comertialClassifications;
    private List<SecurityEntity> securities;
    private List<ActionsEntity> actions;
    @JsonProperty("bocf")
    private String accountNumber;

    @JsonObject
    public InvestmentAccount toTinkAccount(BbvaApiClient apiClient, String holderName) {
        HolderName holder = null;
        if (!Strings.isNullOrEmpty(holderName)) {
            holder = new HolderName(holderName);
        }

        return InvestmentAccount.builder(id)
                .setName(name)
                .setAccountNumber(accountNumber)
                .setHolderName(holder)
                .setCashBalance(new Amount(currency, 0.0))
                .setPortfolios(getPortfolio(apiClient))
                .build();
    }

    private List<Portfolio> getPortfolio(BbvaApiClient apiClient) {
        Portfolio portfolio = new Portfolio();
        List<Instrument> instruments = getInstruments(apiClient);

        portfolio.setUniqueIdentifier(id);
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setTotalValue(currentBalance);
        portfolio.setTotalProfit(getTotalProfit(instruments));
        portfolio.setInstruments(instruments);

        return Collections.singletonList(portfolio);
    }

    private Double getTotalProfit(List<Instrument> instruments) {
        return Optional.ofNullable(instruments).orElse(Collections.emptyList()).stream()
                .mapToDouble(Instrument::getProfit)
                .sum();
    }

    private List<Instrument> getInstruments(BbvaApiClient apiClient) {
        return Optional.ofNullable(securities).orElse(Collections.emptyList()).stream()
                .map(securityEntity -> toTinkInstrument(apiClient, securityEntity))
                .collect(Collectors.toList());
    }

    private Instrument toTinkInstrument(BbvaApiClient apiClient, SecurityEntity securityEntity) {
        return securityEntity.toTinkInstrument(apiClient, Instrument.Type.STOCK, id, securityEntity.getRicCode());
    }
}
