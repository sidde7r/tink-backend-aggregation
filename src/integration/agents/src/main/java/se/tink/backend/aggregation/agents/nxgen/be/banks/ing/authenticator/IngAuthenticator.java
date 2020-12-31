package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.banks.ing.IngConfiguration;
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

public class IngAuthenticator extends StatelessProgressiveAuthenticator {

    private final IngConfiguration ingConfiguration;
    private final SupplementalInformationFormer supplementalInformationFormer;

    public IngAuthenticator(
            IngConfiguration ingConfiguration,
            SupplementalInformationFormer supplementalInformationFormer) {
        this.ingConfiguration = ingConfiguration;
        this.supplementalInformationFormer = supplementalInformationFormer;
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return Arrays.asList(
                new KeyAgreementStep(ingConfiguration),
                new IdentifyStep(ingConfiguration, supplementalInformationFormer),
                new AuthenticateStep(ingConfiguration),
                new PreSignStep(ingConfiguration, supplementalInformationFormer),
                new SignStep(ingConfiguration),
                // auto starts here
                new DeviceAgreementStep(ingConfiguration),
                new BridgeStep(ingConfiguration),
                new MpinAgreementStep(ingConfiguration));
    }
}
