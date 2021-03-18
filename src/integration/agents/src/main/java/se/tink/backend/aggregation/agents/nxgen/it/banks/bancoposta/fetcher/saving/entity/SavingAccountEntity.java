package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.saving.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.util.Optional;
import lombok.Data;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.saving.rpc.SavingAccountDetailsResponse;
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
public final class SavingAccountEntity {
    private String iban;

    @JsonProperty("numeroRapporto")
    private String accountNumber;

    private SavingAccountDetailsResponse accountDetails;

    public Optional<TransactionalAccount> toTinkAccount() {
        return TransactionalAccount.nxBuilder()
                .withType(TransactionalAccountType.SAVINGS)
                .withoutFlags()
                .withBalance(getBalanceModule())
                .withId(getIdModule())
                .setApiIdentifier(accountNumber)
                .build();
    }

    private IdModule getIdModule() {
        return IdModule.builder()
                .withUniqueIdentifier(iban)
                .withAccountNumber(iban)
                .withAccountName("Conto di risparmio")
                .addIdentifier(AccountIdentifier.create(AccountIdentifierType.IBAN, iban))
                .build();
    }

    private BalanceModule getBalanceModule() {
        String currencyCode = getCurrencyCode();

        return BalanceModule.builder()
                .withBalance(
                        ExactCurrencyAmount.of(
                                new BigDecimal(
                                                accountDetails
                                                        .getBody()
                                                        .getSaldo()
                                                        .getBookedBalance())
                                        .movePointLeft(2),
                                currencyCode))
                .setAvailableBalance(
                        ExactCurrencyAmount.of(
                                new BigDecimal(
                                                accountDetails
                                                        .getBody()
                                                        .getSaldo()
                                                        .getAvailableBalance())
                                        .movePointLeft(2),
                                currencyCode))
                .build();
    }

    private String getCurrencyCode() {
        // No info about account currency, so currency is taken from one of the transactions,
        // default EUR
        return accountDetails.getBody().getTransactions().stream()
                .findAny()
                .map(transaction -> transaction.getCurrencyCode())
                .orElse("EUR");
    }
}
