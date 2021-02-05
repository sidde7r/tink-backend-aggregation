package se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.CaisseEpargneApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.steps.FinalizeAuthStep;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.authenticator.steps.RoutingIdentificationStep;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.caisseepargne.storage.CaisseEpargneStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.BpceAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.AuthConsumeStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.PasswordLoginStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.SmsOtpStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.BpceValidationHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.ImageRecognizeHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

@RequiredArgsConstructor
public class CaisseEpargneAuthenticator extends BpceAuthenticator {

    private final CaisseEpargneApiClient caisseEpargneApiClient;

    @Getter private final CaisseEpargneStorage bpceStorage;

    private final SupplementalInformationHelper supplementalInformationHelper;

    private final ImageRecognizeHelper imageRecognizeHelper;

    private final BpceValidationHelper validationHelper;

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return Arrays.asList(
                new RoutingIdentificationStep(caisseEpargneApiClient, bpceStorage),
                new PasswordLoginStep(
                        caisseEpargneApiClient,
                        bpceStorage,
                        imageRecognizeHelper,
                        validationHelper),
                new SmsOtpStep(
                        caisseEpargneApiClient,
                        bpceStorage,
                        supplementalInformationHelper,
                        validationHelper),
                new AuthConsumeStep(caisseEpargneApiClient, bpceStorage),
                new FinalizeAuthStep(caisseEpargneApiClient, bpceStorage));
    }
}
