package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps;

import org.assertj.core.api.Assertions;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.AuthorizationError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngDirectApiClient;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

@RunWith(MockitoJUnitRunner.class)
public class DeviceAgreementStepTest {

    private DeviceAgreementStep objectUnderTest;

    @Mock private IngConfiguration ingConfiguration;

    @Mock private IngDirectApiClient ingDirectApiClient;

    @Before
    public void init() {
        Mockito.when(ingConfiguration.getIngDirectApiClient()).thenReturn(ingDirectApiClient);
        objectUnderTest = new DeviceAgreementStep(ingConfiguration);
    }

    @Test
    public void profileBlockedTest() {
        // given
        final String mobileAppId = "testMobileAppId";
        final String profileBlockedResponseBody =
                "{\"code\" : \"PROFILE_BLOCKED\", \"details\" : { \"reason\" : \"ING_BLOCKED\" } }";
        HttpResponseException responseException = Mockito.mock(HttpResponseException.class);
        HttpResponse response = Mockito.mock(HttpResponse.class);
        Mockito.when(response.getStatus()).thenReturn(403);
        Mockito.when(response.getBody(String.class)).thenReturn(profileBlockedResponseBody);
        Mockito.when(responseException.getResponse()).thenReturn(response);
        Mockito.when(ingDirectApiClient.getDeviceProfileMeans(mobileAppId))
                .thenThrow(responseException);

        // when
        Throwable throwable =
                Assertions.catchThrowable(() -> objectUnderTest.getRemoteProfileMeans(mobileAppId));

        // then
        Assertions.assertThat(throwable).isInstanceOf(AuthorizationException.class);
        Assertions.assertThat(((AuthorizationException) throwable).getError())
                .isEqualTo(AuthorizationError.ACCOUNT_BLOCKED);
        Assertions.assertThat(throwable.getMessage())
                .isEqualTo("Profile blocked. Please contact with bank.");
    }
}
