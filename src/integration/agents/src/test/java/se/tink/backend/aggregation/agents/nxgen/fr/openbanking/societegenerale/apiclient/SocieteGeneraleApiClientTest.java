package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.matches;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.Urls.AIS_BASE_URL;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants.Urls.TRUSTED_BENEFICIARIES_PATH;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.ACCESS_TOKEN;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.BEARER_HEADER_VALUE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.CLIENT_ID;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.NEXT_PAGE_PATH;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.SIGNATURE;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.createTrustedBeneficiariesPage1Response;
import static se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleTestFixtures.createTrustedBeneficiariesPage2Response;

import javax.ws.rs.core.MediaType;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.SocieteGeneraleConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.configuration.SocieteGeneraleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transfer.rpc.TrustedBeneficiariesResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.utils.SignatureHeaderProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SocieteGeneraleApiClientTest {

    private static final String UUID_4_MATCHING_REGEX =
            "[0-9a-f]{8}\\-[0-9a-f]{4}\\-4[0-9a-f]{3}\\-[89ab][0-9a-f]{3}\\-[0-9a-f]{12}";

    private SocieteGeneraleApiClient societeGeneraleApiClient;

    private TinkHttpClient httpClientMock;

    @Before
    public void setUp() {
        final PersistentStorage persistentStorageMock = mock(PersistentStorage.class);
        when(persistentStorageMock.get(SocieteGeneraleConstants.StorageKeys.TOKEN))
                .thenReturn(ACCESS_TOKEN);

        final SocieteGeneraleConfiguration societeGeneraleConfigurationMock =
                mock(SocieteGeneraleConfiguration.class);
        when(societeGeneraleConfigurationMock.getClientId()).thenReturn(CLIENT_ID);

        final SignatureHeaderProvider signatureHeaderProviderMock =
                mock(SignatureHeaderProvider.class);
        when(signatureHeaderProviderMock.buildSignatureHeader(
                        eq(ACCESS_TOKEN), matches(UUID_4_MATCHING_REGEX)))
                .thenReturn(SIGNATURE);

        httpClientMock = mock(TinkHttpClient.class);

        societeGeneraleApiClient =
                new SocieteGeneraleApiClient(
                        httpClientMock,
                        persistentStorageMock,
                        societeGeneraleConfigurationMock,
                        signatureHeaderProviderMock);
    }

    @Test
    public void shouldGetTrustedBeneficiaries() {
        // given
        final TrustedBeneficiariesResponse expectedResponse =
                createTrustedBeneficiariesPage1Response();

        setUpHttpClientMockForApi(TRUSTED_BENEFICIARIES_PATH, expectedResponse);

        // when
        final TrustedBeneficiariesResponse actualResponse =
                societeGeneraleApiClient.getTrustedBeneficiaries();

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    @Test
    public void shouldGetTrustedBeneficiariesNextPage() {
        // given
        final TrustedBeneficiariesResponse expectedResponse =
                createTrustedBeneficiariesPage2Response();
        final URL nextPageUrl = new URL(AIS_BASE_URL + NEXT_PAGE_PATH);

        setUpHttpClientMockForApi(nextPageUrl, expectedResponse);

        // when
        final TrustedBeneficiariesResponse actualResponse =
                societeGeneraleApiClient.getTrustedBeneficiaries(NEXT_PAGE_PATH);

        // then
        assertThat(actualResponse).isEqualTo(expectedResponse);
    }

    private void setUpHttpClientMockForApi(URL url, Object response) {
        final RequestBuilder requestBuilderMock = mock(RequestBuilder.class);

        when(requestBuilderMock.header(
                        SocieteGeneraleConstants.HeaderKeys.AUTHORIZATION, BEARER_HEADER_VALUE))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.header(
                        eq(SocieteGeneraleConstants.HeaderKeys.X_REQUEST_ID),
                        matches(UUID_4_MATCHING_REGEX)))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.header(SocieteGeneraleConstants.HeaderKeys.SIGNATURE, SIGNATURE))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.header(SocieteGeneraleConstants.HeaderKeys.CLIENT_ID, CLIENT_ID))
                .thenReturn(requestBuilderMock);

        when(requestBuilderMock.accept(MediaType.APPLICATION_JSON)).thenReturn(requestBuilderMock);

        when(requestBuilderMock.get(any())).thenReturn(response);

        when(httpClientMock.request(url)).thenReturn(requestBuilderMock);
    }
}
