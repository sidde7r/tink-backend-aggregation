package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator;

import com.google.common.base.Strings;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.agents.rpc.Provider;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.SwedbankApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.CheckIfAccessTokenIsValidStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.CollectStatusStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.ExchangeCodeForTokenStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.InitStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.RefreshAccessTokenStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SwedbankBalticsAuthenticator extends StatelessProgressiveAuthenticator {

    private final List<AuthenticationStep> authenticationSteps;

    public SwedbankBalticsAuthenticator(
            SwedbankApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            CredentialsRequest credentialsRequest,
            Provider provider) {

        final StepDataStorage stepDataStorage = new StepDataStorage(sessionStorage);
        this.authenticationSteps =
                Arrays.asList(
                        new CheckIfAccessTokenIsValidStep(persistentStorage),
                        new RefreshAccessTokenStep(apiClient, persistentStorage),
                        new InitStep(
                                this, apiClient, stepDataStorage, credentialsRequest, provider),
                        new CollectStatusStep(this, apiClient, persistentStorage, stepDataStorage),
                        new ExchangeCodeForTokenStep(
                                apiClient, persistentStorage, stepDataStorage));
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }

    public String verifyCredentialsNotNullOrEmpty(String value) throws LoginException {
        if (Strings.isNullOrEmpty(value) || value.trim().isEmpty()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        return value;
    }
}
