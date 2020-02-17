package se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory;

import java.util.Map;
import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.MockSupplementalInformationProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class MockSupplementalInformationProviderFactory
        implements SupplementalInformationProviderFactory {

    private final Map<String, String> mockCallbackData;

    public MockSupplementalInformationProviderFactory(final Map<String, String> mockCallbackData) {
        this.mockCallbackData = mockCallbackData;
    }

    @Override
    public SupplementalInformationProvider createSupplementalInformationProvider(
            SupplementalRequester supplementalRequester, CredentialsRequest credentialsRequest) {

        return new MockSupplementalInformationProvider(mockCallbackData);
    }
}
