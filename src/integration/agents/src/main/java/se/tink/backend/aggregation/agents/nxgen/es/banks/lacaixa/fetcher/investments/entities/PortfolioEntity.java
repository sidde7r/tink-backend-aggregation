package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.Lists;
import java.util.List;
import se.tink.backend.aggregation.agents.models.Instrument;
import se.tink.backend.aggregation.agents.models.Portfolio;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class PortfolioEntity {
    @JsonProperty("idExpediente")
    private String id;

    private String alias;

    @JsonProperty("numeroExpediente")
    private String contractNumber;

    @JsonProperty("numeroExpediente28")
    private String accountNumber;

    private String cuentaAsociada;

    @JsonProperty("valoracion")
    private BalanceEntity currentValue;

    @JsonProperty("plusvalia")
    private BalanceEntity valueChange;

    private boolean mostrarPlusvaliaRentabilidad;
    private boolean favorito;

    public String getId() {
        return id;
    }

    public String getAlias() {
        return alias;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public BalanceEntity getCurrentValue() {
        return currentValue;
    }

    public BalanceEntity getValueChange() {
        return valueChange;
    }

    @JsonIgnore
    public InvestmentAccount toInvestmentAccount(
            HolderName holderName, List<Instrument> instruments) {
        return InvestmentAccount.builder(accountNumber)
                .setCashBalance(ExactCurrencyAmount.zero(currentValue.getCurrency()))
                .setAccountNumber(contractNumber)
                .setName(contractNumber)
                .setHolderName(holderName)
                .setPortfolios(getPortfolios(instruments))
                .build();
    }

    private List<Portfolio> getPortfolios(List<Instrument> instruments) {
        Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(accountNumber);
        portfolio.setCashValue(0.0);
        portfolio.setInstruments(instruments);
        portfolio.setType(Portfolio.Type.DEPOT);
        portfolio.setTotalProfit(valueChange.getAmount());
        portfolio.setTotalValue(currentValue.getAmount());

        return Lists.newArrayList(portfolio);
    }
}
