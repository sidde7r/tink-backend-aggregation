package se.tink.backend.aggregation.agents.nxgen.fi.brokers.nordnet;

import javax.ws.rs.core.MediaType;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.brokers.nordnet.authenticator.entity.CustomerEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;

public class NordnetApiClient extends NordnetBaseApiClient {

    private IdentityData cachedIdentityData;

    public NordnetApiClient(
            TinkHttpClient client,
            Credentials credentials,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        super(client, credentials, persistentStorage, sessionStorage);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        if (cachedIdentityData == null) {
            RequestBuilder requestBuilder =
                    createRequestInSession(new URL(NordnetBaseConstants.Urls.CUSTOMER_INFO))
                            .type(MediaType.APPLICATION_JSON)
                            .accept(MediaType.APPLICATION_JSON);

            cachedIdentityData = get(requestBuilder, CustomerEntity.class).toTinkIdentity();
        }

        return new FetchIdentityDataResponse(cachedIdentityData);
    }
}
