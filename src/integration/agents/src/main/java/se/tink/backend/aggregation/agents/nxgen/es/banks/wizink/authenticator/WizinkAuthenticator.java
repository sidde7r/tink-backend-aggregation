package se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator;

import com.google.common.base.Strings;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.WizinkStorage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.rpc.CustomerLoginRequest;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.authenticator.rpc.CustomerLoginResponse;
import se.tink.backend.aggregation.agents.nxgen.es.banks.wizink.utils.WizinkEncoder;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStepResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.UsernamePasswordAuthenticationStep;

public class WizinkAuthenticator extends StatelessProgressiveAuthenticator {
    private final WizinkApiClient apiClient;
    private final WizinkStorage storage;

    private final List<AuthenticationStep> authenticationSteps;

    public WizinkAuthenticator(WizinkApiClient apiClient, WizinkStorage wizinkStorage) {
        this.storage = initStorageData(wizinkStorage);
        this.apiClient = apiClient;

        this.authenticationSteps =
                Collections.singletonList(
                        new UsernamePasswordAuthenticationStep(this::processLogin));
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }

    private WizinkStorage initStorageData(WizinkStorage wizinkStorage) {
        // TODO handle cases when auth, cards or accounts fetching went wrong [IFD-1592]
        if (Strings.isNullOrEmpty(wizinkStorage.getDeviceId())) {
            wizinkStorage.markIsFirstFullRefresh();

            String deviceId = UUID.randomUUID().toString();
            wizinkStorage.storeDeviceId(deviceId);
        } else {
            wizinkStorage.markIsNotFirstFullRefresh();
        }

        String indigitallDevice = UUID.randomUUID().toString();
        wizinkStorage.storeIndigitallDevice(indigitallDevice);

        return wizinkStorage;
    }

    public AuthenticationStepResponse processLogin(String username, String password) {
        CustomerLoginResponse response =
                apiClient.login(
                        new CustomerLoginRequest(
                                username,
                                WizinkEncoder.hashPassword(storage.getDeviceId(), password)));

        storage.storeCreditCardData(response.getLoginResponse().getGlobalPosition().getCards());
        storage.storeAccounts(response.getLoginResponse().getGlobalPosition().getProducts());

        return AuthenticationStepResponse.authenticationSucceeded();
    }
}
