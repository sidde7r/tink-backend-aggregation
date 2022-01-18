package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancoposta.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiAccountFetchingStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiConsentCreationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiConsentRediredtAuthorizationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiFinishAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeFetcherApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;

public class BancoPostaAuthenticator implements TypedAuthenticator {

    private final CbiConsentCreationStep consentCreationStep;
    private final BancoPostaScaMethodSelectionStep scaMethodSelectionStep;
    private final CbiConsentRediredtAuthorizationStep redirectAuthorizationStep;
    private final CbiFinishAuthenticationStep finishAuthenticationStep;
    private final CbiAccountFetchingStep accountFetchingStep;

    public BancoPostaAuthenticator(
            CbiGlobeAuthApiClient authApiClient,
            CbiGlobeFetcherApiClient fetcherApiClient,
            CbiStorage storage,
            LocalDateTimeSource localDateTimeSource,
            SupplementalInformationController supplementalInformationController,
            Credentials credentials,
            CbiUrlProvider urlProvider) {
        consentCreationStep =
                new CbiConsentCreationStep(authApiClient, localDateTimeSource, storage);
        scaMethodSelectionStep =
                new BancoPostaScaMethodSelectionStep(
                        authApiClient, urlProvider.getUpdateConsentsRawUrl());
        redirectAuthorizationStep =
                new CbiConsentRediredtAuthorizationStep(
                        supplementalInformationController, authApiClient, storage);
        finishAuthenticationStep =
                new CbiFinishAuthenticationStep(authApiClient, credentials, storage);
        accountFetchingStep = new CbiAccountFetchingStep(fetcherApiClient, storage);
    }

    @Override
    public void authenticate(Credentials credentials) {
        CbiConsentResponse consentResponse = consentCreationStep.createConsentAndSaveId();
        consentResponse = scaMethodSelectionStep.pickScaMethod(consentResponse);
        redirectAuthorizationStep.authorizeConsent(consentResponse);
        finishAuthenticationStep.storeConsentValidUntilDateInCredentials();
        accountFetchingStep.fetchAndSaveAccounts();
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.THIRD_PARTY_APP;
    }
}
