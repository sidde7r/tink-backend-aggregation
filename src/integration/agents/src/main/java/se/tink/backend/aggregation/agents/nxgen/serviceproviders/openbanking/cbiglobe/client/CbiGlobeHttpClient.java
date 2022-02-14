package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client;

import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import javax.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HeaderKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryValues;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.RequestContext;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.errorhandle.CbiErrorHandler;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filterable.request.RequestBuilder;
import se.tink.backend.aggregation.nxgen.http.request.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
@Slf4j
public class CbiGlobeHttpClient {

    private final TinkHttpClient client;
    private final RandomValueGenerator randomValueGenerator;
    private final LocalDateTimeSource localDateTimeSource;

    private final CbiGlobeProviderConfiguration providerConfiguration;

    protected final StrongAuthenticationState strongAuthenticationState;
    protected final String redirectUrl;
    private final String psuIpAddress;

    <T> T makeRequest(
            RequestBuilder request,
            HttpMethod method,
            Class<T> returnType,
            RequestContext context,
            Object body) {

        try {
            if (body != null) {
                return makeBodyRequest(request, method, returnType, body);
            } else {
                return makeNonBodyRequest(request, method, returnType);
            }
        } catch (HttpResponseException hre) {
            CbiErrorHandler.handleError(hre, context);
            throw hre;
        }
    }

    private <T> T makeBodyRequest(
            RequestBuilder request, HttpMethod method, Class<T> returnType, Object body) {
        switch (method) {
            case POST:
                return request.post(returnType, body);
            case PATCH:
                return request.patch(returnType, body);
            case PUT:
                return request.put(returnType, body);
            case DELETE:
                return request.delete(returnType, body);
            default:
                log.error(String.format("Unknown HTTP method: %s", method.name()));
                return request.post(returnType, body);
        }
    }

    private <T> T makeNonBodyRequest(
            RequestBuilder request, HttpMethod method, Class<T> returnType) {
        switch (method) {
            case GET:
                return request.get(returnType);
            case POST:
                return request.post(returnType);
            case PATCH:
                return request.patch(returnType);
            case PUT:
                return request.put(returnType);
            case DELETE:
                return request.delete(returnType);
            default:
                log.error(String.format("Unknown HTTP method: %s", method.name()));
                return request.get(returnType);
        }
    }

    RequestBuilder createRequestInSession(URL url) {
        return client.request(url)
                .type(MediaType.APPLICATION_JSON)
                .header(HeaderKeys.X_REQUEST_ID, randomValueGenerator.getUUID())
                .header(HeaderKeys.ASPSP_CODE, providerConfiguration.getAspspCode())
                .header(
                        HeaderKeys.DATE,
                        formatDate(Date.from(localDateTimeSource.getInstant(ZoneId.of("CET")))));
    }

    RequestBuilder createRequestInSessionWithPsuIp(URL url) {
        return createRequestInSession(url).header(HeaderKeys.PSU_IP_ADDRESS, psuIpAddress);
    }

    private String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.ENGLISH);
        return sdf.format(date);
    }

    protected String buildRedirectUri(boolean isOk) {
        return new URL(redirectUrl)
                .queryParam(QueryKeys.STATE, strongAuthenticationState.getState())
                .queryParam(QueryKeys.RESULT, isOk ? QueryValues.SUCCESS : QueryValues.FAILURE)
                .get();
    }
}
