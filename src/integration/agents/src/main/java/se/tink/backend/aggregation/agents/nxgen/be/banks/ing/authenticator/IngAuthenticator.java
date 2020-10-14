package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngComponents;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps.AuthenticateStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps.BridgeStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps.DeviceAgreementStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps.IdentifyStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps.KeyAgreementStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps.MpinAgreementStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps.PreSignStep;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.steps.SignStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class IngAuthenticator extends StatelessProgressiveAuthenticator {

    private final IngComponents ingComponents;
    private final SupplementalInformationFormer supplementalInformationFormer;

    public IngAuthenticator(
            IngComponents ingComponents,
            SupplementalInformationFormer supplementalInformationFormer) {
        this.ingComponents = ingComponents;
        this.supplementalInformationFormer = supplementalInformationFormer;
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return Arrays.asList(
                new KeyAgreementStep(ingComponents),
                new IdentifyStep(ingComponents, supplementalInformationFormer),
                new AuthenticateStep(ingComponents),
                new PreSignStep(ingComponents, supplementalInformationFormer),
                new SignStep(ingComponents),
                // auto starts here
                new DeviceAgreementStep(ingComponents),
                new BridgeStep(ingComponents),
                new MpinAgreementStep(ingComponents));
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return true;
    }
}
