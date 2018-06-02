package se.tink.backend.common.repository.cassandra;

import java.util.Optional;
import se.tink.libraries.cassandra.capabilities.Creatable;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.backend.core.giros.Giro;

import java.util.List;

public interface GiroRepositoryCustom extends Creatable {
    Giro findOneByAccountNumberAndGiroType(String accountNumber, AccountIdentifier.Type giroType);
    List<Giro> findByAccountNumber(String accountNumber);
    Optional<AccountIdentifier> getIdentifierFor(String accountNumber, AccountIdentifier.Type giroType);
    List<AccountIdentifier> getIdentifiersFor(String accountNumber);
}
