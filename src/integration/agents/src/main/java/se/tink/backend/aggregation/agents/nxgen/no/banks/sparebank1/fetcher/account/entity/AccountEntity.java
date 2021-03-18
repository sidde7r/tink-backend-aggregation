package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.entity;

import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.AccountTypes.CURRENT_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.AccountTypes.DISPOSABLE_ACCOUNT;
import static se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.AccountTypes.SAVINGS_ACCOUNT;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1AmountUtils;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Tags;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.entity.Party;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Data
@Slf4j
public class AccountEntity {
    private String key;
    private String name;
    private String formattedNumber;
    private String disposableAmountInteger;
    private String disposableAmountFraction;
    private String balanceAmountInteger;
    private String balanceAmountFraction;
    private String currencyCode;
    private String accountType;
    private String owner;
    private String accountNumber;

    @JsonIgnore
    public Optional<TransactionalAccount> toTransactionalAccount(AccountDetailsResponse details) {
        return TransactionalAccount.nxBuilder()
                .withType(getTinkAccountType(accountType))
                .withoutFlags()
                .withBalance(buildBalanceModule(details.getAmount().getCreditLine()))
                .withId(buildIdModule())
                .setApiIdentifier(key)
                .addParties(new Party(details.getOwner(), Party.Role.HOLDER))
                .build();
    }

    private IdModule buildIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(accountNumber)
                .withAccountNumber(accountNumber)
                .withAccountName(name)
                .addIdentifier(AccountIdentifier.create(AccountIdentifierType.NO, accountNumber))
                .build();
    }

    private BalanceModule buildBalanceModule(BigDecimal creditLine) {
        return BalanceModule.builder()
                .withBalance(
                        ExactCurrencyAmount.of(
                                Sparebank1AmountUtils.constructDouble(
                                        balanceAmountInteger, balanceAmountFraction),
                                currencyCode))
                .setAvailableBalance(
                        ExactCurrencyAmount.of(
                                Sparebank1AmountUtils.constructDouble(
                                        disposableAmountInteger, disposableAmountFraction),
                                currencyCode))
                .setCreditLimit(ExactCurrencyAmount.of(creditLine.abs(), currencyCode))
                .build();
    }

    @JsonIgnore
    private TransactionalAccountType getTinkAccountType(String accountType) {
        if (accountType == null) {
            log.warn("AccountType not fetched for: " + name);
            return TransactionalAccountType.CHECKING;
        }

        switch (accountType.toLowerCase()) {
            case CURRENT_ACCOUNT:
            case DISPOSABLE_ACCOUNT:
                return TransactionalAccountType.CHECKING;
            case SAVINGS_ACCOUNT:
                return TransactionalAccountType.SAVINGS;
            default:
                log.info("{}: {} ({})", Tags.UNKNOWN_ACCOUNT_TYPE, accountType, name);
                return TransactionalAccountType.CHECKING;
        }
    }
}
