package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.aggregation.agents.utils.jersey.MessageSignInterceptor;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

public class BoursoramaMessageSignFilter extends MessageSignInterceptor {

    private final BoursoramaSignatureHeaderGenerator boursoramaSignatureHeaderGenerator;

    public BoursoramaMessageSignFilter(
            BoursoramaSignatureHeaderGenerator boursoramaSignatureHeaderGenerator) {
        this.boursoramaSignatureHeaderGenerator = boursoramaSignatureHeaderGenerator;
    }

    @Override
    protected void appendAdditionalHeaders(HttpRequest request) {
        request.getHeaders().add(Psd2Headers.Keys.X_REQUEST_ID, UUID.randomUUID());
        String date =
                DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now(ZoneId.of("GMT")));
        request.getHeaders().add(Psd2Headers.Keys.DATE, date);
    }

    @Override
    protected void getSignatureAndAddAsHeader(HttpRequest request) {
        String signatureHeaderValue =
                boursoramaSignatureHeaderGenerator.getSignatureHeaderValue(
                        request.getMethod().name(),
                        request.getURI(),
                        (String) request.getHeaders().getFirst(Psd2Headers.Keys.DIGEST),
                        String.valueOf(
                                request.getHeaders().getFirst(Psd2Headers.Keys.X_REQUEST_ID)),
                        (String) request.getHeaders().getFirst(Psd2Headers.Keys.DATE),
                        String.valueOf(request.getHeaders().getFirst(HttpHeaders.CONTENT_TYPE)));

        request.getHeaders().add(Psd2Headers.Keys.SIGNATURE, signatureHeaderValue);
    }

    @Override
    protected void prepareDigestAndAddAsHeader(HttpRequest request) {
        String requestBody = (String) Optional.ofNullable(request.getBody()).orElse("");
        request.getHeaders()
                .add(
                        Psd2Headers.Keys.DIGEST,
                        boursoramaSignatureHeaderGenerator.getDigestHeaderValue(requestBody));
    }
}
