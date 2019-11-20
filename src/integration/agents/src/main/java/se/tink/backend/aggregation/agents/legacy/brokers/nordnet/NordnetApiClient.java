package se.tink.backend.aggregation.agents.brokers.nordnet;

import com.google.common.base.Strings;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.core.util.MultivaluedMapImpl;
import java.util.Base64;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.UriBuilder;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.impl.client.DefaultRedirectStrategy;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.brokers.nordnet.NordnetConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.brokers.nordnet.NordnetConstants.Urls;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.AccountEntity;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.AccountInfoEntity;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.CustomerInfoResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.PositionsResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Response.AccountInfoResponse;
import se.tink.backend.aggregation.agents.brokers.nordnet.model.Response.AccountResponse;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.net.TinkApacheHttpClient4;

public class NordnetApiClient {
    private static final Logger log = LoggerFactory.getLogger(NordnetApiClient.class);

    private TinkApacheHttpClient4 client;

    private String referrer;
    private final String aggregator;
    /** A concatenated string of account's bank-id (seems to be a simple client specific index) */
    private String accountBankIds;

    private String sessionKey;
    private String accessToken;
    private String ntag;

    public String getNtag() {
        return ntag;
    }

    public void setSessionKey(String sessionKey) {
        this.sessionKey = sessionKey;
    }

    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public void setNtag(String ntag) {
        this.ntag = ntag;
    }

    public NordnetApiClient(TinkApacheHttpClient4 client, String aggregator) {
        this.aggregator = aggregator;
        this.client = client;
    }

    Optional<String> getReferrer() {
        return Optional.ofNullable(Strings.emptyToNull(referrer));
    }

    private void setNextReferrer(MultivaluedMap<String, String> headers) {
        String nextReferrer = headers.getFirst(NordnetConstants.HeaderKeys.NEXT_REFERRER);

        if (!Strings.isNullOrEmpty(nextReferrer)) {
            referrer = Urls.BASE_URL + nextReferrer;
        }
    }

    public AccountResponse fetchAccounts() {

        String uri = UriBuilder.fromUri(Urls.GET_ACCOUNTS_URL).build().toASCIIString();
        AccountResponse accounts = this.get(uri, AccountResponse.class);

        accountBankIds =
                accounts.stream().map(a -> a.getAccountId()).collect(Collectors.joining(","));
        AccountInfoResponse infos =
                this.get(
                        String.format(Urls.GET_ACCOUNTS_INFO_URL, accountBankIds),
                        AccountInfoResponse.class);

        for (AccountEntity accountEntity : accounts) {
            String accId = accountEntity.getAccountId();

            for (AccountInfoEntity infoEntity : infos) {
                String infoId = infoEntity.getAccountId();

                if (accId.equalsIgnoreCase(infoId)) {
                    log.info("Nordnet-account-info : {}", infoEntity.toString());
                    accountEntity.setInfo(infoEntity);
                    break;
                }
            }
        }

        return accounts;
    }

    public IdentityData fetchIdentityData() {
        CustomerInfoResponse customerInfo =
                get(Urls.GET_CUSTOMER_INFO_URL, CustomerInfoResponse.class);

        return customerInfo.toTinkIdentity();
    }

    <T> T post(String url, Object request, Class<T> responseEntity) {
        return createClientRequest(url).post(responseEntity, request);
    }

    <T> T postForm(String url, MultivaluedMapImpl request, Class<T> responseEntity) {
        ClientResponse response = postForm(url, request);

        return response.getEntity(responseEntity);
    }

    ClientResponse postForm(String url, MultivaluedMap request) {
        ClientResponse response =
                createClientRequest(url)
                        .type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
                        .post(ClientResponse.class, request);

        manage(response);
        return response;
    }

    <T> T get(String url, Class<T> responseEntity) {
        return get(url).getEntity(responseEntity);
    }

    ClientResponse get(String url) {
        ClientResponse response = createClientRequest(url).get(ClientResponse.class);
        manage(response);

        return response;
    }

    void manage(ClientResponse response) {
        if (response.getStatus() >= 400) {
            throw new UniformInterfaceException(response);
        }

        setNextReferrer(response.getHeaders());
    }

    WebResource.Builder createClientRequest(String url) {
        return createClientRequest(url, MediaType.APPLICATION_JSON_TYPE, Collections.emptyMap());
    }

    WebResource.Builder createClientRequest(String url, MediaType contentType) {
        return createClientRequest(url, contentType, Collections.emptyMap());
    }

    private WebResource.Builder createClientRequest(
            String url, MediaType contentType, Map<String, String> headers) {
        WebResource.Builder requestBuilder =
                client.resource(url)
                        .header(HttpHeaders.USER_AGENT, aggregator)
                        .accept(
                                "text/html",
                                "application/xhtml+xml",
                                "application/xml;q=0.9",
                                "*/*;q=0.8")
                        .type(contentType);
        if (!Strings.isNullOrEmpty(sessionKey)) {
            requestBuilder.header(HttpHeaders.AUTHORIZATION, getBasicAuthHeader());
        }

        Optional<String> referrer = getReferrer();

        for (String header : headers.keySet()) {
            requestBuilder.header(header, headers.get(header));
        }

        referrer.ifPresent(s -> requestBuilder.header(HttpHeaders.REFERER, s));

        if (!Strings.isNullOrEmpty(accessToken)) {
            requestBuilder.header(
                    HttpHeaders.AUTHORIZATION, String.format("Bearer %s", accessToken));
        }

        return requestBuilder;
    }

    private String getBasicAuthHeader() {
        return "Basic "
                + Base64.getEncoder().encodeToString((sessionKey + ":" + sessionKey).getBytes());
    }

    public static final RedirectStrategy REDIRECT_STRATEGY =
            new DefaultRedirectStrategy() {
                @Override
                public boolean isRedirected(
                        HttpRequest request, HttpResponse response, HttpContext context) {
                    String referrer = request.getRequestLine().getUri();
                    response.setHeader(HeaderKeys.NEXT_REFERRER, referrer);

                    String location = getLocationUri(response);
                    return location != null
                            && !location.startsWith("/now/mobile/")
                            && !location.startsWith("/mux/login/startse.html")
                            && !location.startsWith("/mux/login/login_eleg.html");
                }

                private String getLocationUri(HttpResponse response) {
                    Header header = response.getFirstHeader(HttpHeaders.LOCATION);

                    if (header == null) {
                        return null;
                    }
                    String location =
                            header.getValue().toLowerCase().replace("https://www.nordnet.se", "");
                    location = location.toLowerCase().replace("https://classic.nordnet.se", "");
                    return location;
                }
            };

    public Optional<PositionsResponse> getPositions() {
        // Always fetches positions for all accounts/portfolios, but called once for each.
        try {
            ClientResponse clientResponse =
                    get(String.format(Urls.GET_POSITIONS_URL, accountBankIds));
            PositionsResponse response = clientResponse.getEntity(PositionsResponse.class);
            return Optional.of(response);
        } catch (UniformInterfaceException e) {
            ClientResponse response = e.getResponse();
            if (response.getStatus() != 204) {
                log.warn("nordnet - position fetching failed", e);
            }
            return Optional.empty();
        }
    }
}
