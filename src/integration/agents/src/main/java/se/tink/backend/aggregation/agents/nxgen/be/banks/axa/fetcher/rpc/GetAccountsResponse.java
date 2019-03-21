package se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.rpc;

import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.entities.OutputEntity;
import se.tink.backend.aggregation.agents.nxgen.be.banks.axa.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TypeMapper;
import se.tink.backend.aggregation.nxgen.core.account.transactional.CheckingAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.SavingsAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;

import java.util.Collection;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JsonObject
public final class GetAccountsResponse {
    private OutputEntity output;

    public Collection<TransactionalAccount> getAccounts() {
        return Optional.ofNullable(output)
                .map(OutputEntity::getAccounts)
                .map(Stream::of)
                .orElse(Stream.empty())
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
        final String accountNumber = iban.substring(4); // Removes leading "BE03"

        final Supplier<TransactionalAccount> checkingAccount =
                () ->
                        CheckingAccount.builder()
                                .setUniqueIdentifier(accountNumber)
                                .setAccountNumber(accountNumber)
                                .setBalance(new Amount(accountEntity.getCurrency(), balance))
                                .setAlias(accountEntity.getTypeDescription())
                                .addAccountIdentifier(new IbanIdentifier(iban))
                                .addHolderName(accountEntity.getTitularName())
                                .build();

        final Supplier<TransactionalAccount> savingsAccount =
                () ->
                        SavingsAccount.builder()
                                .setUniqueIdentifier(accountNumber)
                                .setAccountNumber(accountNumber)
                                .setBalance(new Amount(accountEntity.getCurrency(), balance))
                                .setAlias(accountEntity.getTypeDescription())
                                .addAccountIdentifier(new IbanIdentifier(iban))
                                .addHolderName(accountEntity.getTitularName())
                                .build();

        final TypeMapper<Supplier<TransactionalAccount>> accountTypeMapper =
                TypeMapper.<Supplier<TransactionalAccount>>builder()
                        .put(checkingAccount, "1047")
                        .put(savingsAccount, "0016", "1135")
                        .build();

        return accountTypeMapper.translate(accountEntity.getAccountType()).map(Supplier::get);
    }
}
