package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator;

import static org.assertj.core.api.Java6Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.HashMap;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbApiClient;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

public class DnbAuthenticatorTest {

    private SupplementalInformationHelper mockSupplemental;
    private StrongAuthenticationState mockState;
    private DnbApiClient mockApiClient;
    private Credentials mockCredentials;

    private DnbAuthenticator dnbAuthenticator;

    @Before
    public void setup() {
        mockSupplemental = mock(SupplementalInformationHelper.class);
        mockState = mock(StrongAuthenticationState.class);
        mockApiClient = mock(DnbApiClient.class);
        mockCredentials = mock(Credentials.class);

        dnbAuthenticator =
                new DnbAuthenticator(mockSupplemental, mockState, mockApiClient, mockCredentials);
    }

    @Test
    public void shouldReturnTimedOutStatusWhenNoSupplementalInfoGathered() {
        // given
        given(mockState.getSupplementalKey()).willReturn("");
        given(mockSupplemental.waitForSupplementalInformation(anyString(), anyLong(), any()))
                .willReturn(Optional.empty());

        // when
        ThirdPartyAppResponse<String> result = dnbAuthenticator.collect("");

        // then
        assertThat(result.getStatus()).isEqualTo(ThirdPartyAppStatus.TIMED_OUT);
    }

    @Test
    public void shouldReturnAuthErrorStatusWhenConsentInvalid() {
        // given
        given(mockState.getSupplementalKey()).willReturn("");
        given(mockSupplemental.waitForSupplementalInformation(anyString(), anyLong(), any()))
                .willReturn(Optional.of(new HashMap<>()));
        given(mockApiClient.isConsentValid()).willReturn(false);

        // when
        ThirdPartyAppResponse<String> result = dnbAuthenticator.collect("");

        // then
        assertThat(result.getStatus()).isEqualTo(ThirdPartyAppStatus.AUTHENTICATION_ERROR);
    }

    @Test
    public void shouldReturnDoneStatusWhenConsentValid() {
        // given
        given(mockState.getSupplementalKey()).willReturn("");
        given(mockSupplemental.waitForSupplementalInformation(anyString(), anyLong(), any()))
                .willReturn(Optional.of(new HashMap<>()));
        given(mockApiClient.isConsentValid()).willReturn(true);

        // when
        ThirdPartyAppResponse<String> result = dnbAuthenticator.collect("");

        // then
        assertThat(result.getStatus()).isEqualTo(ThirdPartyAppStatus.DONE);
    }

    @Test
    public void shouldReturnAuthErrorDuringInitIfNoPsuIdInCredentials() {
        // given
        given(mockCredentials.getField(DnbConstants.CredentialsKeys.PSU_ID)).willReturn("");

        // when
        ThirdPartyAppResponse<String> result = dnbAuthenticator.init();

        // then
        assertThat(result.getStatus()).isEqualTo(ThirdPartyAppStatus.AUTHENTICATION_ERROR);
    }
}
