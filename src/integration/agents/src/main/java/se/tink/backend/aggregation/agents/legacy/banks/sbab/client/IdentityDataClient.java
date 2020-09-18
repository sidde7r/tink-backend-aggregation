package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.sun.jersey.api.client.Client;
import java.util.NoSuchElementException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class IdentityDataClient extends SBABClient implements IdentityDataFetcher {

    public IdentityDataClient(Client client, Credentials credentials, String userAgent) {
        super(client, credentials, userAgent);
    }

    @Override
    public IdentityData fetchIdentityData() {
        Document overview = getJsoupDocument(OVERVIEW_URL);

        Element loggedInAs = overview.getElementById("loggedInAs");

        if (loggedInAs == null) {
            throw new NoSuchElementException("Could not find name. HTML changed?");
        }

        return SeIdentityData.of(loggedInAs.text().trim(), credentials.getField(Key.USERNAME));
    }
}
