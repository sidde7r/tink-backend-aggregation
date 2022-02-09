package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.util.List;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.SwedbankBalticsApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.AllAccountsConsentSCAAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.CheckIfAccessTokenIsValidStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.CollectStatusStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.DetailedConsentSCAAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.ExchangeCodeForTokenStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.GetAllAccountsStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.GetConsentForAllAccountsStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.GetDetailedConsentStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.InitSCAProcessStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.RefreshAccessTokenStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbankbaltics.authenticator.steps.helper.SCAAuthenticationHelper;
import se.tink.backend.aggregation.agents.utils.supplementalfields.BalticFields.SmartIdChallengeCode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n_aggregation.Catalog;

public class SwedbankBalticsAuthenticator extends StatelessProgressiveAuthenticator {

    private final List<AuthenticationStep> authenticationSteps;
    private final SupplementalInformationController supplementalInformationController;
    private final Catalog catalog;

    public SwedbankBalticsAuthenticator(
            SwedbankBalticsApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            SupplementalInformationController supplementalInformationController,
            Catalog catalog,
            User user) {

        final StepDataStorage stepDataStorage = new StepDataStorage(sessionStorage);
        final SCAAuthenticationHelper scaAuthenticationHelper =
                new SCAAuthenticationHelper(apiClient, stepDataStorage, persistentStorage, this);

        this.authenticationSteps =
                ImmutableList.of(
                        new CheckIfAccessTokenIsValidStep(persistentStorage),
                        new RefreshAccessTokenStep(apiClient, persistentStorage),
                        new InitSCAProcessStep(this, apiClient, stepDataStorage),
                        new CollectStatusStep(this, apiClient, stepDataStorage),
                        new ExchangeCodeForTokenStep(apiClient, persistentStorage, stepDataStorage),
                        new GetConsentForAllAccountsStep(
                                apiClient, persistentStorage, stepDataStorage, user),
                        // the step below is relevant for LT only, for other countries it will be
                        // skipped automatically during GetConsentForAllAccountsStep
                        new AllAccountsConsentSCAAuthenticationStep(
                                stepDataStorage, scaAuthenticationHelper),
                        new GetAllAccountsStep(apiClient, stepDataStorage, persistentStorage),
                        new GetDetailedConsentStep(apiClient, stepDataStorage, persistentStorage),
                        new DetailedConsentSCAAuthenticationStep(
                                stepDataStorage, scaAuthenticationHelper));
        this.supplementalInformationController = supplementalInformationController;
        this.catalog = catalog;
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }

    public String verifyCredentialsNotNullOrEmpty(String credential) throws LoginException {
        if (Strings.isNullOrEmpty(credential) || credential.trim().isEmpty()) {
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
        return credential;
    }

    public void displayChallengeCodeToUser(String challengeCode) {

        Field field = SmartIdChallengeCode.build(catalog, challengeCode);
        try {
            supplementalInformationController.askSupplementalInformationSync(field);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }
}
