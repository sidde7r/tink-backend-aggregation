package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.util;

import static org.mockito.Mockito.mock;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.configuration.LuminorConfiguration;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class LanguageMethodTest {

    private LuminorApiClient mockClient;

    @Before
    public void setup() {
        TinkHttpClient httpMock = mock(TinkHttpClient.class);
        PersistentStorage persistantStorageMock = mock(PersistentStorage.class);
        String localeMock = "locale";
        String providerMarket = "market";
        User userMock = mock(User.class);
        AgentConfiguration<LuminorConfiguration> configurationMock = mock(AgentConfiguration.class);

        mockClient =
                new LuminorApiClient(
                        httpMock,
                        persistantStorageMock,
                        localeMock,
                        providerMarket,
                        userMock,
                        configurationMock);
    }

    @Test
    public void shouldReturnLanguageSEWhenLocaleSweden() {
        String result = mockClient.getLanguage("sv_SE");
        Assert.assertEquals("sv", result);
    }

    @Test
    public void shouldReturnEnglishWhenWeirdSymbols() {
        String result = mockClient.getLanguage("hejhejvaderdettaforstring");
        Assert.assertEquals("en", result);
    }

    @Test
    public void shouldReturnLanguageRUWhenLocaleRussia() {
        String result = mockClient.getLanguage("ru_RU");
        Assert.assertEquals("ru", result);
    }

    @Test
    public void shouldReturnLanguageENWhenLocaleNull() {
        String result = mockClient.getLanguage(null);
        Assert.assertEquals("en", result);
    }
}
