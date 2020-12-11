package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.fetcher.identity;

import static se.tink.libraries.identitydata.NameElement.Type.FIRST_NAME;
import static se.tink.libraries.identitydata.NameElement.Type.SURNAME;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity.IdentityEntity;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.NameElement;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class BoursoramaIdentityFetcherTest {

    private BoursoramaApiClient apiClient;
    private BoursoramaIdentityFetcher identityFetcher;

    @Before
    public void init() {
        apiClient = Mockito.mock(BoursoramaApiClient.class);
        identityFetcher = new BoursoramaIdentityFetcher(apiClient);
    }

    @Test
    public void shouldParseNameAndSurname() {
        // give
        Mockito.when(apiClient.fetchIdentityData()).thenReturn(loadSimpleCorrectResponse());

        // when
        IdentityData identityData = identityFetcher.fetchIdentity().getIdentityData();

        // then
        Assert.assertEquals(2, identityData.getNameElements().size());
        NameElement firstNameElement = identityData.getNameElements().get(0);
        Assert.assertEquals(FIRST_NAME, firstNameElement.getType());
        Assert.assertEquals("Tyuiop", firstNameElement.getValue());
        NameElement secondNameElement = identityData.getNameElements().get(1);
        Assert.assertEquals(SURNAME, secondNameElement.getType());
        Assert.assertEquals("ABDCEF", secondNameElement.getValue());
    }

    private IdentityEntity loadSimpleCorrectResponse() {
        return SerializationUtils.deserializeFromString(
                "{\n"
                        + "  \"connectedPsu\": \"ABDCEF Tyuiop\",\n"
                        + "  \"_links\": {\n"
                        + "    \"self\": {\n"
                        + "      \"href\": \"\",\n"
                        + "      \"method\": \"GET\"\n"
                        + "    }\n"
                        + "  }\n"
                        + "}",
                IdentityEntity.class);
    }
}
