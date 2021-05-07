package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.List;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.rpc.CustomerLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.rpc.CustomerLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.steps.GetUnmaskDataStep;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.steps.SessionIdStep;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.utils.WizinkEncoder;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.OtpStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class WizinkAuthenticator extends StatelessProgressiveAuthenticator {
    private final WizinkApiClient apiClient;
    private final WizinkStorage storage;

    private final List<AuthenticationStep> authenticationSteps;

    public WizinkAuthenticator(
            WizinkApiClient apiClient,
            WizinkStorage wizinkStorage,
            SupplementalInformationFormer supplementalInformationFormer) {
        this.storage = initStorageData(wizinkStorage);
        this.apiClient = apiClient;

        this.authenticationSteps =
                ImmutableList.of(
                        new UsernamePasswordAuthenticationStep(this::processLogin),
                        new SessionIdStep(apiClient, wizinkStorage),
                        new OtpStep(this::processOtp, supplementalInformationFormer),
                        new GetUnmaskDataStep(apiClient, wizinkStorage));
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }

    private WizinkStorage initStorageData(WizinkStorage wizinkStorage) {
        String deviceId = UUID.randomUUID().toString();
        wizinkStorage.storeDeviceId(deviceId);
        String indigitallDevice = UUID.randomUUID().toString();
        wizinkStorage.storeIndigitallDevice(indigitallDevice);

        return wizinkStorage;
    }

    AuthenticationStepResponse processLogin(String username, String password) {
        CustomerLoginResponse response =
                apiClient.login(
                        new CustomerLoginRequest(
                                username,
                                WizinkEncoder.hashPassword(storage.getDeviceId(), password)));

        storage.storeCreditCardData(response.getLoginResponse().getGlobalPosition().getCards());
        storage.storeLoginResponse(response.getLoginResponse());

        return AuthenticationStepResponse.executeNextStep();
    }

    private AuthenticationStepResponse processOtp(String otpCode) {
        apiClient.fetchCookieForUnmaskIban(otpCode);
        return AuthenticationStepResponse.executeNextStep();
    }
}
