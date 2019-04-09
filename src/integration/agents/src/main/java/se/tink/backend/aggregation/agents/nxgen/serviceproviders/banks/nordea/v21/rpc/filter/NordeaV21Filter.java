package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.rpc.filter;

import java.util.Objects;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants.Header;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21Constants.HeaderKey;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public abstract class NordeaV21Filter extends Filter {
    private int requestId = 1;
    private final String marketCode;

    protected NordeaV21Filter(String marketCode) {
        this.marketCode = marketCode;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest)
            throws HttpClientException, HttpResponseException {
        httpRequest.getHeaders().add(HeaderKey.REQUEST_ID, requestId++);
        httpRequest
                .getHeaders()
                .add(Header.PLATFORM_VERSION.getKey(), Header.PLATFORM_VERSION.getValue());
        httpRequest.getHeaders().add(HeaderKey.APP_COUNTRY, marketCode);
        httpRequest.getHeaders().add(HeaderKey.APP_VERSION, marketCode);
        httpRequest.getHeaders().add(Header.APP_LANGUAGE.getKey(), Header.APP_LANGUAGE.getValue());
        httpRequest.getHeaders().add(Header.APP_NAME.getKey(), Header.APP_NAME.getValue());
        httpRequest.getHeaders().add(Header.DEVICE_MAKE.getKey(), Header.DEVICE_MAKE.getValue());
        httpRequest.getHeaders().add(Header.DEVICE_MODEL.getKey(), Header.DEVICE_MODEL.getValue());
        httpRequest
                .getHeaders()
                .add(Header.PLATFORM_TYPE.getKey(), Header.PLATFORM_TYPE.getValue());
        httpRequest.getHeaders().add("Accept", MediaType.WILDCARD);

        if (Objects.equals(httpRequest.getMethod(), HttpMethod.POST)) {
            httpRequest.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON);
            httpRequest.getHeaders().add("Accept", MediaType.APPLICATION_JSON);
        }

        URL url =
                httpRequest
                        .getUrl()
                        .parameter(NordeaV21Constants.UrlParameter.MARKET_CODE, marketCode)
                        .parameter(
                                NordeaV21Constants.UrlParameter.LOWER_CASE_MARKET_CODE,
                                marketCode.toLowerCase());
        httpRequest.setUrl(url);

        return nextFilter(httpRequest);
    }
}
