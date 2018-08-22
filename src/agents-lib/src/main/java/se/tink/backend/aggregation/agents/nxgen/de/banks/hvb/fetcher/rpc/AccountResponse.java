package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
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
        return Collections.unmodifiableCollection(
                accounts.stream()
                        .map(AccountResponse::toTransactionalAccount)
                        .collect(Collectors.toSet()));
    }

    private static AccountTypes getAccountType(final AccountEntity accountEntity) {
        final String accountType = accountEntity.getType().trim();
        switch (accountType) {
        case "2":
            return AccountTypes.CHECKING;
        default:
            logger.info("Account type was: {}; interpreting this as a savings account", accountType);
            return AccountTypes.SAVINGS;
        }
    }

    private static TransactionalAccount.Builder<?, ?> toTransactionalAccount(final AccountEntity accountEntity) {
        final Amount amount = new Amount(accountEntity.getCurrency().trim(), accountEntity.getCurrentBalance());
        final IbanIdentifier iban = new IbanIdentifier(accountEntity.getBic().trim(), accountEntity.getIban().trim());
        final AccountTypes accountType = getAccountType(accountEntity);
        final String accountNumber = accountEntity.getNumber().trim();
        return TransactionalAccount.builder(accountType, accountNumber, amount)
                .setAccountNumber(accountNumber)
                .addIdentifier(iban)
                .setName(accountEntity.getTitle().trim());
    }
}
