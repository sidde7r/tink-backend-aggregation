package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.entities;

import java.math.BigDecimal;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Data
public final class AccountEntity {
    private String accountIdentifier;
    private String name;
    private double balance;
    private String currencyCode;
    private String limit;
    private AccountDetailsResponse accountDetails;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.CHECKING)
                .withoutFlags()
                .withBalance(getBalanceModule())
                .withId(getIdModule())
                .setApiIdentifier(accountIdentifier)
                .build();
    }

    private IdModule getIdModule() {
        String iban = accountDetails.getBody().getIban();
        return IdModule.builder()
                .withUniqueIdentifier(iban)
                .withAccountNumber(iban)
                .withAccountName(name)
                .addIdentifier(AccountIdentifier.create(AccountIdentifier.Type.IBAN, iban))
                .build();
    }

    private BalanceModule getBalanceModule() {
        BigDecimal bookedBalance =
                getBalance(
                        this.accountDetails.getBody().getSaldo().getBookedBalanceSymbol(),
                        this.accountDetails.getBody().getSaldo().getBookedBalance());
        BigDecimal availableBalance =
                getBalance(
                        this.accountDetails.getBody().getSaldo().getAvailableBalanceSymbol(),
                        this.accountDetails.getBody().getSaldo().getAvailableBalance());

        return BalanceModule.builder()
                .withBalance(ExactCurrencyAmount.of(bookedBalance, currencyCode))
                .setAvailableBalance(ExactCurrencyAmount.of(availableBalance, currencyCode))
                .setCreditLimit(ExactCurrencyAmount.of(limit, currencyCode))
                .build();
    }

    BigDecimal getBalance(String symbol, String balance) {
        return new BigDecimal(symbol + balance).divide(new BigDecimal("100"));
    }
}
