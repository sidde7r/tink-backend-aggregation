package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier;

import java.util.Collection;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountIdentifierEntity;
import se.tink.libraries.account.AccountIdentifier;

public interface IdentifierMapper {

    AccountIdentifier mapIdentifier(AccountIdentifierEntity id);

    AccountIdentifierEntity getTransactionalAccountPrimaryIdentifier(
            Collection<AccountIdentifierEntity> identifiers,
            List<ExternalAccountIdentification4Code> allowedAccountIdentifiers);

    AccountIdentifierEntity getCreditCardIdentifier(
            Collection<AccountIdentifierEntity> identifiers);
}
