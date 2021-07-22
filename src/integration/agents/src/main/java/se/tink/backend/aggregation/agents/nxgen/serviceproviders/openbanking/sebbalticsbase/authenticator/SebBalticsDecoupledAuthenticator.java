package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SupplementalInfoException;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.steps.CheckIfAccessTokenIsValidStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.steps.CreateNewConsentStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.steps.ExchangeCodeForTokenStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.steps.InitStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.authenticator.steps.RefreshAccessTokenStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.configuration.SebBalticsConfiguration;
import se.tink.backend.aggregation.agents.utils.supplementalfields.BalticFields;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.AuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.i18n.Catalog;

public class SebBalticsDecoupledAuthenticator extends StatelessProgressiveAuthenticator {

    private final List<AuthenticationStep> authenticationSteps;
    SupplementalInformationController supplementalInformationController;
    Catalog catalog;

    public SebBalticsDecoupledAuthenticator(
            SebBalticsBaseApiClient apiClient,
            AgentConfiguration<SebBalticsConfiguration> agentConfiguration,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage,
            Credentials credentials,
            String bankBic,
            LocalDate localDate,
            SupplementalInformationController supplementalInformationController,
            Catalog catalog) {

        SebBalticsConfiguration configuration =
                agentConfiguration.getProviderSpecificConfiguration();
        this.supplementalInformationController = supplementalInformationController;
        this.catalog = catalog;

        this.authenticationSteps =
                Arrays.asList(
                        new CheckIfAccessTokenIsValidStep(apiClient, persistentStorage),
                        new RefreshAccessTokenStep(apiClient, persistentStorage),
                        new InitStep(this, apiClient, sessionStorage, configuration, bankBic),
                        new ExchangeCodeForTokenStep(
                                apiClient, sessionStorage, persistentStorage, credentials),
                        new CreateNewConsentStep(this, apiClient, persistentStorage, localDate));
    }

    @Override
    public List<AuthenticationStep> authenticationSteps() {
        return authenticationSteps;
    }

    public void displayChallengeCodeToUser(String challengeCode) {

        Field field = BalticFields.BankIdReferenceInfo.build(catalog, challengeCode);
        try {
            supplementalInformationController.askSupplementalInformationSync(field);
        } catch (SupplementalInfoException e) {
            // ignore empty response!
            // we're actually not interested in response at all, we just show a text!
        }
    }
}
