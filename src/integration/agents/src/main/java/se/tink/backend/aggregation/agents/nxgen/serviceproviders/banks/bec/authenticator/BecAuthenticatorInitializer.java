package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator;

import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps.BecAuthWithCodeAppStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps.BecAuthWithKeyCardStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps.BecAutoAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps.BecChoose2FAMethodStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.steps.BecInitializeAuthenticationStep;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.libraries.credentials.service.UserAvailability;
import se.tink.libraries.i18n.Catalog;

@RequiredArgsConstructor
public class BecAuthenticatorInitializer {

    private final BecApiClient apiClient;
    private final Credentials credentials;
    private final UserAvailability userAvailability;
    private final BecStorage storage;
    private final Catalog catalog;
    private final SupplementalInformationController supplementalInformationController;

    public BecAuthenticator initializeAuthenticator() {
        BecAutoAuthenticationStep autoAuthenticationStep = autoAuthenticationStep();
        BecInitializeAuthenticationStep initializeAuthenticationStep =
                initializeAuthenticationStep();
        BecChoose2FAMethodStep choose2FAMethodStep = choose2FAMethodStep();

        BecAuthWithCodeAppStep authWithCodeAppStep = authWithCodeAppStep();
        BecAuthWithKeyCardStep authWithKeyCardStep = authWithKeyCardStep();

        return new BecAuthenticator(
                autoAuthenticationStep,
                initializeAuthenticationStep,
                choose2FAMethodStep,
                authWithCodeAppStep,
                authWithKeyCardStep);
    }

    private BecAutoAuthenticationStep autoAuthenticationStep() {
        return new BecAutoAuthenticationStep(apiClient, credentials, storage, userAvailability);
    }

    private BecInitializeAuthenticationStep initializeAuthenticationStep() {
        return new BecInitializeAuthenticationStep(apiClient, credentials, storage);
    }

    private BecChoose2FAMethodStep choose2FAMethodStep() {
        return new BecChoose2FAMethodStep(catalog, supplementalInformationController);
    }

    private BecAuthWithCodeAppStep authWithCodeAppStep() {
        return new BecAuthWithCodeAppStep(
                apiClient, credentials, storage, catalog, supplementalInformationController);
    }

    private BecAuthWithKeyCardStep authWithKeyCardStep() {
        return new BecAuthWithKeyCardStep(
                apiClient, credentials, storage, catalog, supplementalInformationController);
    }
}
