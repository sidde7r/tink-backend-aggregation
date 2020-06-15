package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
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

    private String accountNumber;
    private double cashAmount;
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

        List<InstrumentModule> instruments =
                holdings.stream()
                        .map(CustodyHoldingEntity::toInstrument)
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .collect(Collectors.toList());
        IdModule idModule =
                IdModule.builder()
                        .withUniqueIdentifier(id)
                        .withAccountNumber(accountNumber)
                        .withAccountName(name)
                        .addIdentifier(
                                AccountIdentifier.create(
                                        AccountIdentifier.Type.COUNTRY_SPECIFIC, id))
                        .build();
        PortfolioModule portfolioModule =
                PortfolioModule.builder()
                        .withType(PortfolioModule.PortfolioType.DEPOT)
                        .withUniqueIdentifier(id)
                        .withCashValue(cashAmount)
                        .withTotalProfit(profit)
                        .withTotalValue(marketValue)
                        .withInstruments(instruments)
                        .build();
        return InvestmentAccount.nxBuilder()
                .withPortfolios(portfolioModule)
                .withCashBalance(ExactCurrencyAmount.of(cashAmount, currency))
                .withId(idModule)
                // You can place and withdraw funds from an Investment account (e.g. `cashAmount`),
                // but not make or receive domestic transfers.
                .canPlaceFunds(AccountCapabilities.Answer.YES)
                .canWithdrawCash(AccountCapabilities.Answer.YES)
                .canReceiveExternalTransfer(AccountCapabilities.Answer.NO)
                .canExecuteExternalTransfer(AccountCapabilities.Answer.NO)
                .build();
    }
}
