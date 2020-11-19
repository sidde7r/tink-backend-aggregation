package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.danskebank.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.api.UkOpenBankingApiDefinitions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountIdentifierEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.DefaultIdentifierMapper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.identifier.IdentifierMapper;
import se.tink.libraries.mapper.PrioritizedValueExtractor;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class DanskeFiIdentifierMapperTest {

    private IdentifierMapper objectUnderTest;
    private IdentifierMapper defaultIdentifierMapper;

    @Before
    public void setup() {
        PrioritizedValueExtractor prioritizedValueExtractor = new PrioritizedValueExtractor();
        objectUnderTest = new DanskeFiIdentifierMapper(prioritizedValueExtractor);
        defaultIdentifierMapper = new DefaultIdentifierMapper(prioritizedValueExtractor);
    }

    @Test
    public void shouldMapIbanForCreditCard() {
        // given
        List<AccountIdentifierEntity> identifierEntities =
                Collections.singletonList(getIbanIdentifierEntity());
        // when
        AccountIdentifierEntity creditCardIdentifier =
                objectUnderTest.getCreditCardIdentifier(identifierEntities);
        // then
        assertThat(creditCardIdentifier).isNotNull();
        assertThat(creditCardIdentifier.getIdentification()).isEqualTo("FI1234567887654321");
    }

    @Test
    public void shouldChooseTheSameTransactionalAccountIdentifierAsDefaultMapper() {
        // given
        ImmutableList<AccountIdentifierEntity> identifierEntities =
                ImmutableList.of(getIbanIdentifierEntity(), getBbanIdentifierEntity());
        List<UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code> allowedCodes =
                Collections.singletonList(
                        UkOpenBankingApiDefinitions.ExternalAccountIdentification4Code.IBAN);
        // when
        AccountIdentifierEntity result =
                objectUnderTest.getTransactionalAccountPrimaryIdentifier(
                        identifierEntities, allowedCodes);
        AccountIdentifierEntity expectedResult =
                defaultIdentifierMapper.getTransactionalAccountPrimaryIdentifier(
                        identifierEntities, allowedCodes);
        // then
        assertThat(result).isEqualToComparingFieldByField(expectedResult);
    }

    private static AccountIdentifierEntity getIbanIdentifierEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"Identification\": \"FI1234567887654321\",\n"
                        + "    \"Name\": \"Grzegorz Brzeczyszczykiewicz\",\n"
                        + "    \"SchemeName\": \"UK.OBIE.IBAN\"\n"
                        + "}",
                AccountIdentifierEntity.class);
    }

    private static AccountIdentifierEntity getBbanIdentifierEntity() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "    \"Identification\": \"FI1234567887654321\",\n"
                        + "    \"Name\": \"Grzegorz Brzeczyszczykiewicz\",\n"
                        + "    \"SchemeName\": \"UK.OBIE.BBAN\"\n"
                        + "}",
                AccountIdentifierEntity.class);
    }
}
