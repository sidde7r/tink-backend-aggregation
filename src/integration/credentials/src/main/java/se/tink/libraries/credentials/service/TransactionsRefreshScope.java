package se.tink.libraries.credentials.service;

import java.time.LocalDate;
import java.util.Set;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

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
}
