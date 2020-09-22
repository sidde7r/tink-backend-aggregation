package se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

import java.util.List;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.IspConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.apiclient.IspApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.entity.CheckRecordedDeviceResponsePayload;
import se.tink.backend.aggregation.agents.nxgen.it.banks.isp.authenticator.rpc.CheckRecordedDeviceResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class IspAuthenticatorTest {

    private IspAuthenticator objUnderTest;
    private IspApiClient apiClient;
    private PersistentStorage persistentStorage;
    private SessionStorage sessionStorage;

    @Before
    public void init() {
        this.apiClient = mock(IspApiClient.class);
        SupplementalInformationFormer supplementalInformationFormer =
                mock(SupplementalInformationFormer.class);
        this.persistentStorage = mock(PersistentStorage.class);
        this.sessionStorage = mock(SessionStorage.class);
        this.objUnderTest =
                new IspAuthenticator(
                        apiClient,
                        supplementalInformationFormer,
                        sessionStorage,
                        persistentStorage);
    }

    @Test
    public void
            authenticationStepsShouldReturnAutoAuthenticationStepsIfAutoAuthenticationConditionsAreFulfiled() {
        // given
        given(this.sessionStorage.get(StorageKeys.IS_AUTO_AUTH_POSSIBLE)).willReturn("true");
        given(this.persistentStorage.get(StorageKeys.REMEMBER_ME_TOKEN)).willReturn("fakeToken");

        CheckRecordedDeviceResponsePayload payload = mock(CheckRecordedDeviceResponsePayload.class);
        given(payload.getAccessToken()).willReturn("fakeToken");
        CheckRecordedDeviceResponse response = mock(CheckRecordedDeviceResponse.class);
        given(response.getPayload()).willReturn(payload);
        given(response.isOk()).willReturn(true);

        given(this.apiClient.checkRecordedDevice(any(), any())).willReturn(response);
        // when
        List<AuthenticationStep> listToCheck = objUnderTest.authenticationSteps();
        // then
        assertThat(listToCheck).hasSize(1);
    }

    @Test
    public void
            authenticationStepsShouldReturnManualAuthenticationStepsIfAutoAuthenticationConditionsAreNotFulfiled() {
        // given
        given(this.sessionStorage.get(StorageKeys.IS_AUTO_AUTH_POSSIBLE)).willReturn("false");
        given(this.persistentStorage.get(StorageKeys.REMEMBER_ME_TOKEN)).willReturn("fakeToken");

        CheckRecordedDeviceResponsePayload payload = mock(CheckRecordedDeviceResponsePayload.class);
        given(payload.getAccessToken()).willReturn(null);
        CheckRecordedDeviceResponse response = mock(CheckRecordedDeviceResponse.class);
        given(response.getPayload()).willReturn(payload);
        given(response.isOk()).willReturn(false);

        given(this.apiClient.checkRecordedDevice(any(), any())).willReturn(response);
        // when
        List<AuthenticationStep> listToCheck = objUnderTest.authenticationSteps();
        // then
        assertThat(listToCheck).hasSize(5);
    }
}
