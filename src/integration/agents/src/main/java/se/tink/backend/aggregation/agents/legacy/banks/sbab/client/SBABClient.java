package se.tink.backend.aggregation.agents.banks.sbab.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.client.filter.ClientFilter;
import java.net.URI;
import java.net.URISyntaxException;
import javax.ws.rs.core.MediaType;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.banks.sbab.configuration.SBABConfiguration;

public class SBABClient {

    final Client client;
    final Credentials credentials;
    final ObjectMapper MAPPER = new ObjectMapper();

    protected String signBaseUrl;

    static final String SECURE_BASE_URL = "https://secure.sbab.se";
    static final String OVERVIEW_URL = SECURE_BASE_URL + "/privat";

    private String bearerToken;
    private String remoteIp;
    private final String userAgent;

    public SBABClient(Client client, Credentials credentials, String userAgent) {
        this.client = client;
        this.credentials = credentials;
        this.userAgent = userAgent;
    }

    public void setConfiguration(SBABConfiguration sbabConfiguration) {
        this.signBaseUrl = sbabConfiguration.getSignBaseUrl();
    }

    Builder createRequest(String url) {
        Builder builder = client.resource(url).header("User-Agent", userAgent);
        return Strings.isNullOrEmpty(remoteIp)
                ? builder
                : builder.header("X-Forwarded-For", remoteIp);
    }

    Builder createFormEncodedHtmlRequest(String url) {
        return createRequest(url)
                .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                .accept(MediaType.TEXT_HTML_TYPE);
    }

    Builder createHtmlRequest(String url) {
        return createRequest(url).accept(MediaType.TEXT_HTML);
    }

    Builder createJsonRequestWithBearer(String url) {
        return createRequest(url)
                .type(MediaType.APPLICATION_JSON_TYPE)
                .accept(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + bearerToken);
    }

    Document getJsoupDocument(String url) {
        return Jsoup.parse(createRequest(url).accept(MediaType.TEXT_HTML).get(String.class));
    }

    String getRedirectUrl(ClientResponse response, String baseUrl) throws URISyntaxException {
        String location = response.getHeaders().getFirst("Location");
        Preconditions.checkState(
                !Strings.isNullOrEmpty(location),
                "Did not get redirect url in response from bank.");
        return hasHost(location) ? location : baseUrl + location;
    }

    String portletResponseToValidJson(String portletResponse) {
        portletResponse = portletResponse.trim();
        portletResponse = portletResponse.replace("while(true);", "");
        portletResponse = portletResponse.replaceAll("^\"|\"$", "");
        portletResponse = portletResponse.replace("\\", "");
        return portletResponse;
    }

    private static boolean hasHost(String url) throws URISyntaxException {
        URI uri = new URI(url);
        return uri.getHost() != null;
    }

    public void addFilter(ClientFilter filter) {
        client.addFilter(filter);
    }

    protected String getUrl(String baseUrl, String path) {
        return baseUrl + path;
    }

    protected String getUrl(String baseUrl, String path, Object... args) {
        return String.format(getUrl(baseUrl, path), args);
    }

    public void setBearerToken(String token) {
        bearerToken = token;
    }
}
