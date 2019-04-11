package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.EngagementResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.rpc.FundDetailsResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.Amount;

@JsonObject
public class FundsListEntity {
    @JsonProperty("expediente")
    private String accountId;

    @JsonProperty("titulares")
    private List<String> holders;

    @JsonProperty("titularitatConjunta")
    private boolean jointOwnership;

    private boolean noOperarExpediente;

    @JsonProperty("tablaFondos")
    private List<FundEntity> fundsList;

    @JsonProperty("totalExpediente")
    private BalanceEntity totalBalance;

    @JsonIgnore
    public Optional<InvestmentAccount> toTinkInvestment(
            LaCaixaApiClient apiClient, HolderName holderName, EngagementResponse engagements) {
        if (totalBalance == null) {
            return Optional.empty();
        }

        Optional<String> contractName = engagements.getContractName(accountId);
        String investmentName = contractName.orElse(accountId);
        List<Instrument> instruments = getInstruments(apiClient);

        Portfolio portfolio = getPortfolio(instruments);

        return Optional.of(
                InvestmentAccount.builder(sanitizeUniqueIdentifier())
                        .setAccountNumber(accountId)
                        .setHolderName(holderName)
                        .setName(investmentName)
                        .setCashBalance(Amount.valueOf(totalBalance.getCurrency(), 0, 2))
                        .setPortfolios(Lists.newArrayList(portfolio))
                        .build());
    }

    @JsonIgnore
    private List<Instrument> getInstruments(LaCaixaApiClient apiClient) {
        List<Instrument> instruments = new ArrayList<>();
        Optional.ofNullable(fundsList)
                .orElse(Collections.emptyList())
                .forEach(
                        fund -> {
                            FundDetailsResponse fundDetails =
                                    apiClient.fetchFundDetails(
                                            fund.getFundReference(),
                                            fund.getFundCode(),
                                            fund.getCurrency());
                            instruments.add(fundDetails.toTinkInstruments());
                        });
        return instruments;
    }

    @JsonIgnore
    private Portfolio getPortfolio(List<Instrument> instruments) {
        Portfolio portfolio = new Portfolio();
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setTotalProfit(getTotalProfit(instruments));
        portfolio.setUniqueIdentifier(sanitizeUniqueIdentifier());
        portfolio.setTotalValue(totalBalance.doubleValue());
        portfolio.setInstruments(instruments);
        portfolio.setCashValue(0.0);
        return portfolio;
    }

    @JsonIgnore
    private Double getTotalProfit(List<Instrument> instruments) {
        return instruments.stream().mapToDouble(Instrument::getProfit).sum();
    }

    @JsonIgnore
    private String sanitizeUniqueIdentifier() {
        return accountId.replaceAll("[^\\dA-Za-z]", "");
    }
}
