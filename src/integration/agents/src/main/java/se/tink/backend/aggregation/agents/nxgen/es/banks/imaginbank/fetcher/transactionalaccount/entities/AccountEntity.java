package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.ImaginBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.investment.InvestmentAccount;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.portfolio.PortfolioModule.PortfolioType;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.account.identifiers.formatters.DisplayAccountIdentifierFormatter;
import se.tink.libraries.amount.ExactCurrencyAmount;

@Slf4j
@JsonObject
public class AccountEntity {

    @JsonProperty("alias")
    private String accountType;

    @JsonProperty("saldoDisponible")
    private double availableBalance;

    @JsonProperty("moneda")
    private String currency;

    @JsonProperty("numeroCuenta")
    private AccountIdentifierEntity identifiers;

    public AccountIdentifierEntity getIdentifiers() {
        return identifiers;
    }

    @JsonIgnore
    public Optional<TransactionalAccount> toTinkAccount(List<Party> parties) {
        return TransactionalAccount.nxBuilder()
                .withTypeAndFlagsFrom(ImaginBankConstants.ACCOUNT_TYPE_MAPPER, accountType)
                .withBalance(getBalance())
                .withId(
                        IdModule.builder()
                                .withUniqueIdentifier(identifiers.getIban())
                                .withAccountNumber(formatIban(identifiers.getIban()))
                                .withAccountName(accountType)
                                .addIdentifier(new IbanIdentifier(identifiers.getIban()))
                                .build())
                .setBankIdentifier(identifiers.getIban())
                .addParties(parties)
                .putInTemporaryStorage(
                        ImaginBankConstants.TemporaryStorage.ACCOUNT_REFERENCE,
                        identifiers.getAccountReference())
                .build();
    }

    @JsonIgnore
    public Optional<InvestmentAccount> toTinkInvestmentAccount(List<Party> parties) {
        return Optional.of(
                InvestmentAccount.nxBuilder()
                        .withPortfolios(
                                PortfolioModule.builder()
                                        .withType(PortfolioType.PENSION)
                                        .withUniqueIdentifier(identifiers.getIban())
                                        .withCashValue(0)
                                        .withTotalProfit(0.00)
                                        .withTotalValue(availableBalance)
                                        .withoutInstruments()
                                        .build())
                        .withCashBalance(getAmount())
                        .withId(
                                IdModule.builder()
                                        .withUniqueIdentifier(identifiers.getIban())
                                        .withAccountNumber(formatIban(identifiers.getIban()))
                                        .withAccountName(accountType)
                                        .addIdentifier(new IbanIdentifier(identifiers.getIban()))
                                        .build())
                        .setBankIdentifier(identifiers.getIban())
                        .addParties(parties)
                        .build());
    }

    @JsonIgnore
    public boolean isTransactionalAccount() {
        return ImaginBankConstants.ACCOUNT_TYPE_MAPPER.translate(accountType).isPresent();
    }

    @JsonIgnore
    public boolean isInvestmentAccount() {
        return ImaginBankConstants.INVESTMENT_ACCOUNT_MAPPER.isOneOf(
                getAccountType(), InvestmentAccount.ALLOWED_ACCOUNT_TYPES);
    }

    @JsonIgnore
    public String getAccountType() {
        return accountType;
    }

    @JsonIgnore
    private BalanceModule getBalance() {
        return BalanceModule.of(getAmount());
    }

    @JsonIgnore
    private ExactCurrencyAmount getAmount() {
        return new ExactCurrencyAmount(BigDecimal.valueOf(availableBalance), currency);
    }

    @JsonIgnore
    private String formatIban(String iban) {
        return new DisplayAccountIdentifierFormatter()
                .apply(AccountIdentifier.create(AccountIdentifierType.IBAN, iban));
    }
}
