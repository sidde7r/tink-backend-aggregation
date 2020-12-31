package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.AuthorizeWithOtpStep;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.CheckIfAccessTokenIsValidStep;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.CheckOtpResponseStep;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.LoginStep;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers.AktiaAccessTokenRetriever;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers.AktiaLoginDetailsFetcher;
import se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.authenticator.steps.helpers.AktiaOtpCodeExchanger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationFormer;

public class AktiaAuthenticator extends StatelessProgressiveAuthenticator {

    private final List<AuthenticationStep> authenticationSteps;

    public AktiaAuthenticator(
            SupplementalInformationFormer supplementalInformationFormer,
            AktiaAccessTokenRetriever accessTokenRetriever,
            AktiaLoginDetailsFetcher loginDetailsFetcher,
            AktiaOtpCodeExchanger aktiaOtpCodeExchanger) {

        final AktiaOtpDataStorage otpDataStorage = loginDetailsFetcher.getOtpDataStorage();

        this.authenticationSteps =
                Arrays.asList(
                        new CheckIfAccessTokenIsValidStep(accessTokenRetriever),
                        new LoginStep(accessTokenRetriever, loginDetailsFetcher),
                        new AuthorizeWithOtpStep(
                                supplementalInformationFormer,
                                aktiaOtpCodeExchanger,
                                otpDataStorage),
                        new CheckOtpResponseStep(otpDataStorage));
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }
}
