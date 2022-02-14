package se.tink.backend.aggregation.agents.nxgen.it.openbanking.iccrea.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.CredentialsTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiUrlProvider;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiAccountFetchingStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiConsentCreationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.CbiFinishAuthenticationStep;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.authenticator.rpc.CbiConsentResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeAuthApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeFetcherApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticator;

public class IccreaAuthenticator implements TypedAuthenticator {

    private final CbiConsentCreationStep consentCreationStep;
    private final IccreaScaMethodSelectionStep scaMethodSelectionStep;
    private final IccreaCredentialsAuthenticationStep credentialsAuthenticationStep;
    private final IccreaConsentAuthorizationStep consentAuthorizationStep;
    private final CbiFinishAuthenticationStep finishAuthenticationStep;
    private final CbiAccountFetchingStep accountFetchingStep;

    public IccreaAuthenticator(
            CbiGlobeAuthApiClient authApiClient,
            CbiGlobeFetcherApiClient fetcherApiClient,
            CbiStorage storage,
            LocalDateTimeSource localDateTimeSource,
            Credentials credentials,
            UserInteractions userInteractions,
            CbiUrlProvider urlProvider) {
        consentCreationStep =
                new CbiConsentCreationStep(authApiClient, localDateTimeSource, storage);
        scaMethodSelectionStep =
                new IccreaScaMethodSelectionStep(
                        authApiClient, urlProvider.getUpdateConsentsRawUrl());
        credentialsAuthenticationStep =
                new IccreaCredentialsAuthenticationStep(
                        authApiClient, credentials, urlProvider.getUpdateConsentsRawUrl());
        consentAuthorizationStep =
                new IccreaConsentAuthorizationStep(authApiClient, userInteractions, storage);
        finishAuthenticationStep =
                new CbiFinishAuthenticationStep(authApiClient, credentials, storage);
        accountFetchingStep = new CbiAccountFetchingStep(fetcherApiClient, storage);
    }

    @Override
    public void authenticate(Credentials credentials) {
        CbiConsentResponse consentResponse = consentCreationStep.createConsentAndSaveId();
        consentResponse = scaMethodSelectionStep.pickScaMethod(consentResponse);
        credentialsAuthenticationStep.authenticate(consentResponse, CbiConsentResponse.class);
        consentAuthorizationStep.authorizeConsent();
        finishAuthenticationStep.storeConsentValidUntilDateInCredentials();
        accountFetchingStep.fetchAndSaveAccounts();
    }

    @Override
    public CredentialsTypes getType() {
        return CredentialsTypes.PASSWORD;
    }
}
