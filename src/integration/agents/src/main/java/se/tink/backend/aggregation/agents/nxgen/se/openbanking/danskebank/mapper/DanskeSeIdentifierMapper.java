package se.tink.backend.aggregation.agents.nxgen.se.openbanking.danskebank.mapper;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.BBAN;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.IdentifierMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class DanskeSeIdentifierMapper implements IdentifierMapper {
    private final PrioritizedValueExtractor valueExtractor;
    private final DefaultIdentifierMapper defaultMapper;

    public DanskeSeIdentifierMapper(PrioritizedValueExtractor valueExtractor) {
        this.valueExtractor = valueExtractor;
        this.defaultMapper = new DefaultIdentifierMapper(valueExtractor);
    }

    @Override
    public AccountIdentifier mapIdentifier(AccountIdentifierEntity id) {
        return defaultMapper.mapIdentifier(id);
    }

    @Override
    public AccountIdentifierEntity getTransactionalAccountPrimaryIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {
        return valueExtractor
                .pickByValuePriority(
                        identifiers,
                        AccountIdentifierEntity::getIdentifierType,
                        ImmutableList.of(BBAN))
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Could not extract account identifier. No available identifier with BBAN type"));
    }

    @Override
    public AccountIdentifierEntity getCreditCardIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {
        return getTransactionalAccountPrimaryIdentifier(identifiers);
    }

    @Override
    public String getUniqueIdentifier(AccountIdentifierEntity accountIdentifier) {
        return defaultMapper.getUniqueIdentifier(accountIdentifier);
    }
}
