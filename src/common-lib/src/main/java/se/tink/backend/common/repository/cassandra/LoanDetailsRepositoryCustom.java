package se.tink.backend.common.repository.cassandra;

import java.util.UUID;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.backend.core.LoanDetails;

public interface LoanDetailsRepositoryCustom extends Creatable {

    void deleteByAccountId(UUID accountId);

    LoanDetails findOneByAccountId(UUID accountId);

    boolean hasBeenUpdated(LoanDetails loanDetails);
}
