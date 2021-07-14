package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.libraries.account.AccountIdentifier;

/**
 * * Be aware, that changing implementation of this interface methods, can create duplicates in
 * database.
 */
public interface IdentifierMapper {

    AccountIdentifier mapIdentifier(AccountIdentifierEntity id);

    AccountIdentifierEntity getTransactionalAccountPrimaryIdentifier(
            Collection<AccountIdentifierEntity> identifiers);

    AccountIdentifierEntity getCreditCardIdentifier(
            Collection<AccountIdentifierEntity> identifiers);

    default Optional<AccountIdentifier> getMarketSpecificIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {
        return Optional.empty();
    }

    String getUniqueIdentifier(AccountIdentifierEntity accountIdentifier);
}
