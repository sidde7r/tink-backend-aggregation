package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.steps.FinalizeAuthStep;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.steps.PasswordLoginStep;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.steps.SmsOtpStep;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.Storage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class CaisseEpargneAuthenticator extends StatelessProgressiveAuthenticator {

    private final CaisseEpargneApiClient apiClient;
    private final Storage instanceStorage;
    private final SupplementalInformationProvider supplementalInformationProvider;
    private final PersistentStorage persistentStorage;

    public CaisseEpargneAuthenticator(
            CaisseEpargneApiClient apiClient,
            Storage instanceStorage,
            SupplementalInformationProvider supplementalInformationProvider,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.instanceStorage = instanceStorage;
        this.supplementalInformationProvider = supplementalInformationProvider;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return Arrays.asList(
                new PasswordLoginStep(apiClient, instanceStorage, persistentStorage),
                new SmsOtpStep(apiClient, instanceStorage, supplementalInformationProvider),
                new FinalizeAuthStep(apiClient, instanceStorage));
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return !persistentStorage
                .get(StorageKeys.COULD_AUTO_AUTHENTICATE, Boolean.class)
                .orElse(false);
    }
}
