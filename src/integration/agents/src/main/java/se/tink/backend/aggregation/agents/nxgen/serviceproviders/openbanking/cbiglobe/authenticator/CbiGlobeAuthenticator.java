package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeFetcherApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

public class CbiGlobeAuthenticator implements TypedAuthenticator {

    private final CbiConsentCreationStep consentCreationStep;
    private final CbiConsentRediredtAuthorizationStep rediredtAuthorizationStep;
    private final CbiAccountFetchingStep accountFetchingStep;
    private final CbiFinishAuthenticationStep finishAuthenticationStep;

    public CbiGlobeAuthenticator(
            CbiGlobeAuthApiClient authApiClient,
            CbiGlobeFetcherApiClient fetcherApiClient,
            CbiStorage storage,
            LocalDateTimeSource localDateTimeSource,
            SupplementalInformationController supplementalInformationController,
            Credentials credentials) {
        consentCreationStep =
                new CbiConsentCreationStep(authApiClient, localDateTimeSource, storage);
        rediredtAuthorizationStep =
                new CbiConsentRediredtAuthorizationStep(
                        supplementalInformationController, authApiClient, storage);
        accountFetchingStep = new CbiAccountFetchingStep(fetcherApiClient, storage);
        finishAuthenticationStep =
                new CbiFinishAuthenticationStep(authApiClient, credentials, storage);
    }

    @Override
    public void authenticate(Credentials credentials) {
        CbiConsentResponse consentResponse = consentCreationStep.createConsentAndSaveId();
        rediredtAuthorizationStep.authorizeConsent(consentResponse);
        finishAuthenticationStep.storeConsentValidUntilDateInCredentials();
        accountFetchingStep.fetchAndSaveAccounts();
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }
}
