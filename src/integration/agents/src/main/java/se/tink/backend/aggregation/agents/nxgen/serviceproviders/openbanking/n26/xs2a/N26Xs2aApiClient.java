package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.xs2a;

import java.util.Map;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.N26Constants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersForAgentPlatformApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.authenticator.Xs2aAuthenticationDataAccessor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.configuration.Xs2aDevelopersProviderConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class N26Xs2aApiClient extends Xs2aDevelopersForAgentPlatformApiClient {

    public N26Xs2aApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Xs2aDevelopersProviderConfiguration configuration,
            boolean userPresent,
            String userIp,
            RandomValueGenerator randomValueGenerator,
            Xs2aAuthenticationDataAccessor xs2aAuthenticationDataAccessor) {
        super(
                client,
                persistentStorage,
                configuration,
                userPresent,
                userIp,
                randomValueGenerator,
                xs2aAuthenticationDataAccessor);
    }

    @Override
    protected Map<String, Object> getUserSpecificHeaders() {
        Map<String, Object> headers = super.getUserSpecificHeaders();
        headers.put(HeaderKeys.PSU_IP_ADDRESS, userIp);
        headers.put(HeaderKeys.PSU_INITIATED, "true");
        return headers;
    }

    @Override
    protected RequestBuilder createRequest(URL url) {
        return super.createRequest(URL.of(N26Constants.parseXs2aUrl(url)));
    }
}
