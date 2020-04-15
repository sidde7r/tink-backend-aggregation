package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.mapper.accounttype.detail;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.Test;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.protocol.parts.response.HIUPD;

public class IngDibaAccountTypeMapperTest {

    @Test
    public void shouldMapSupportedProductNamesProperly() {
        AccountTypeMapper mapper = new IngDibaAccountTypeMapper();

        getExpectedMappings()
                .forEach(
                        (key, value) -> {
                            HIUPD hiupd = new HIUPD().setProductName(key);
                            Optional<AccountTypes> type = mapper.getAccountTypeFor(hiupd);
                            assertThat(type.isPresent()).isTrue();
                            assertThat(type.get()).isEqualTo(value);
                        });
    }

    @Test
    public void shouldMapSupportedProductNamesProperlyBeingCaseInsensitive() {
        AccountTypeMapper mapper = new IngDibaAccountTypeMapper();

        getExpectedMappings()
                .forEach(
                        (key, value) -> {
                            List<String> productNameVariation =
                                    Arrays.asList(key.toLowerCase(), key.toUpperCase());
                            for (String productName : productNameVariation) {
                                HIUPD hiupd = new HIUPD().setProductName(productName);
                                Optional<AccountTypes> type = mapper.getAccountTypeFor(hiupd);
                                assertThat(type.isPresent()).isTrue();
                                assertThat(type.get()).isEqualTo(value);
                            }
                        });
    }

    @Test
    public void shouldReturnEmptyOptionalIfNoMappingFound() {
        AccountTypeMapper mapper = new IngDibaAccountTypeMapper();
        HIUPD hiupd = new HIUPD().setProductName("Uknown product name");
        Optional<AccountTypes> type = mapper.getAccountTypeFor(hiupd);
        assertThat(type.isPresent()).isFalse();
    }

    private Map<String, AccountTypes> getExpectedMappings() {
        Map<String, AccountTypes> expectedMappings = new HashMap<>();
        expectedMappings.put("Extra-Konto".toUpperCase(), AccountTypes.SAVINGS);
        expectedMappings.put("Sparbrief".toUpperCase(), AccountTypes.SAVINGS);
        expectedMappings.put("Vl-Sparen".toUpperCase(), AccountTypes.SAVINGS);
        expectedMappings.put("Girokonto".toUpperCase(), AccountTypes.CHECKING);
        expectedMappings.put("Direkt-Depot".toUpperCase(), AccountTypes.INVESTMENT);
        return expectedMappings;
    }
}
