package se.tink.backend.aggregation.agents.banks.sbab.client;

import static se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.HeaderKeys.CSRF_TOKEN;
import static se.tink.backend.aggregation.agents.banks.sbab.SBABConstants.HeaderKeys.USER_AGENT;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.WebResource.Builder;
import javax.ws.rs.core.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import se.tink.backend.agents.rpc.Credentials;

public class SBABClient {

    final Client client;
    final Credentials credentials;

    private String csrfToken;
    private final String userAgent;

    public SBABClient(Client client, Credentials credentials, String userAgent) {
        this.client = client;
        this.credentials = credentials;
        this.userAgent = userAgent;
    }

    Builder createRequest(String url) {
        return client.resource(url).header(USER_AGENT, userAgent);
    }

    Builder createJsonRequestWithCsrf(String url) {
        return createRequest(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .header(CSRF_TOKEN, csrfToken);
    }

    Document getJsoupDocument(String url) {
        return Jsoup.parse(createRequest(url).accept(MediaType.TEXT_HTML).get(String.class));
    }

    public void setCsrfToken(String token) {
        csrfToken = token;
    }
}
