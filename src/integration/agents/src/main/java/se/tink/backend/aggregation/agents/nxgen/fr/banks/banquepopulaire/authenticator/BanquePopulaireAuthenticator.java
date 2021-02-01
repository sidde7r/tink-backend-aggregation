package se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator;

import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.apiclient.BanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.steps.FinalizeAuthStep;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.authenticator.steps.InitiateSessionStep;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.banquepopulaire.storage.BanquePopulaireStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.BpceAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.AuthConsumeStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.PasswordLoginStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.SmsOtpStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.BpceValidationHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bpcegroup.authenticator.steps.helper.ImageRecognizeHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;

@RequiredArgsConstructor
public class BanquePopulaireAuthenticator extends BpceAuthenticator {

    private final BanquePopulaireApiClient banquePopulaireApiClient;

    @Getter private final BanquePopulaireStorage bpceStorage;

    private final SupplementalInformationHelper supplementalInformationHelper;

    private final ImageRecognizeHelper imageRecognizeHelper;

    private final BpceValidationHelper validationHelper;

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return Arrays.asList(
                new InitiateSessionStep(banquePopulaireApiClient, bpceStorage),
                new PasswordLoginStep(
                        banquePopulaireApiClient,
                        bpceStorage,
                        imageRecognizeHelper,
                        validationHelper),
                new SmsOtpStep(
                        banquePopulaireApiClient,
                        bpceStorage,
                        supplementalInformationHelper,
                        validationHelper),
                new AuthConsumeStep(banquePopulaireApiClient, bpceStorage),
                new FinalizeAuthStep(banquePopulaireApiClient, bpceStorage));
    }
}
