package se.tink.backend.aggregation.agents.nxgen.se.openbanking.danskebank.mapper;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.BBAN;

import java.util.Collection;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.IdentifierMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.account.identifiers.SwedishIdentifier;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class DanskeSeIdentifierMapper implements IdentifierMapper {
    private final DefaultIdentifierMapper defaultMapper;

    public DanskeSeIdentifierMapper(PrioritizedValueExtractor valueExtractor) {
        this.defaultMapper = new DefaultIdentifierMapper(valueExtractor);
    }

    @Override
    public AccountIdentifier mapIdentifier(AccountIdentifierEntity id) {
        return AccountIdentifier.create(AccountIdentifierType.IBAN, id.getIdentification());
    }

    @Override
    public AccountIdentifierEntity getTransactionalAccountPrimaryIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {
        return defaultMapper.getTransactionalAccountPrimaryIdentifier(identifiers);
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

    @Override
    public Optional<AccountIdentifier> getMarketSpecificIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {
        Optional<String> bban =
                identifiers.stream()
                        .filter(i -> isBBAN(i.getIdentifierType().toValue()))
                        .map(AccountIdentifierEntity::getIdentification)
                        .findFirst();

        return bban.map(SwedishIdentifier::new);
    }

    private boolean isBBAN(String key) {
        return BBAN.toValue().equalsIgnoreCase(key);
    }
}
