package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.identifier;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.BBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.SORT_CODE_ACCOUNT_NUMBER;

import com.google.common.collect.ImmutableList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.account.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.PrioritizedValueExtractor;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.AccountIdentifier.Type;

@RequiredArgsConstructor
public class IdentifierMapper {

    private final PrioritizedValueExtractor valueExtractor;

    private static final GenericTypeMapper<Type, ExternalAccountIdentification4Code> typeMapper =
            GenericTypeMapper.<Type, ExternalAccountIdentification4Code>genericBuilder()
                    .put(
                            Type.SORT_CODE,
                            ExternalAccountIdentification4Code.SORT_CODE_ACCOUNT_NUMBER)
                    .put(Type.IBAN, ExternalAccountIdentification4Code.IBAN)
                    .put(Type.BBAN, ExternalAccountIdentification4Code.BBAN)
                    .put(Type.PAYMENT_CARD_NUMBER, ExternalAccountIdentification4Code.PAN)
                    .build();

    public AccountIdentifier mapIdentifier(AccountIdentifierEntity id) {
        Type type =
                typeMapper
                        .translate(id.getIdentifierType())
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Unable to map identifier type: " + id));

        return AccountIdentifier.create(type, id.getIdentification());
    }

    public AccountIdentifierEntity getTransactionalAccountPrimaryIdentifier(
            List<AccountIdentifierEntity> identifiers) {
        return valueExtractor.pickByValuePriority(
                identifiers,
                AccountIdentifierEntity::getIdentifierType,
                ImmutableList.of(SORT_CODE_ACCOUNT_NUMBER, IBAN, BBAN));
    }
}
