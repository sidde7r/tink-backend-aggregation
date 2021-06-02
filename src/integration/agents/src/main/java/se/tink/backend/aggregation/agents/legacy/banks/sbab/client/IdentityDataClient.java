package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import org.apache.http.HttpStatus;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.ErrorText;
import se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.Url;
import se.tink.backend.aggregation.agents.banks.sbab.entities.Payload;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class IdentityDataClient extends SBABClient implements IdentityDataFetcher {

    public IdentityDataClient(Client client, Credentials credentials, String userAgent) {
        super(client, credentials, userAgent);
    }

    @Override
    public IdentityData fetchIdentityData() {
        ClientResponse response =
                createJsonRequestWithCsrf(Url.IDENTITY_URL).get(ClientResponse.class);

        if (response.getStatus() != HttpStatus.SC_OK) {
            throw new IllegalStateException(
                    ErrorText.HTTP_ERROR
                            + response.getStatus()
                            + ErrorText.HTTP_MESSAGE
                            + response.getEntity(String.class));
        }
        Payload identity = response.getEntity(Payload.class);
        return SeIdentityData.of(
                identity.getPersonalDetails().getUserName(),
                identity.getPersonalDetails().getSsn());
    }
}
