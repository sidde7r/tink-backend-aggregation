package se.tink.backend.common.repository.cassandra;

import com.google.common.collect.ImmutableListMultimap;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.Account;
import se.tink.backend.core.Loan;

import java.util.List;
import java.util.UUID;

public interface LoanDataRepositoryCustom extends Creatable {
    void deleteByAccountId(UUID accountId);

    Loan findLeastRecentOneByAccountId(String accountId);
    
    Loan findMostRecentOneByAccountId(String accountId);

    Loan findMostRecentOneByAccountId(UUID accountId);

    List<Loan> findMostRecentByAccountId(UUID accountId, int limit);

    List<Loan> findAllByAccountId(String accountId);

    ImmutableListMultimap<String, Loan> findAllByAccounts(List<Account> accounts);

    boolean hasBeenUpdated(Loan loan);
}
