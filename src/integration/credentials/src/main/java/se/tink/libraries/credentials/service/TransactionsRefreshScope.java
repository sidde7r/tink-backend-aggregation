package se.tink.libraries.credentials.service;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import se.tink.libraries.account.AccountIdentifier;

@Getter
@Setter
@EqualsAndHashCode
public class TransactionsRefreshScope {
    /**
     * It is a transaction booked date limit applied for all accounts not specified in
     * accountsTransactionDate.
     */
    private LocalDate transactionBookedDateGte;

    /** It is a set of account transaction refresh scope objects. */
    private Set<AccountTransactionsRefreshScope> accounts;

    @JsonIgnore
    public Optional<LocalDate> getTransactionBookedDateGteForAccountIdentifiers(
            Collection<AccountIdentifier> identifiers) {

        Optional<LocalDate> defaultLimit = Optional.ofNullable(transactionBookedDateGte);
        if (accounts == null || accounts.isEmpty()) {
            return defaultLimit;
        }

        Optional<LocalDate> accountLimit =
                accounts.stream()
                        .filter(
                                it -> {
                                    Set<AccountIdentifier> accountScopeIdentifiers =
                                            it.getAccountIdentifiers().stream()
                                                    .map(AccountIdentifier::createOrThrow)
                                                    .collect(Collectors.toSet());
                                    return accountScopeIdentifiers.removeAll(identifiers);
                                })
                        .findAny()
                        .map(AccountTransactionsRefreshScope::getTransactionBookedDateGte);

        if (accountLimit.isPresent()) {
            return accountLimit;
        }

        return defaultLimit;
    }
}
