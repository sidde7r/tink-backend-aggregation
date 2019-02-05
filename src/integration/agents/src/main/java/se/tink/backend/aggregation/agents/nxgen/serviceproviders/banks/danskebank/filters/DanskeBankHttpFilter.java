package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.filters;

import java.util.Objects;
import javax.ws.rs.core.MediaType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.danskebank.DanskeBankConfiguration;
import se.tink.backend.aggregation.nxgen.http.HttpMethod;
import se.tink.backend.aggregation.nxgen.http.HttpRequest;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.filter.Filter;

public class DanskeBankHttpFilter extends Filter {
    private static final String CLIENT_ID_KEY = "x-ibm-client-id";
    private static final String CLIENT_SECRET_KEY = "x-ibm-client-secret";
    private static final String APP_CULTURE_KEY = "x-app-culture";
    private static final String ADRUM_KEY = "ADRUM";
    private static final String ADRUM_VALUE = "isAjax:true";
    private static final String APP_VERSION_KEY = "x-app-version";
    private static final String ADRUM1_KEY = "ADRUM_1";
    private static final String ADRUM1_VALUE = "isMobile:true";

    private final DanskeBankConfiguration configuration;

    public DanskeBankHttpFilter(DanskeBankConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public HttpResponse handle(HttpRequest httpRequest) throws HttpClientException, HttpResponseException {
        httpRequest.getHeaders().add("Accept", MediaType.APPLICATION_JSON);

        httpRequest.getHeaders().add(ADRUM_KEY, ADRUM_VALUE);
        httpRequest.getHeaders().add(ADRUM1_KEY, ADRUM1_VALUE);

        // Danske Bank in Finland have a header limit of 17 headers. This is solved by not adding the x-app-culture
        // header (for Finland only).
        if (configuration.shouldAddXAppCultureHeader()) {
            httpRequest.getHeaders().add(APP_CULTURE_KEY, configuration.getAppCulture());
        }

        httpRequest.getHeaders().add(CLIENT_ID_KEY, configuration.getClientId());
        httpRequest.getHeaders().add(CLIENT_SECRET_KEY, configuration.getClientSecret());

        httpRequest.getHeaders().add(APP_VERSION_KEY, configuration.getAppVersionHeader());

        if (Objects.equals(HttpMethod.POST, httpRequest.getMethod())) {
            httpRequest.getHeaders().add("Content-Type", MediaType.APPLICATION_JSON);
        }

        return nextFilter(httpRequest);
    }
}
