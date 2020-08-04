package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.identitydata;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.identitydata.rpc.IdentityDataResponse;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class IdentityDataFetcherTest {

    private static final String IDENITY_DATA_JSON =
            "{\"address\": {\"address_line1\": \"Somestreet 22\", \"city\": \"AwesomeCity\", \"country_code\": \"NO\", \"postal_code\": \"1234\"}, \"birth_date\": \"1922-01-29\", \"customer_id\": \"12345678901\", \"employee\": false, \"enrollment_date\": \"2009-05-16\", \"first_name\": \"First Second\", \"last_name\": \"Surname\", \"loyalty_group\": \"F-PLUSS\", \"person_id\": \"12345678901\", \"phone_number\": \"1234564785\", \"segment\": \"household\", \"us_resident\": false }";

    @Test
    public void shouldReturnProperlyMappedIdentityData() {
        // given
        FetcherClient fetcherClient = mock(FetcherClient.class);
        given(fetcherClient.fetchIdentityData())
                .willReturn(
                        SerializationUtils.deserializeFromString(
                                IDENITY_DATA_JSON, IdentityDataResponse.class));
        IdentityDataFetcher identityDataFetcher = new IdentityDataFetcher(fetcherClient);

        // when
        IdentityData identityData = identityDataFetcher.fetchIdentityData();

        // then
        assertThat(identityData.getDateOfBirth()).isEqualTo(LocalDate.of(1922, 1, 29));
        assertThat(identityData.getFullName()).isEqualTo("First Second Surname");
        assertThat(identityData.getNameElements()).hasSize(2);
    }
}
