package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet;

import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.entitiy.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordnetApiClient extends NordnetBaseApiClient {

    public NordnetApiClient(
            TinkHttpClient client,
            Credentials credentials,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        super(client, credentials, persistentStorage, sessionStorage);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        RequestBuilder requestBuilder =
                createRequestInSession(new URL(NordnetBaseConstants.Urls.CUSTOMER_INFO))
                        .type(MediaType.APPLICATION_JSON)
                        .accept(MediaType.APPLICATION_JSON);

        return new FetchIdentityDataResponse(
                get(requestBuilder, CustomerEntity.class).toTinkIdentity());
    }
}
