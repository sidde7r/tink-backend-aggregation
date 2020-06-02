package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountEntity {

    @JsonProperty("default")
    private boolean jsonMemberDefault;

    @JsonProperty("bank_accno")
    private String bankAccno;

    @JsonProperty("account_code")
    private String accountCode;

    private String symbol;

    private String role;

    private String accno;

    private String accid;

    private String alias;

    private String type;

    public String getAccid() {
        return accid;
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return getAccountType().equals(AccountTypes.CHECKING)
                || getAccountType().equals(AccountTypes.SAVINGS);
    }

    @JsonIgnore
    public boolean isInvestmentAccount() {
        return getAccountType().equals(AccountTypes.INVESTMENT)
                || getAccountType().equals(AccountTypes.PENSION);
    }

    @JsonIgnore
    private AccountTypes getAccountType() {
        return NordnetConstants.getAccountTypeMapper()
                .translate(symbol)
                .orElseThrow(() -> new IllegalStateException("Could not map account"));
    }

    @JsonIgnore
    private TransactionalAccountType convertAccountType() {
        return (getAccountType().equals(AccountTypes.CHECKING))
                ? TransactionalAccountType.CHECKING
                : TransactionalAccountType.SAVINGS;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(ExactCurrencyAmount balance) {

        return TransactionalAccount.nxBuilder()
                .withType(convertAccountType())
                .withoutFlags() // TODO unsure about this
                .withBalance(BalanceModule.of(balance))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accno)
                                .withAccountNumber(bankAccno)
                                .withAccountName(alias)
                                .addIdentifier(new SwedishIdentifier(accno))
                                .build())
                .setBankIdentifier(accno)
                .setApiIdentifier(accno)
                .build();
    }

    @JsonIgnore
    public Optional<InvestmentAccount> toInvestmentAccount(
            AccountInfoEntity accountInfoEntity, PortfolioModule portfolio) {

        return Optional.of(
                InvestmentAccount.nxBuilder()
                        .withPortfolios(portfolio)
                        .withCashBalance(getCashBalance(accountInfoEntity))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accno)
                                        .withAccountNumber(bankAccno == null ? accno : bankAccno)
                                        .withAccountName(alias)
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifier.Type.SE, accno))
                                        .build())
                        .build());
    }

    @JsonIgnore
    public PortfolioModule toPortfolioModule(
            AccountInfoEntity accountInfoEntity, List<InstrumentModule> instruments) {
        return PortfolioModule.builder()
                .withType(getPortfolioType())
                .withUniqueIdentifier(accno)
                .withCashValue(getCashBalance(accountInfoEntity).getDoubleValue())
                .withTotalProfit(getTotalProfit(instruments))
                .withTotalValue(accountInfoEntity.getFullMarketValue().getDoubleValue())
                .withInstruments(instruments)
                .setRawType(accountCode)
                .build();
    }

    @JsonIgnore
    private PortfolioModule.PortfolioType getPortfolioType() {
        switch (Strings.nullToEmpty(accountCode).toLowerCase()) {
            case "dep":
                return PortfolioModule.PortfolioType.DEPOT;
            case "isk":
                return PortfolioModule.PortfolioType.ISK;
            case "kf":
                return PortfolioModule.PortfolioType.KF;
            case "tjf":
                return PortfolioModule.PortfolioType.PENSION;
            default:
                return (isTypeOccupationalPension()
                        ? PortfolioModule.PortfolioType.PENSION
                        : PortfolioModule.PortfolioType.OTHER);
        }
    }

    @JsonIgnore
    private boolean isTypeOccupationalPension() {
        return Strings.nullToEmpty(type).toLowerCase().contains("btp1");
    }

    /**
     * The amount of cash in the account minus what is borrowed, but those numbers are not directly
     * available. Had to guess a bit here. The exact meaning of 'pawn value', 'loan limit',
     * 'collateral', 'account credit' and others is not known.
     */
    @JsonIgnore
    private ExactCurrencyAmount getCashBalance(AccountInfoEntity accountInfoEntity) {

        // Essentially 'available cash' + 'remaining credit'
        double tradingPower = accountInfoEntity.getTradingPower().getValue().doubleValue();

        // Approwed/full credit (Possible alternative here is the 'loan limit')
        double pawnValue = accountInfoEntity.getPawnValue().getValue().doubleValue();

        // This should be: 'available cash' - 'actually utilised credit'
        double balance = tradingPower - pawnValue;

        // If this account/portfolio is used as collateral for some loan, and the market value of
        // the investments don't
        // cover the amount, then the trading power has been decreased by the remaining part.
        // (Not 100% sure about the logic here.)
        double cashCollateral =
                Math.max(
                        accountInfoEntity.getCollateral().getValue().doubleValue()
                                - accountInfoEntity.getFullMarketValue().getDoubleValue(),
                        0.0);

        return new ExactCurrencyAmount(
                new BigDecimal(balance + cashCollateral),
                accountInfoEntity.getFullMarketValue().getCurrencyCode());
    }

    @JsonIgnore
    private double getTotalProfit(List<InstrumentModule> instruments) {
        return instruments.stream()
                .filter(a -> a.getProfit() != null)
                .map(InstrumentModule::getProfit)
                .mapToDouble(Double::doubleValue)
                .sum();
    }
}
