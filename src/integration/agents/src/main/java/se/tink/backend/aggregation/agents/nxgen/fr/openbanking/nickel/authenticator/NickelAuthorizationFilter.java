package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.authenticator;

import java.nio.charset.StandardCharsets;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.ws.rs.core.MultivaluedMap;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcAlgorithm;
import se.tink.agent.sdk.utils.signer.qsealc.QsealcSigner;
import se.tink.agent.sdk.utils.signer.qsealc.SignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.nickel.NickelConstants.HeaderValues;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterOrder;
import se.tink.backend.aggregation.nxgen.http.filter.engine.FilterPhases;
import se.tink.backend.aggregation.nxgen.http.filter.filters.iface.Filter;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.libraries.cryptography.hash.Hash;
import se.tink.libraries.serialization.utils.SerializationUtils;

@FilterOrder(category = FilterPhases.REQUEST_HANDLE, order = Integer.MAX_VALUE)
public class NickelAuthorizationFilter extends Filter {

    private final SignatureHeaderGenerator signatureHeaderGenerator;
    private final String userIp;
    private final String financialAuthorizationNumber;
    private final LocalDateTimeSource localDateTimeSource;

    public NickelAuthorizationFilter(
            String financialAuthorizationNumber,
            String userIp,
            String qsealcThumbprint,
            QsealcSigner qsealcSigner,
            LocalDateTimeSource localDateTimeSource) {
        this.userIp = userIp;
        this.financialAuthorizationNumber = financialAuthorizationNumber;
        this.localDateTimeSource = localDateTimeSource;
        signatureHeaderGenerator =
                new SignatureHeaderGenerator(
                        NickelConstants.SIGNATURE_FORMAT,
                        NickelConstants.SIGNABLE_HEADERS,
                        qsealcThumbprint,
                        qsealcSigner,
                        QsealcAlgorithm.RSA_SHA256);
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {

        MultivaluedMap<String, Object> headers = httpRequest.getHeaders();

        headers.putSingle(HeaderKeys.HOST, httpRequest.getURI().getHost());
        addDigestHeader(httpRequest);
        headers.putSingle(HeaderKeys.PSU_IP_ADDRESS, userIp);
        headers.putSingle(HeaderKeys.PSU_USER_AGENT, HeaderValues.PSU_USER_AGENT);
        headers.putSingle(HeaderKeys.TPP_ETSI_AUTHORIZATION_NUMBER, financialAuthorizationNumber);
        headers.putSingle(
                HeaderKeys.TPP_SIGNATURE_TIMESTAMP,
                DateTimeFormatter.ISO_LOCAL_DATE_TIME
                        .withZone(ZoneId.of("CET"))
                        .format(localDateTimeSource.getInstant(ZoneId.of("CET"))));
        sign(httpRequest);
        return nextFilter(httpRequest);
    }

    private void sign(HttpRequest httpRequest) {
        final Map<String, Object> headers = new LinkedHashMap<>();
        headers.put(HeaderKeys.REQUEST_TARGET, buildRequestTarget(httpRequest));
        httpRequest.getHeaders().entrySet().stream()
                .filter(entry -> entry.getValue().size() >= 1)
                .forEach(a -> headers.put(a.getKey(), a.getValue().get(0)));
        httpRequest
                .getHeaders()
                .putSingle(
                        NickelConstants.HeaderKeys.SIGNATURE,
                        signatureHeaderGenerator.generateSignatureHeader(headers));
    }

    private String buildRequestTarget(HttpRequest httpRequest) {
        StringBuilder stringBuilder =
                new StringBuilder()
                        .append(httpRequest.getMethod().name().toLowerCase())
                        .append(' ')
                        .append(httpRequest.getURI().getPath());
        if (httpRequest.getURI().getQuery() != null) {
            stringBuilder.append('?').append(httpRequest.getURI().getQuery());
        }
        return stringBuilder.toString();
    }

    private void addDigestHeader(HttpRequest httpRequest) {
        if (httpRequest.getBody() != null) {
            String body = SerializationUtils.serializeToString(httpRequest.getBody());
            if (body != null) {
                httpRequest.setBody(body);
                httpRequest
                        .getHeaders()
                        .putSingle(
                                HeaderKeys.DIGEST,
                                Hash.sha256Base64(body.getBytes(StandardCharsets.UTF_8)));
            }
        }
    }
}
