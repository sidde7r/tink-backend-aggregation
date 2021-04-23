package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.fetcher.identitydata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.nio.file.Paths;
import java.time.LocalDate;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.rpc.CustomerLoginResponse;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class WizinkIdentityDataFetcherTest {
    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/wizink/resources/authenticator";

    private WizinkIdentityDataFetcher identityDataFetcher;
    private WizinkStorage wizinkStorage;

    @Before
    public void setup() {
        wizinkStorage = mock(WizinkStorage.class);
        identityDataFetcher = new WizinkIdentityDataFetcher(wizinkStorage);
    }

    @Test
    public void shouldFetchAndMapIdentityData() {
        // given
        prepareStorageData("customer_login_response.json");

        // when
        IdentityData identityData = identityDataFetcher.fetchIdentityData();

        // then
        assertThat(identityData.getFullName()).isEqualTo("John Doe Loe");
        assertThat(identityData.getDateOfBirth()).isEqualTo(LocalDate.of(1964, 2, 14));
        assertThat(identityData.getSsn()).isEqualTo("******88j");
    }

    @Test
    public void shouldFetchAndMapIdentityDataWithoutUnknownDocumentType() {
        // given
        prepareStorageData("customer_login_response_without_nif.json");

        // when
        IdentityData identityData = identityDataFetcher.fetchIdentityData();

        // then
        assertThat(identityData.getFullName()).isEqualTo("John Doe Loe");
        assertThat(identityData.getDateOfBirth()).isEqualTo(LocalDate.of(1964, 2, 14));
        assertThat(identityData.getSsn()).isNull();
    }

    private void prepareStorageData(String fileName) {
        when(wizinkStorage.getXTokenUser())
                .thenReturn("20F59394856FDF03ADFCF8D053EF49AE460D41961241889BB8A107FDE036E820");
        when(wizinkStorage.getLoginResponse())
                .thenReturn(
                        SerializationUtils.deserializeFromString(
                                        Paths.get(TEST_DATA_PATH, fileName).toFile(),
                                        CustomerLoginResponse.class)
                                .getLoginResponse());
    }
}
