package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.compliance.account_capabilities.AccountCapabilities;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class CustodyAccountEntity {
    private static final String PENSION = "PENSION";

    private String accountNumber;
    private double cashAmount;
    private String classification;
    private String currency;
    private String id;
    private double marketValue;
    private String name;

    @JsonProperty("profit_loss")
    private double profit;

    @JsonProperty("profit_loss_percentage")
    private double profitPercentage;

    private List<CustodyHoldingEntity> holdings;

    public InvestmentAccount toTinkAccount() {
        List<InstrumentModule> instruments = Collections.emptyList();
        if (!isPension()) {
            instruments =
                    holdings.stream()
                            .map(CustodyHoldingEntity::toInstrument)
                            .filter(Optional::isPresent)
                            .map(Optional::get)
                            .collect(Collectors.toList());
        }

        return InvestmentAccount.nxBuilder()
                .withPortfolios(preparePortfolio(instruments))
                .withCashBalance(ExactCurrencyAmount.of(cashAmount, currency))
                .withId(prepareIdModule())
                // You can place funds on an Investment account (e.g. `cashAmount`),
                // but not execute or receive external transfers.
                // It's unknown if we can withdraw cash directly or if that requires an intermediate
                // account.
                .canPlaceFunds(AccountCapabilities.Answer.YES)
                .canWithdrawCash(AccountCapabilities.Answer.UNKNOWN)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.NO)
                .canExecuteExternalTransfer(AccountCapabilities.Answer.NO)
                .build();
    }

    private boolean isPension() {
        return PENSION.equals(classification);
    }

    private IdModule prepareIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(id)
                .withAccountNumber(accountNumber)
                .withAccountName(name)
                .addIdentifier(
                        AccountIdentifier.create(AccountIdentifier.Type.COUNTRY_SPECIFIC, id))
                .build();
    }

    private PortfolioModule preparePortfolio(List<InstrumentModule> instruments) {
        return PortfolioModule.builder()
                .withType(getPortfolioType())
                .withUniqueIdentifier(id)
                .withCashValue(cashAmount)
                .withTotalProfit(profit)
                .withTotalValue(marketValue)
                .withInstruments(instruments)
                .build();
    }

    private PortfolioModule.PortfolioType getPortfolioType() {
        if (isPension()) {
            return PortfolioModule.PortfolioType.PENSION;
        }
        return PortfolioModule.PortfolioType.DEPOT;
    }
}
