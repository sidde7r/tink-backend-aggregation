package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.RevolutConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.BalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.DayTradesEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.HoldingsItemEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.InfoEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.InvestmentResultEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.KycEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity.PortfolioResultEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class InvestmentAccountResponse {

    @JsonProperty("assetTypes")
    private List<String> assetTypes;

    @JsonProperty("dayTrades")
    private DayTradesEntity dayTradesEntity;

    @JsonProperty("reports")
    private List<Object> reports;

    @JsonProperty("performance")
    private List<Object> performance;

    @JsonProperty("kyc")
    private KycEntity kycEntity;

    @JsonProperty("balance")
    private BalanceEntity balanceEntity;

    @JsonProperty("id")
    private String id;

    @JsonProperty("state")
    private String state;

    @JsonProperty("holdings")
    private List<HoldingsItemEntity> holdings;

    @JsonProperty("info")
    private InfoEntity infoEntity;

    public String getAccountName() {
        return String.format("Revolut %s Investments", getHoldingsInfo().getCurrency());
    }

    public long getBalance() {
        return balanceEntity.getAmount();
    }

    public String getHolderName() {
        return String.format("%s %s", kycEntity.getFirstName(), kycEntity.getLastName());
    }

    public List<String> getHoldingNames() {
        return holdings.stream()
                .filter(HoldingsItemEntity::isValidInstrument)
                .map(HoldingsItemEntity::getId)
                .collect(Collectors.toList());
    }

    public List<HoldingsItemEntity> getHoldings() {
        return holdings.stream()
                .filter(HoldingsItemEntity::isValidInstrument)
                .collect(Collectors.toList());
    }

    public HoldingsItemEntity getHoldingsInfo() {
        return holdings.get(0);
    }

    private List<InstrumentModule> getInstruments(
            List<InvestmentResultEntity> investmentResultEntities) {
        return holdings.stream()
                .filter(HoldingsItemEntity::isValidInstrument)
                .map(item -> item.toTinkInstrument(investmentResultEntities))
                .collect(Collectors.toList());
    }

    private PortfolioModule getPortfolio(PortfolioResultEntity portfolioResultEntity) {
        return PortfolioModule.builder()
                .withType(PortfolioModule.PortfolioType.DEPOT)
                .withUniqueIdentifier(id)
                .withCashValue(portfolioResultEntity.availableFunds())
                .withTotalProfit(portfolioResultEntity.totalProfit())
                .withTotalValue(portfolioResultEntity.totalValue())
                .withInstruments(
                        getInstruments(portfolioResultEntity.getInvestmentResultEntities()))
                .build();
    }

    private ExactCurrencyAmount getInvestmentBalance() {
        return ExactCurrencyAmount.of(
                new BigDecimal(getBalance() / RevolutConstants.REVOLUT_AMOUNT_DIVIDER),
                balanceEntity.getCurrency());
    }

    public Collection<InvestmentAccount> toInvestmentAccounts(
            PortfolioResultEntity portfolioResultEntity) {

        return Arrays.asList(
                InvestmentAccount.nxBuilder()
                        .withPortfolios(getPortfolio(portfolioResultEntity))
                        .withCashBalance(getInvestmentBalance())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(id)
                                        .withAccountNumber(
                                                getHoldingsInfo().getDetailsEntity().getPocketId())
                                        .withAccountName(getAccountName())
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifierType.TINK,
                                                        id)) // No identifiers available on account
                                        .build())
                        .addHolderName(getHolderName())
                        .build());
    }
}
