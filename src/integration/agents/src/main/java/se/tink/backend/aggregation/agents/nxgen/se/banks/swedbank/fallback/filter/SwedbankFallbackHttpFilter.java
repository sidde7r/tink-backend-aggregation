package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.filter;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import java.security.cert.CertificateException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.SwedbankFallbackConstants;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.SwedbankFallbackConstants.Headers;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.configuration.SwedbankPsd2Configuration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback.utils.SignatureUtils;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.api.Psd2Headers.Keys;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.eidassigner.identity.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;

public class SwedbankFallbackHttpFilter extends Filter {

    private final RandomValueGenerator randomValueGenerator;
    private final SwedbankPsd2Configuration configuration;
    private final AgentsServiceConfiguration agentsServiceConfiguration;
    private final EidasIdentity eidasIdentity;
    private final String qSealcBase64;

    public SwedbankFallbackHttpFilter(
            RandomValueGenerator randomValueGenerator,
            SwedbankPsd2Configuration configuration,
            AgentsServiceConfiguration agentsServiceConfiguration,
            EidasIdentity eidasIdentity,
            String qSealc) {
        this.randomValueGenerator = randomValueGenerator;
        this.configuration = configuration;
        this.agentsServiceConfiguration = agentsServiceConfiguration;
        this.eidasIdentity = eidasIdentity;
        try {
            this.qSealcBase64 =
                    CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(qSealc);
        } catch (CertificateException e) {
            throw new IllegalStateException("Invalid qsealc detected", e);
        }
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        appendFallbackHeaders(httpRequest);

        HttpResponse resp = nextFilter(httpRequest);

        // Don't handle http exceptions when fetching transactions. Since Swedbank frequently
        // returns 500 when fetching transactions we've added logic to return whatever we got
        // in the transaction fetcher.
        if (!httpRequest.getUrl().get().contains(SwedbankSEConstants.Endpoint.TRANSACTIONS_BASE)) {
            handleException(resp);
        }

        return resp;
    }

    private void appendFallbackHeaders(HttpRequest request) {

        final EidasProxyConfiguration eidasProxyConfig = agentsServiceConfiguration.getEidasProxy();
        final String digest = SignatureUtils.getDigestHeaderValue(request);
        final Map<String, Object> headers = getHeaders(digest);
        final String signature =
                SignatureUtils.generateSignatureHeader(
                        headers, eidasProxyConfig, eidasIdentity, qSealcBase64);

        for (Entry<String, Object> header : headers.entrySet()) {
            request.getHeaders().add(header.getKey(), header.getValue());
        }
        request.getHeaders().add(Keys.SIGNATURE, signature);
        request.getHeaders().add(Headers.DSID, generateDSID());
    }

    private Map<String, Object> getHeaders(String digest) {

        Map<String, Object> headers = new HashMap<>();

        headers.put(Keys.X_CLIENT, Headers.CLIENT_NAME);
        headers.put(Keys.TPP_X_REQUEST_ID, Psd2Headers.getRequestId());
        headers.put(Keys.TPP_APP_ID, configuration.getClientId());
        headers.put(Keys.DATE.toLowerCase(), SwedbankFallbackConstants.getCurrentFormattedDate());
        headers.put(Keys.DIGEST.toLowerCase(), digest);
        headers.put(Keys.TPP_SIGNATURE_CERTIFICATE, qSealcBase64);

        return headers;
    }

    private void handleException(HttpResponse response) {
        if (response.getStatus() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
            String error = Strings.nullToEmpty(response.getBody(String.class)).toLowerCase();
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Http status: " + response.getStatus() + ", body: " + error);
        }
    }

    private String generateDSID() {
        return randomValueGenerator.generateRandomBase64UrlEncoded(6);
    }
}
