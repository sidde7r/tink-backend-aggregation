package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngApiClient;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.IngConstants.ScaMethod;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps.LoginStep;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps.OtpStep;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.steps.PushStep;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IngMultifactorAuthenticator extends StatelessProgressiveAuthenticator {
    private final IngApiClient apiClient;
    private final RandomValueGenerator randomValueGenerator;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final CredentialsRequest credentialsRequest;
    private final SupplementalInformationHelper supplementalInformationHelper;

    public IngMultifactorAuthenticator(
            IngApiClient apiClient,
            RandomValueGenerator randomValueGenerator,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            CredentialsRequest credentialsRequest,
            SupplementalInformationHelper supplementalInformationHelper) {
        this.apiClient = apiClient;
        this.randomValueGenerator = randomValueGenerator;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.credentialsRequest = credentialsRequest;
        this.supplementalInformationHelper = supplementalInformationHelper;
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        final Map<Integer, String> scaStepMapper =
                ImmutableMap.of(ScaMethod.SMS, OtpStep.STEP_ID, ScaMethod.PUSH, PushStep.STEP_ID);
        return ImmutableList.of(
                new LoginStep(
                        apiClient,
                        sessionStorage,
                        persistentStorage,
                        randomValueGenerator,
                        scaStepMapper,
                        isManualAuthentication(credentialsRequest)),
                new OtpStep(
                        apiClient,
                        sessionStorage,
                        persistentStorage,
                        supplementalInformationHelper),
                new PushStep(
                        apiClient,
                        sessionStorage,
                        persistentStorage,
                        supplementalInformationHelper));
    }

    private boolean isManualAuthentication(CredentialsRequest request) {
        return request.isCreate() || request.isUpdate();
    }
}
