package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.fetcher.rpc.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.instrument.InstrumentModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Getter
@JsonInclude(Include.NON_NULL)
@JsonObject
public class AccountEntity {

    @JsonProperty("accno")
    private String accountNumber;

    @JsonProperty("accid")
    private String accountId;

    @JsonProperty("bank_accno")
    private String bankAccountNumber;

    private String type;

    private String symbol;

    @JsonProperty("account_code")
    private String accountCode;

    private String role;

    @JsonProperty("default")
    private boolean jsonMemberDefault;

    @JsonProperty("alias")
    private String alias; // Default alias is the user's name, they can change this

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
        return NordnetBaseConstants.getAccountTypeMapper()
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
    private PortfolioModule.PortfolioType getPortfolioType() {
        return NordnetBaseConstants.getPortfolioTypeMapper()
                .translate(accountCode)
                .orElse(PortfolioModule.PortfolioType.OTHER);
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(
            ExactCurrencyAmount balance, String userFullName) {

        return TransactionalAccount.nxBuilder()
                .withType(convertAccountType())
                .withoutFlags()
                .withBalance(BalanceModule.of(balance))
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(accountNumber)
                                .withAccountNumber(bankAccountNumber)
                                .withAccountName(alias)
                                .addIdentifier(new SwedishIdentifier(bankAccountNumber))
                                .build())
                .addHolderName(userFullName)
                .setBankIdentifier(accountNumber)
                .setApiIdentifier(accountNumber)
                .build();
    }

    @JsonIgnore
    public Optional<InvestmentAccount> toInvestmentAccount(
            AccountInfoEntity accountInfoEntity, PortfolioModule portfolio, String userFullName) {

        return Optional.of(
                InvestmentAccount.nxBuilder()
                        .withPortfolios(portfolio)
                        .withCashBalance(getCashBalance(accountInfoEntity))
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(accountNumber)
                                        .withAccountNumber(
                                                bankAccountNumber == null
                                                        ? accountNumber
                                                        : bankAccountNumber)
                                        .withAccountName(alias)
                                        .addIdentifier(
                                                AccountIdentifier.create(
                                                        AccountIdentifierType.SE,
                                                        bankAccountNumber))
                                        .build())
                        .addHolderName(userFullName)
                        .build());
    }

    @JsonIgnore
    public PortfolioModule toPortfolioModule(
            AccountInfoEntity accountInfoEntity, List<InstrumentModule> instruments) {
        return PortfolioModule.builder()
                .withType(getPortfolioType())
                .withUniqueIdentifier(accountNumber)
                .withCashValue(getCashBalance(accountInfoEntity).getDoubleValue())
                .withTotalProfit(calculateInstrumentsProfit(instruments))
                .withTotalValue(accountInfoEntity.getFullMarketValue().getDoubleValue())
                .withInstruments(instruments)
                .setRawType(accountCode)
                .build();
    }

    private Double calculateInstrumentsProfit(List<InstrumentModule> instruments) {
        return instruments.stream().map(InstrumentModule::getProfit).reduce(Double::sum).orElse(0d);
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
                BigDecimal.valueOf(balance + cashCollateral),
                accountInfoEntity.getFullMarketValue().getCurrencyCode());
    }
}
