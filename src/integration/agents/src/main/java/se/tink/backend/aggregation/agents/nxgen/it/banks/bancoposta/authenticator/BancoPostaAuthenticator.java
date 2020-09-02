package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.BancoPostaApiClient;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.FinalizeAuthStep;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.OnboardingStep;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.RegisterAppStep;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.RegisterInitStep;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.RegisterVerificationStep;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.step.SyncWalletStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.i18n.Catalog;

public class BancoPostaAuthenticator extends StatelessProgressiveAuthenticator {

    public static final String SYNC_WALLET_STEP_ID = "syncWalletStep";
    public static final String REGSITER_VERIFICATION_STEP_ID = "registerVerificationStep";
    public static final String FINALIZE_AUTH_STEP_ID = "finalizeAuthStep";
    public static final String ONBOARDING_STEP_ID = "onboardingStep";
    public static final String REGISTER_APP_STEP_ID = "registerAppStep";

    private final BancoPostaApiClient apiClient;
    private final UserContext userContext;
    private final Catalog catalog;

    public BancoPostaAuthenticator(
            BancoPostaApiClient apiClient, UserContext userContext, Catalog catalog) {
        this.apiClient = apiClient;
        this.userContext = userContext;
        this.catalog = catalog;
    }

    @Override
    public boolean isManualAuthentication(CredentialsRequest request) {
        return userContext.isManualAuthFinished();
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        if (userContext.isManualAuthFinished()) {
            return Collections.singletonList(new FinalizeAuthStep(apiClient, userContext));
        }
        return ImmutableList.of(
                new RegisterInitStep(apiClient, userContext),
                new RegisterVerificationStep(apiClient, userContext),
                new SyncWalletStep(apiClient, catalog),
                new OnboardingStep(apiClient, userContext, catalog),
                new RegisterAppStep(apiClient, userContext),
                new FinalizeAuthStep(apiClient, userContext));
    }
}
