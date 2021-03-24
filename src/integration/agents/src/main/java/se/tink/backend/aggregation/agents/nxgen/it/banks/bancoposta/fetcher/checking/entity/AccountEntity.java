package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.checking.rpc.AccountDetailsResponse;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
@Data
public final class AccountEntity {
    private static final String CURRENCY = "EUR";

    @JsonProperty("numeroConto")
    private String accountIdentifier;

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
                .withAccountName("Conto")
                .addIdentifier(AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
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
                // No information about currency in responses, so EUR used as default
                .withBalance(ExactCurrencyAmount.of(bookedBalance, CURRENCY))
                .setAvailableBalance(ExactCurrencyAmount.of(availableBalance, CURRENCY))
                .build();
    }

    BigDecimal getBalance(String symbol, String balance) {
        return new BigDecimal(symbol + balance).movePointLeft(2);
    }
}
