package se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.factory;

import se.tink.backend.aggregation.agents.contexts.SupplementalRequester;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.supplementalinformation.SupplementalInformationProvider;
import se.tink.libraries.credentials.service.CredentialsRequest;

public interface SupplementalInformationProviderFactory {

    SupplementalInformationProvider createSupplementalInformationProvider(
            final SupplementalRequester supplementalRequester,
            final CredentialsRequest credentialsRequest);
}
