package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonUnwrapped;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.NordeaFiConstants;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v30.fetcher.transactionalaccount.entities.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.InvestmentAccount;
import se.tink.libraries.amount.Amount;
import se.tink.backend.system.rpc.Instrument;
import se.tink.backend.system.rpc.Portfolio;

@JsonObject
public class InvestmentAccountEntity {

    @JsonProperty("account_number")
    private String accountNumber;

    @JsonUnwrapped
    private AmountEntity balance;

    private String name;

    private String id;

    private List<HoldingEntity> holdings;

    @JsonProperty("profit_loss")
    private double profitLoss;

    public InvestmentAccount toTinkInvestmentAccount() {

        return InvestmentAccount.builder(id)
                .setCashBalance(new Amount(balance.getCurrency(), 0))
                .setBankIdentifier(id)
                .setAccountNumber(accountNumber)
                .setName(name)
                .setPortfolios(Collections.singletonList(getTinkPortfolio()))
                .build();

    }

    private Portfolio getTinkPortfolio() {

        String rawType = getRawType();

        Portfolio portfolio = new Portfolio();
        portfolio.setUniqueIdentifier(id);
        portfolio.setRawType(rawType);
        portfolio.setType(NordeaFiConstants.GET_PORTFOLIO_TYPE(rawType));
        portfolio.setTotalProfit(profitLoss);
        portfolio.setTotalValue(balance.getValue());
        portfolio.setInstruments(getInstruments());

        return portfolio;
    }

    private List<Instrument> getInstruments() {

        return holdings.stream()
                .map(HoldingEntity::toTinkInstrument)
                .collect(Collectors.toList());
    }

    private String getRawType() {

        // id format: TYPE:ACCOUNT
        return id.split(":")[0];
    }
}
