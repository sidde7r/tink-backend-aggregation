package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.api.Psd2Headers;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.request.HttpRequest;

@RequiredArgsConstructor
public class BoursoramaPostRequestSignFilter extends BoursoramaRequestSignFilterBase {

    @Getter private final BoursoramaSignatureHeaderGenerator boursoramaSignatureHeaderGenerator;

    @Override
    String getSignatureHeaderValue(HttpRequest request) {
        return boursoramaSignatureHeaderGenerator.getSignatureHeaderValueForPost(
                request.getURI(),
                (String) request.getHeaders().getFirst(Psd2Headers.Keys.DIGEST),
                String.valueOf(request.getHeaders().getFirst(Psd2Headers.Keys.X_REQUEST_ID)),
                (String) request.getHeaders().getFirst(Psd2Headers.Keys.DATE));
    }

    @Override
    HttpMethod getHttpMethod() {
        return HttpMethod.POST;
    }

    @Override
    String prepareRequestBody(HttpRequest request) {
        String serializedBody = serializeBodyIfNecessary(request);
        request.setBody(serializedBody);
        return serializedBody;
    }
}
