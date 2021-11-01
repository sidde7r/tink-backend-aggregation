package se.tink.backend.aggregation.agents.banks.sbab.client;

import static se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.HeaderKeys.USER_AGENT;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import java.net.URI;
import java.util.function.Function;
import javax.ws.rs.core.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.constants.CommonHeaders;

public class SBABClient {

    final Client client;
    final Function<String, URI> uriFunction;
    final Credentials credentials;

    public SBABClient(Client client, Function<String, URI> uriFunction, Credentials credentials) {
        this.client = client;
        this.uriFunction = uriFunction;
        this.credentials = credentials;
    }

    WebResource createResource(String url) {
        return client.resource(uriFunction.apply(url));
    }

    Builder createJsonRequest(String url) {
        return createResource(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .header(USER_AGENT, CommonHeaders.DEFAULT_USER_AGENT);
    }

    Document getJsoupDocument(String url) {
        return Jsoup.parse(
                createResource(url)
                        .accept(MediaType.TEXT_HTML)
                        .header(USER_AGENT, CommonHeaders.DEFAULT_USER_AGENT)
                        .get(String.class));
    }
}
