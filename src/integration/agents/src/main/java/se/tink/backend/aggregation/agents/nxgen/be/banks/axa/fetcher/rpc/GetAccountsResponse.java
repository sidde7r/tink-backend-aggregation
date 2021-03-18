package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.OutputEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.balance.BalanceModule;
import se.tink.backend.aggregation.nxgen.core.account.nxbuilders.modules.id.IdModule;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccountType;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public final class GetAccountsResponse {
    private OutputEntity output;

    public Collection<TransactionalAccount> getAccounts() {
        return Optional.ofNullable(output)
                .map(OutputEntity::getAccounts)
                .map(Stream::of)
                .orElseGet(Stream::empty)
                .flatMap(Collection::stream)
                .filter(Objects::nonNull)
                .map(GetAccountsResponse::toTransactionalAccount)
                .flatMap(o -> o.map(Stream::of).orElseGet(Stream::empty))
                .collect(Collectors.toSet());
    }

    private static Optional<TransactionalAccount> toTransactionalAccount(
            final AccountEntity accountEntity) {
        final double balance = Double.parseDouble(accountEntity.getBalance());
        final String displayIban = accountEntity.getAccountNumber();
        final String iban = displayIban.replaceAll(" ", "");

        final Supplier<TransactionalAccount> checkingAccount =
                () ->
                        TransactionalAccount.nxBuilder()
                                .withType(TransactionalAccountType.CHECKING)
                                .withInferredAccountFlags()
                                .withBalance(
                                        BalanceModule.builder()
                                                .withBalance(
                                                        ExactCurrencyAmount.of(
                                                                balance,
                                                                accountEntity.getCurrency()))
                                                .build())
                                .withId(
                                        IdModule.builder()
                                                .withUniqueIdentifier(iban)
                                                .withAccountNumber(iban)
                                                .withAccountName(accountEntity.getTypeDescription())
                                                .addIdentifier(
                                                        AccountIdentifier.create(
                                                                AccountIdentifierType.IBAN, iban))
                                                .build())
                                .addHolderName(accountEntity.getTitularName())
                                .build()
                                .orElseThrow(IllegalStateException::new);

        final Supplier<TransactionalAccount> savingsAccount =
                () ->
                        TransactionalAccount.nxBuilder()
                                .withType(TransactionalAccountType.SAVINGS)
                                .withInferredAccountFlags()
                                .withBalance(
                                        BalanceModule.builder()
                                                .withBalance(
                                                        ExactCurrencyAmount.of(
                                                                balance,
                                                                accountEntity.getCurrency()))
                                                .build())
                                .withId(
                                        IdModule.builder()
                                                .withUniqueIdentifier(iban)
                                                .withAccountNumber(iban)
                                                .withAccountName(accountEntity.getTypeDescription())
                                                .addIdentifier(
                                                        AccountIdentifier.create(
                                                                AccountIdentifierType.IBAN, iban))
                                                .build())
                                .addHolderName(accountEntity.getTitularName())
                                .build()
                                .orElseThrow(IllegalStateException::new);

        final TypeMapper<Supplier<TransactionalAccount>> accountTypeMapper =
                TypeMapper.<Supplier<TransactionalAccount>>builder()
                        .put(checkingAccount, "0002", "0340", "1047")
                        .put(savingsAccount, "0015", "0016", "0386", "1135")
                        .build();

        return accountTypeMapper.translate(accountEntity.getAccountType()).map(Supplier::get);
    }
}
