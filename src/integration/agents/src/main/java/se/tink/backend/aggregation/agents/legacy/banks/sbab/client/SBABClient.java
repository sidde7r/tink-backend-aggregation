package se.tink.backend.aggregation.agents.banks.sbab.client;

import static se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.HeaderKeys.USER_AGENT;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;
import javax.ws.rs.core.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.constants.CommonHeaders;

public class SBABClient {

    final Client client;
    final Credentials credentials;

    public SBABClient(Client client, Credentials credentials) {
        this.client = client;
        this.credentials = credentials;
    }

    Builder createRequest(String url) {
        return client.resource(url).header(USER_AGENT, CommonHeaders.DEFAULT_USER_AGENT);
    }

    Builder createJsonRequest(String url) {
        return createRequest(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON);
    }

    Document getJsoupDocument(String url) {
        return Jsoup.parse(createRequest(url).accept(MediaType.TEXT_HTML).get(String.class));
    }
}
