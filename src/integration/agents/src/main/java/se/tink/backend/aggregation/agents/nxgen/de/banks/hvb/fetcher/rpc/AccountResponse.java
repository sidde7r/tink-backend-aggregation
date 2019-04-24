package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.rpc;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.assertj.core.util.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.HVBConstants;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.entities.AccountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.libraries.account.identifiers.IbanIdentifier;
import se.tink.libraries.amount.Amount;
import se.tink.libraries.serialization.utils.SerializationUtils;

@JsonObject
public final class AccountResponse {
    private static final Logger logger = LoggerFactory.getLogger(AccountResponse.class);

    private List<AccountEntity> accounts;

    public AccountResponse() {}

    public AccountResponse(final List<AccountEntity> accountEntities) {
        accounts = accountEntities;
    }

    public Collection<TransactionalAccount.Builder<?, ?>> getTransactionalAccounts() {
        return Optional.ofNullable(accounts).orElseGet(Collections::emptyList).stream()
                .filter(AccountResponse::validateAndLogAccountType)
                .map(AccountResponse::toTransactionalAccount)
                .collect(Collectors.toSet());
    }

    /**
     * @return true iff the account type is recognized. Logs an error if the account type was not
     *     found.
     */
    private static boolean validateAndLogAccountType(final AccountEntity accountEntity) {
        final Optional<AccountTypes> accountType = entityToAccountType(accountEntity);
        if (!accountType.isPresent()) {
            logger.error(
                    "{} - Could not figure out account type for account entity {}",
                    HVBConstants.LogTags.HVB_UNRECOGNIZED_ACCOUNT_TYPE.toTag(),
                    SerializationUtils.serializeToString(accountEntity));
            return false;
        }
        return true;
    }

    private static AccountTypes getAccountType(final AccountEntity accountEntity) {
        return entityToAccountType(accountEntity).orElseThrow(IllegalStateException::new);
    }

    private static Optional<AccountTypes> entityToAccountType(final AccountEntity accountEntity) {
        final String accountTypeString = Preconditions.checkNotNull(accountEntity.getType()).trim();
        final Optional<AccountTypes> type = stringToAccountType(accountTypeString);
        if (!type.isPresent()) {
            logger.warn(
                    "{} - Received unknown account type: {} for account entity {}",
                    HVBConstants.LogTags.HVB_UNKNOWN_ACCOUNT_TYPE.toTag(),
                    accountTypeString,
                    SerializationUtils.serializeToString(accountEntity));
            final String title =
                    Optional.ofNullable(accountEntity.getTitle()).map(String::trim).orElse("");
            return titleToAccountType(title);
        }
        return type;
    }

    private static Optional<AccountTypes> titleToAccountType(@Nonnull final String accountTitle) {
        final String upperCaseTitle = accountTitle.toUpperCase();
        if (HVBConstants.CHECKING_ACCOUNT_TITLE_SUBSTRINGS.stream()
                .anyMatch(upperCaseTitle::contains)) {
            return Optional.of(AccountTypes.CHECKING);
        } else if (HVBConstants.SAVINGS_ACCOUNT_TITLE_SUBSTRINGS.stream()
                .anyMatch(upperCaseTitle::contains)) {
            return Optional.of(AccountTypes.SAVINGS);
        }
        return Optional.empty();
    }

    private static Optional<AccountTypes> stringToAccountType(@Nonnull final String accountType) {
        switch (accountType) {
            case "2":
                return Optional.of(AccountTypes.CHECKING);
            case "6":
                return Optional.of(AccountTypes.SAVINGS);
            default:
                return Optional.empty();
        }
    }

    @Nullable
    private static String trimNullable(@Nullable final String string) {
        return string == null ? null : string.trim();
    }

    private static TransactionalAccount.Builder<?, ?> toTransactionalAccount(
            final AccountEntity accountEntity) {
        final String currency = trimNullable(accountEntity.getCurrency());
        final Double balance = accountEntity.getCurrentBalance();
        final String bic = Preconditions.checkNotNull(accountEntity.getBic());
        final String iban = Preconditions.checkNotNull(accountEntity.getIban());
        final String accountNumber = Preconditions.checkNotNull(accountEntity.getNumber());
        final String accountName = trimNullable(accountEntity.getTitle());
        final AccountTypes accountType = getAccountType(accountEntity);

        final Amount amount = new Amount(currency, balance);
        final IbanIdentifier ibanIdentifier = new IbanIdentifier(bic, iban);
        return TransactionalAccount.builder(accountType, accountNumber, amount)
                .setAccountNumber(accountNumber)
                .addIdentifier(ibanIdentifier)
                .setName(accountName);
    }
}
