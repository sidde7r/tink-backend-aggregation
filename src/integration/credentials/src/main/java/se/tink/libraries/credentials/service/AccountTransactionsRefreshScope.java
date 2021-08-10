package se.tink.libraries.credentials.service;

import java.time.LocalDate;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@EqualsAndHashCode
public class AccountTransactionsRefreshScope {
    /** A set of identifiers of an account. */
    private Set<String> accountIdentifiers;

    /**
     * Lower transaction booked date limit (transaction booked date expected to be greater or equal
     * to specified value).
     */
    private LocalDate transactionBookedDateGte;
}
