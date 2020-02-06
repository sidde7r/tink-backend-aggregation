package se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory;

import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class MockSupplementalInformationProviderFactory
        implements SupplementalInformationProviderFactory {

    @Override
    public SupplementalInformationProvider createSupplementalInformationProvider(
            SupplementalRequester supplementalRequester, CredentialsRequest credentialsRequest) {
        // TODO: implement supplemental information mocking.
        throw new IllegalStateException("Not yet implemented");
    }
}
