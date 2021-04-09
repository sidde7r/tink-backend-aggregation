package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank.mapper;

import java.util.Collection;
import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.IdentifierMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class DanskeFiIdentifierMapper implements IdentifierMapper {

    private final DefaultIdentifierMapper defaultMapper;

    public DanskeFiIdentifierMapper(PrioritizedValueExtractor valueExtractor) {
        this.defaultMapper = new DefaultIdentifierMapper(valueExtractor);
    }

    @Override
    public AccountIdentifier mapIdentifier(AccountIdentifierEntity id) {
        return defaultMapper.mapIdentifier(id);
    }

    @Override
    public AccountIdentifierEntity getTransactionalAccountPrimaryIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {
        return defaultMapper.getTransactionalAccountPrimaryIdentifier(identifiers);
    }

    @Override
    public AccountIdentifierEntity getCreditCardIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {
        return identifiers.stream()
                .filter(
                        identifier ->
                                UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN
                                        .equals(identifier.getIdentifierType()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Missing IBAN card identifier"));
    }

    @Override
    public String getUniqueIdentifier(AccountIdentifierEntity accountIdentifier) {
        return accountIdentifier.getIdentification();
    }
}
