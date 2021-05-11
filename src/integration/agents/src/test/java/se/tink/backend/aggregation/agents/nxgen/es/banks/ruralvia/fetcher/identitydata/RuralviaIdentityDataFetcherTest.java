package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.fetcher.identitydata;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.exceptions.refresh.IdentityRefreshException;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.libraries.identitydata.IdentityData;

public class RuralviaIdentityDataFetcherTest {

    private static final String TEST_DATA_PATH =
            "src/integration/agents/src/test/java/se/tink/backend/aggregation/agents/nxgen/es/banks/ruralvia/resources";

    private RuralviaApiClient apiClient;
    private RuralviaIdentityDataFetcher identityDataFetcher;
    private static Credentials credentials;
    private static String identityDataDetails;
    private static String globalPosition;

    @BeforeClass
    public static void setUpOnce() throws IOException {
        credentials = new Credentials();
        credentials.setField(Key.NATIONAL_ID_NUMBER, "76229620F");

        globalPosition =
                new String(Files.readAllBytes(Paths.get(TEST_DATA_PATH, "globalPosition.html")));

        identityDataDetails =
                new String(
                        Files.readAllBytes(
                                Paths.get(TEST_DATA_PATH, "identityDataDetailsPage.html")));
    }

    @Before
    public void setUp() throws Exception {
        apiClient = mock(RuralviaApiClient.class);
        identityDataFetcher = new RuralviaIdentityDataFetcher(apiClient, credentials);
    }

    @Test
    public void fetchIdentityDataShouldReturnData() {
        // given
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-uuuu");
        when(apiClient.getGlobalPositionHtml()).thenReturn(globalPosition);
        when(apiClient.navigateThroughIdentity(any())).thenReturn(identityDataDetails);

        // when
        IdentityData data = identityDataFetcher.fetchIdentityData();

        // then
        Assert.assertEquals(LocalDate.parse("29-09-1900", formatter), data.getDateOfBirth());
        Assert.assertEquals("76229620F", data.getSsn());
        Assert.assertEquals("JOHN TINKER DOE", data.getFullName());
    }

    @Test(expected = IdentityRefreshException.class)
    public void fetchIdentityDataShouldThrowExceptionWhenThereIsNoData() {
        // given
        when(apiClient.getGlobalPositionHtml()).thenReturn(globalPosition);
        when(apiClient.navigateThroughIdentity(any())).thenReturn("");

        // when
        IdentityData result = identityDataFetcher.fetchIdentityData();

        // then

    }
}
