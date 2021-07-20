package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.SpardaStorage;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.TestDataReader;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparda.client.SpardaAuthApiClient;
import se.tink.backend.aggregation.agents.utils.berlingroup.consent.ConsentResponse;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.MockRandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class SpardaRedirectHelperTest {

    private static final String TEST_STATE = "test_state";
    private static final String TEST_CLIENT_ID = "test_client_id";
    private static final String TEST_CONSENT_ID = "test_consent_id";
    private static final String TEST_REDIRECT_URL = "http://example.com/test/callback";

    private SpardaAuthApiClient mockAuthApiClient;
    private SpardaStorage mockStorage;

    private SpardaRedirectHelper redirectHelper;

    @Before
    public void setup() {
        mockAuthApiClient = mock(SpardaAuthApiClient.class);
        mockStorage = mock(SpardaStorage.class);

        redirectHelper =
                new SpardaRedirectHelper(
                        mockStorage,
                        mockAuthApiClient,
                        null,
                        TEST_CLIENT_ID,
                        TEST_REDIRECT_URL,
                        new StrongAuthenticationState(TEST_STATE),
                        new ConstantLocalDateTimeSource(),
                        new MockRandomValueGenerator());
    }

    @Test
    public void shouldBuildAuthorizeUrlProperlyAndSaveCodeVerifier() {
        // given
        when(mockAuthApiClient.createConsent(
                        any(),
                        eq(new URL(TEST_REDIRECT_URL).queryParam("state", TEST_STATE)),
                        any()))
                .thenReturn(
                        TestDataReader.readFromFile(
                                TestDataReader.CONSENT_CREATED, ConsentResponse.class));
        // when
        URL url = redirectHelper.buildAuthorizeUrl(TEST_STATE);
        // then
        verify(mockStorage).saveConsentId(TEST_CONSENT_ID);
        assertThat(url.toString())
                .isEqualTo(
                        "https://idp.sparda-n.de/oauth2/authorize?bic=GENODEF1S06&client_id=test_client_id&redirect_uri=http://example.com/test/callback?state=test_state&response_type=code&scope=AIS:tx-10df0600ce00c29872e18be81705a0c407c0eca352f9673ba181fc771f5bfca0&code_challenge_method=S256&code_challenge=xfsjW-_YdbkV-mxHAqeruTys89fEFLccvv-eGwqfvUE");
    }

    @Test
    public void shouldEndWithCancelledWhenCallbackParamHasNotOkParam() {
        // given
        Map<String, String> callbackData = new HashMap<>();
        callbackData.put("nok", "true");
        // when
        Throwable throwable =
                catchThrowable(() -> redirectHelper.handleSpecificCallbackDataError(callbackData));
        // then
        assertThat(throwable)
                .isInstanceOf(ThirdPartyAppException.class)
                .hasMessage("Not-ok callback received!");
    }
}
