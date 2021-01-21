package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.fetcher.investment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaConstants.PortfolioTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.HolderName;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class PortfolioEntity {
    private static final String INVALID_CHARACTERS_PATTERN =
            "[^\\p{L}\\p{M}\\p{N}\\p{P}\\p{Z}\\p{Cf}\\p{Cs}\\s]";

    @JsonProperty("instrumentPositions")
    private List<InstrumentEntity> instruments;

    private double totalProfit;
    private double totalOwnCapital;
    private double totalBuyingPower;
    private double totalProfitPercent;
    private double totalBalance;
    private String accountName;
    private String accountType;
    private boolean depositable;
    private String accountId;

    public List<InstrumentEntity> getInstruments() {
        return Optional.ofNullable(instruments).orElseGet(Collections::emptyList);
    }

    public double getTotalProfit() {
        return totalProfit;
    }

    public double getTotalOwnCapital() {
        return totalOwnCapital;
    }

    public double getTotalBuyingPower() {
        return totalBuyingPower;
    }

    public double getTotalProfitPercent() {
        return totalProfitPercent;
    }

    public double getTotalBalance() {
        return totalBalance;
    }

    public String getAccountName(String clusterId) {
        // This is to remove all emojis and flags.
        // Will will have it like this until the database can handle does unicode.
        accountName = accountName.replaceAll(INVALID_CHARACTERS_PATTERN, "");
        if (clusterId.contains(AvanzaConstants.CLUSTER_ID_NESTON)
                && accountId.contains(accountName)) {
            return String.format("%s %s", accountName, getPortfolioType());
        }
        return accountName;
    }

    public String getAccountType() {
        return accountType;
    }

    public boolean isDepositable() {
        return depositable;
    }

    public String getAccountId() {
        return accountId;
    }

    @JsonIgnore
    public PortfolioModule toTinkPortfolioModule(List<InstrumentModule> instruments) {
        final double totalValue = totalOwnCapital - totalBalance;

        return PortfolioModule.builder()
                .withType(getPortfolioType())
                .withUniqueIdentifier(accountId)
                .withCashValue(totalBalance)
                .withTotalProfit(totalProfit)
                .withTotalValue(totalValue)
                .withInstruments(instruments)
                .setRawType(accountType)
                .build();
    }

    public InvestmentAccount toTinkInvestmentAccount(
            HolderName holderName,
            String clearingNumber,
            List<InstrumentModule> instruments,
            String clusterId) {
        return toTinkInvestmentAccount(
                holderName, clearingNumber, toTinkPortfolioModule(instruments), clusterId);
    }

    private InvestmentAccount toTinkInvestmentAccount(
            HolderName holderName,
            String clearingNumber,
            PortfolioModule portfolio,
            String clusterId) {
        final double interestPayable = totalOwnCapital - portfolio.getTotalValue() - totalBalance;
        final String accountNumber =
                clearingNumber != null
                        ? String.format("%s-%s", clearingNumber, accountId)
                        : accountId;

        return InvestmentAccount.nxBuilder()
                .withPortfolios(portfolio)
                .withCashBalance(ExactCurrencyAmount.inSEK(totalBalance + interestPayable))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(getAccountId())
                                .withAccountNumber(accountNumber)
                                .withAccountName(getAccountName(clusterId))
                                .addIdentifier(
                                        AccountIdentifier.create(
                                                AccountIdentifier.Type.SE, accountId))
                                .setProductName(portfolio.getRawType())
                                .build())
                .addHolderName(holderName.toString())
                .setApiIdentifier(getAccountId())
                .build();
    }

    @JsonIgnore
    private PortfolioModule.PortfolioType getPortfolioType() {
        switch (getAccountType().toLowerCase()) {
            case PortfolioTypes.INVESTERINGSSPARKONTO:
                return PortfolioModule.PortfolioType.ISK;
            case PortfolioTypes.AKTIEFONDKONTO:
                return PortfolioModule.PortfolioType.DEPOT;
            case PortfolioTypes.TJANSTEPENSION:
            case PortfolioTypes.PENSIONSFORSAKRING:
            case PortfolioTypes.IPS:
            case PortfolioTypes.AVTALS_PENSION:
                return PortfolioModule.PortfolioType.PENSION;
            case PortfolioTypes.KAPITALFORSAKRING:
            case PortfolioTypes.KAPITALFORSAKRING_BARN:
                return PortfolioModule.PortfolioType.KF;
            default:
                return PortfolioModule.PortfolioType.OTHER;
        }
    }
}
