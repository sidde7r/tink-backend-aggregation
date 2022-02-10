package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator;

import java.util.Arrays;
import java.util.List;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvApiClient;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.LhvConstants.SignSteps;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.steps.CheckIfAccessTokenIsValidStep;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.steps.CreateNewConsentStep;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.steps.ExchangeCodeForTokenStep;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.steps.RefreshAccessTokenStep;
import se.tink.backend.aggregation.agents.nxgen.ee.openbanking.lhv.authenticator.steps.ScaStep;
import se.tink.backend.aggregation.agents.utils.supplementalfields.BalticFields;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.step.ThirdPartyAppAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n_aggregation.Catalog;

public class LhvAuthenticator extends StatelessProgressiveAuthenticator {

    SupplementalInformationController supplementalInformationController;
    SupplementalInformationHelper supplementalInformationHelper;

    private final List<AuthenticationStep> authenticationSteps;
    private final Catalog catalog;

    public LhvAuthenticator(
            LhvApiClient apiClient,
            Credentials credentials,
            PersistentStorage persistentStorage,
            SupplementalInformationController supplementalInformationController,
            SessionStorage sessionStorage,
            Catalog catalog,
            SupplementalInformationHelper supplementalInformationHelper,
            StrongAuthenticationState strongAuthenticationState) {

        this.supplementalInformationController = supplementalInformationController;
        this.supplementalInformationHelper = supplementalInformationHelper;

        LhvThirdPartyAppRequestParamsProvider lhvThirdPartyAppRequestParamsProvider =
                new LhvThirdPartyAppRequestParamsProvider(
                        strongAuthenticationState, credentials, persistentStorage);

        authenticationSteps =
                Arrays.asList(
                        new CheckIfAccessTokenIsValidStep(apiClient, persistentStorage),
                        new RefreshAccessTokenStep(apiClient, persistentStorage),
                        new ScaStep(apiClient, sessionStorage, this),
                        new ExchangeCodeForTokenStep(
                                apiClient, sessionStorage, persistentStorage, credentials),
                        new CreateNewConsentStep(
                                apiClient, persistentStorage, strongAuthenticationState),
                        new ThirdPartyAppAuthenticationStep(
                                SignSteps.STEP_ID,
                                lhvThirdPartyAppRequestParamsProvider,
                                lhvThirdPartyAppRequestParamsProvider::processThirdPartyCallback));

        this.catalog = catalog;
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }

    public void displayChallengeCodeToUser(String code) {
        final Field field = BalticFields.SmartIdChallengeCode.build(catalog, code);
        try {
            supplementalInformationController.askSupplementalInformationAsync(field);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }
}
