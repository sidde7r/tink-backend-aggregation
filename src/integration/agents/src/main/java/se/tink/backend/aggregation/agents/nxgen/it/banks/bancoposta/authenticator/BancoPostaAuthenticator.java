package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.FinalizeAuthStep;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.OnboardingStep;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.RegisterAppStep;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.RegisterInitStep;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.RegisterVerificationStep;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.SyncWalletStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.libraries.i18n.Catalog;

@RequiredArgsConstructor
public class BancoPostaAuthenticator extends StatelessProgressiveAuthenticator {

    public static final String SYNC_WALLET_STEP_ID = "syncWalletStep";
    public static final String REGSITER_VERIFICATION_STEP_ID = "registerVerificationStep";
    public static final String FINALIZE_AUTH_STEP_ID = "finalizeAuthStep";
    public static final String ONBOARDING_STEP_ID = "onboardingStep";
    public static final String REGISTER_APP_STEP_ID = "registerAppStep";

    private final BancoPostaApiClient apiClient;
    private final BancoPostaStorage storage;
    private final Catalog catalog;

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        if (storage.isManualAuthFinished()) {
            return autoAuthSteps();
        } else {
            return manualAuthSteps();
        }
    }

    private List<AuthenticationStep> manualAuthSteps() {
        return ImmutableList.of(
                new RegisterInitStep(apiClient, storage),
                new RegisterVerificationStep(apiClient, storage),
                new SyncWalletStep(apiClient, catalog),
                new OnboardingStep(apiClient, storage, catalog),
                new RegisterAppStep(apiClient, storage),
                new FinalizeAuthStep(apiClient, storage));
    }

    private List<AuthenticationStep> autoAuthSteps() {
        return Collections.singletonList(new FinalizeAuthStep(apiClient, storage));
    }
}
