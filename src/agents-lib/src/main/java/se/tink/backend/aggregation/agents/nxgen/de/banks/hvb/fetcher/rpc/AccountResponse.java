package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.TransactionalAccount;
import se.tink.backend.aggregation.rpc.AccountTypes;
import se.tink.backend.core.Amount;
import se.tink.libraries.account.identifiers.IbanIdentifier;

@JsonObject
public final class AccountResponse {
    private static final Logger logger = LoggerFactory.getLogger(AccountResponse.class);

    private List<AccountEntity> accounts;

    public Collection<TransactionalAccount.Builder<?, ?>> getTransactionalAccounts() {
        return Optional.ofNullable(accounts).orElse(Collections.emptyList())
                .stream()
                .map(AccountResponse::toTransactionalAccount)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(Collectors.toSet());
    }

    private static Optional<AccountTypes> getAccountType(final AccountEntity accountEntity) {
        if (!accountEntity.getType().isPresent()) {
            return Optional.empty();
        }
        final String accountType = accountEntity.getType().get().trim();
        switch (accountType) {
        case "2":
            return Optional.of(AccountTypes.CHECKING);
        default:
            logger.warn("Received unknown account type: {}", accountType);
        }
        return Optional.empty();
    }

    private static Optional<TransactionalAccount.Builder<?, ?>> toTransactionalAccount(
            final AccountEntity accountEntity) {
        if (!accountEntity.getCurrency().isPresent()) {
            logger.warn("Could not find account currency");
            return Optional.empty();
        } else if (!accountEntity.getCurrentBalance().isPresent()) {
            logger.warn("Could not find current account balance");
            return Optional.empty();
        } else if (!accountEntity.getBic().isPresent()) {
            logger.warn("Could not find account BIC");
            return Optional.empty();
        } else if (!accountEntity.getIban().isPresent()) {
            logger.warn("Could not find account IBAN");
            return Optional.empty();
        } else if (!accountEntity.getNumber().isPresent()) {
            logger.warn("Could not find account number");
            return Optional.empty();
        } else if (!accountEntity.getTitle().isPresent()) {
            logger.warn("Could not find account title");
            return Optional.empty();
        }
        final Optional<AccountTypes> accountType = getAccountType(accountEntity);
        if (!accountType.isPresent()) {
            logger.warn("Could not find or recognize account type");
            return Optional.empty();
        }
        final Amount amount = new Amount(accountEntity.getCurrency().get().trim(),
                accountEntity.getCurrentBalance().get());
        final IbanIdentifier iban = new IbanIdentifier(accountEntity.getBic().get().trim(),
                accountEntity.getIban().get().trim());
        final String accountNumber = accountEntity.getNumber().get().trim();
        return Optional.of(TransactionalAccount.builder(accountType.get(), accountNumber, amount)
                .setAccountNumber(accountNumber)
                .addIdentifier(iban)
                .setName(accountEntity.getTitle().get().trim()));
    }
}
