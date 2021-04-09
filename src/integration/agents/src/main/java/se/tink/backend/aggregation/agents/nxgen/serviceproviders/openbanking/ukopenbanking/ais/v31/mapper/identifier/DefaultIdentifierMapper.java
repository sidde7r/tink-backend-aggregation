package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.BBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.SAVINGS_ROLL_NUMBER;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.SORT_CODE_ACCOUNT_NUMBER;

import com.google.common.collect.ImmutableList;
import java.util.Collection;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.nxgen.core.account.GenericTypeMapper;
import se.tink.libraries.account.AccountIdentifier;
import se.tink.libraries.account.enums.AccountIdentifierType;
import se.tink.libraries.mapper.PrioritizedValueExtractor;

@RequiredArgsConstructor
public class DefaultIdentifierMapper implements IdentifierMapper {

    private static final List<ExternalAccountIdentification4Code>
            ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS =
                    ImmutableList.of(SORT_CODE_ACCOUNT_NUMBER, IBAN, BBAN, SAVINGS_ROLL_NUMBER);

    private static final GenericTypeMapper<
                    AccountIdentifierType, ExternalAccountIdentification4Code>
            typeMapper =
                    GenericTypeMapper
                            .<AccountIdentifierType, ExternalAccountIdentification4Code>
                                    genericBuilder()
                            .put(
                                    AccountIdentifierType.SORT_CODE,
                                    ExternalAccountIdentification4Code.SORT_CODE_ACCOUNT_NUMBER)
                            .put(
                                    AccountIdentifierType.IBAN,
                                    ExternalAccountIdentification4Code.IBAN)
                            .put(
                                    AccountIdentifierType.BBAN,
                                    ExternalAccountIdentification4Code.BBAN,
                                    ExternalAccountIdentification4Code.SAVINGS_ROLL_NUMBER)
                            .put(
                                    AccountIdentifierType.PAYMENT_CARD_NUMBER,
                                    ExternalAccountIdentification4Code.PAN)
                            .build();

    protected final PrioritizedValueExtractor valueExtractor;

    @Override
    public AccountIdentifier mapIdentifier(AccountIdentifierEntity id) {
        AccountIdentifierType type =
                typeMapper
                        .translate(id.getIdentifierType())
                        .orElseThrow(
                                () ->
                                        new IllegalArgumentException(
                                                "Unable to map identifier type: " + id));

        return AccountIdentifier.create(type, id.getIdentification());
    }

    @Override
    public AccountIdentifierEntity getTransactionalAccountPrimaryIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {

        return valueExtractor
                .pickByValuePriority(
                        identifiers,
                        AccountIdentifierEntity::getIdentifierType,
                        ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS)
                .orElseThrow(
                        () ->
                                new NoSuchElementException(
                                        "Could not extract account identifier. No available identifier with type of: "
                                                + StringUtils.join(
                                                        ALLOWED_TRANSACTIONAL_ACCOUNT_IDENTIFIERS,
                                                        ", ")));
    }

    @Override
    public AccountIdentifierEntity getCreditCardIdentifier(
            Collection<AccountIdentifierEntity> identifiers) {
        return identifiers.stream()
                .filter(i -> ExternalAccountIdentification4Code.PAN.equals(i.getIdentifierType()))
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Missing PAN card identifier"));
    }

    @Override
    public String getUniqueIdentifier(AccountIdentifierEntity accountIdentifier) {
        return accountIdentifier.getIdentification();
    }
}
