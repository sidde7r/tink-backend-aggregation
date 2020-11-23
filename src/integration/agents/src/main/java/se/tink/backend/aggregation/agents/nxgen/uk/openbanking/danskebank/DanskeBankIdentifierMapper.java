package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.danskebank;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.PAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.SORT_CODE_ACCOUNT_NUMBER;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.IdentifierMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

public class DanskeBankIdentifierMapper implements IdentifierMapper {

    protected PrioritizedValueExtractor valueExtractor;
    protected DefaultIdentifierMapper identifierMapper;

    private static final List<ExternalAccountIdentification4Code>
            ALLOWED_CREDIT_CARD_ACCOUNT_IDENTIFIERS =
                    ImmutableList.of(SORT_CODE_ACCOUNT_NUMBER, IBAN, PAN);

    public DanskeBankIdentifierMapper(PrioritizedValueExtractor valueExtractor) {
        this.valueExtractor = valueExtractor;
        this.identifierMapper = new DefaultIdentifierMapper(valueExtractor);
    }

    @Override
    public AccountIdentifier mapIdentifier(AccountIdentifierEntity id) {
        return this.identifierMapper.mapIdentifier(id);
    }

    @Override
    public AccountIdentifierEntity getTransactionalAccountPrimaryIdentifier(
            Collection<AccountIdentifierEntity> identifiers,
            List<ExternalAccountIdentification4Code> allowedAccountIdentifiers) {
        return this.identifierMapper.getTransactionalAccountPrimaryIdentifier(
                identifiers, allowedAccountIdentifiers);
    }

    @Override
    public AccountIdentifierEntity getCreditCardIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {
        return valueExtractor
                .pickByValuePriority(
                        identifiers,
                        AccountIdentifierEntity::getIdentifierType,
                        ALLOWED_CREDIT_CARD_ACCOUNT_IDENTIFIERS)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Missing allowed credit card account identifiers like SortCode, IBAN, PAN"));
    }
}
